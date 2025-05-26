package com.fake.st10262898_budgetbunny_poepart2

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.fake.st10262898_budgetbunny_poepart2.data.ExpenseFirebase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.io.ByteArrayOutputStream

class EditTransactionsActivity : AppCompatActivity() {

    private val db = Firebase.firestore
    private lateinit var expenseId: String
    private var imageBase64: String? = null

    // UI Components
    private lateinit var nameEditText: EditText
    private lateinit var amountEditText: EditText
    private lateinit var imageView: ImageView
    private lateinit var takePicButton: Button
    private lateinit var saveButton: Button

    companion object {
        private const val CAMERA_REQUEST_CODE = 101
        private const val CAMERA_PERMISSION_CODE = 102
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_transaction)

        // Initialize UI components
        nameEditText = findViewById(R.id.etExpenseName)
        amountEditText = findViewById(R.id.etExpenseAmount)
        imageView = findViewById(R.id.ivReceipt)
        takePicButton = findViewById(R.id.btnTakePicture)
        saveButton = findViewById(R.id.btnSaveChanges)

        // Get expense ID from intent
        expenseId = intent.getStringExtra("expenseId") ?: run {
            showErrorAndFinish("Invalid Expense")
            return
        }

        // Load existing expense data
        loadExpenseData()

        // Set up button click listeners
        takePicButton.setOnClickListener {
            if (checkCameraPermission()) {
                openCamera()
            } else {
                requestCameraPermission()
            }
        }

        saveButton.setOnClickListener {
            saveExpenseChanges()
        }
    }

    private fun loadExpenseData() {
        db.collection("expenses").document(expenseId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val expense = ExpenseFirebase(
                        expenseName = document.getString("expenseName") ?: "",
                        expenseAmount = document.getDouble("expenseAmount") ?: 0.0,
                        username = document.getString("username") ?: "",
                        expenseCategory = document.getString("expenseCategory"),
                        expenseDate = document.getLong("expenseDate") ?: 0L,
                        expenseImageBase64 = document.getString("expenseImageBase64")
                    ).apply {
                        id = document.id
                    }

                    nameEditText.setText(expense.expenseName)
                    amountEditText.setText(expense.expenseAmount.toString())
                    imageBase64 = expense.expenseImageBase64

                    // Load image if exists
                    expense.expenseImageBase64?.let { base64 ->
                        val imageBytes = Base64.decode(base64, Base64.DEFAULT)
                        val bitmap = BitmapUtils.byteArrayToBitmap(imageBytes)
                        imageView.setImageBitmap(bitmap)
                    }
                } else {
                    showErrorAndFinish("Expense not found")
                }
            }
            .addOnFailureListener {
                showErrorAndFinish("Error loading expense")
            }
    }

    private fun saveExpenseChanges() {
        val newName = nameEditText.text.toString().trim()
        val newAmount = amountEditText.text.toString().toDoubleOrNull()

        if (newName.isBlank() || newAmount == null) {
            Toast.makeText(this, "Please enter valid name and amount", Toast.LENGTH_SHORT).show()
            return
        }

        // Get the existing expense to preserve unchanged fields
        db.collection("expenses").document(expenseId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val existingExpense = document.toObject(ExpenseFirebase::class.java)!!

                    val updatedExpense = existingExpense.copy(
                        expenseName = newName,
                        expenseAmount = newAmount,
                        expenseImageBase64 = imageBase64 ?: existingExpense.expenseImageBase64
                    )

                    db.collection("expenses").document(expenseId)
                        .set(updatedExpense.toMap())
                        .addOnSuccessListener {
                            Toast.makeText(this, "Expense updated successfully!", Toast.LENGTH_SHORT).show()
                            setResult(Activity.RESULT_OK)
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to save expense", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    showErrorAndFinish("Expense not found")
                }
            }
            .addOnFailureListener {
                showErrorAndFinish("Error loading expense data")
            }
    }

    private fun showErrorAndFinish(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun checkCameraPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_CODE
        )
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as? Bitmap
            imageBitmap?.let {
                imageView.setImageBitmap(it)
                imageBase64 = convertBitmapToBase64(it)
            }
        }
    }

    private fun convertBitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
}