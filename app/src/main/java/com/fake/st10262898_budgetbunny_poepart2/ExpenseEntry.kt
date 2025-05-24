package com.fake.st10262898_budgetbunny_poepart2

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ExpenseEntry : AppCompatActivity() {

    private lateinit var expenseNameEditText: EditText
    private lateinit var expenseAmountEditText: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var nextButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_entry)



        // Bind the views
        expenseNameEditText = findViewById(R.id.editTextText)
        expenseAmountEditText = findViewById(R.id.ExpenseAmount)
        categorySpinner = findViewById(R.id.mySpinner)
        nextButton = findViewById(R.id.button)

        // Set up dropdown with string-array from strings.xml
        val categories = resources.getStringArray(R.array.dropdown_items)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        // Button click listener
        nextButton.setOnClickListener {
            val name = expenseNameEditText.text.toString().trim()
            val amountText = expenseAmountEditText.text.toString().trim()
            val category = categorySpinner.selectedItem?.toString() ?: ""

            if (name.isEmpty() || amountText.isEmpty() || category.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amount = amountText.toDoubleOrNull()
            if (amount == null) {
                Toast.makeText(this, "Enter a valid number for the amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
            val username = sharedPreferences.getString("username", "") ?: ""

            // Get current date
            val currentDate = Calendar.getInstance().timeInMillis

            Log.d("ExpenseEntry", "Saving expense: $name, $amount, $category for $username")

            // Save to Firebase
            val db = FirebaseFirestore.getInstance()
            val expense = hashMapOf(
                "expenseName" to name,
                "expenseAmount" to amount,
                "username" to username,
                "expenseCategory" to category,
                "expenseDate" to currentDate,
                "expenseImage" to null
            )

            db.collection("expenses")
                .add(expense)
                .addOnSuccessListener {
                    Toast.makeText(this, "Expense saved successfully", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to save expense: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}