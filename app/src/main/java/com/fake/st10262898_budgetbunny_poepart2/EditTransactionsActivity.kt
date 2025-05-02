package com.fake.st10262898_budgetbunny_poepart2

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.fake.st10262898_budgetbunny_poepart2.data.BudgetBunnyDatabase
import com.fake.st10262898_budgetbunny_poepart2.data.Expense
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class EditTransactionsActivity : AppCompatActivity() {


    private var expenseId = -1
    private var capturedImage: ByteArray? = null

    companion object {
        private const val CAMERA_REQUEST_CODE = 101
        private const val CAMERA_PERMISSION_CODE = 102
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_transaction)

        expenseId = intent.getIntExtra("expenseId", -1)
        if (expenseId == -1) {
            Toast.makeText(this, "Invalid Expense", Toast.LENGTH_SHORT).show()
            finish()
        }

        val nameEditText = findViewById<EditText>(R.id.etExpenseName)
        val amountEditText = findViewById<EditText>(R.id.etExpenseAmount)
        val imageView = findViewById<ImageView>(R.id.ivReceipt)
        val takePicButton = findViewById<Button>(R.id.btnTakePicture)
        val saveButton = findViewById<Button>(R.id.btnSaveChanges)

        val db = BudgetBunnyDatabase.getDatabase(this)
        val expenseDao = db.expenseDao()

        lifecycleScope.launch {
            try {
                val expense = expenseDao.getExpenseById(expenseId) ?: run {
                    showError("Expense not found")
                    return@launch
                }

                runOnUiThread {
                    nameEditText.setText(expense.expenseName)
                    amountEditText.setText(expense.expenseAmount.toString())

                    expense.expenseImage?.let { byteArray ->
                        val bitmap = BitmapUtils.byteArrayToBitmap(byteArray)
                        imageView.setImageBitmap(bitmap)
                    }
                }
            } catch (e: Exception) {
                showError("Error loading expense")
            }
        }

        takePicButton.setOnClickListener {
            if (checkCameraPermission()) {
                openCamera()
            } else {
                requestCameraPermission()
            }
        }

        saveButton.setOnClickListener {
            val newName = nameEditText.text.toString()
            val newAmount = amountEditText.text.toString().toDoubleOrNull()

            if (newName.isBlank() || newAmount == null) {
                Toast.makeText(this, "Fill in valid name and amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val expense = expenseDao.getExpenseById(expenseId) ?: run {
                        showError("Expense not found")
                        return@launch
                    }

                    val updatedExpense = expense.copy(
                        expenseName = newName,
                        expenseAmount = newAmount,
                        expenseImage = capturedImage ?: expense.expenseImage
                    )

                    expenseDao.updateExpense(updatedExpense)

                    runOnUiThread {
                        Toast.makeText(
                            this@EditTransactionsActivity,
                            "Expense updated!",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                } catch (e: Exception) {
                    showError("Error saving expense")
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val bitmap = data?.extras?.get("data") as? Bitmap ?: return
            findViewById<ImageView>(R.id.ivReceipt).setImageBitmap(bitmap)
            capturedImage = BitmapUtils.bitmapToByteArray(bitmap)
        }
    }
}