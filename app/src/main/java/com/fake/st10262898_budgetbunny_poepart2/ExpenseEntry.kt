package com.fake.st10262898_budgetbunny_poepart2

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ExpenseEntry : AppCompatActivity() {

    private lateinit var expenseNameEditText: EditText
    private lateinit var expenseAmountEditText: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var nextButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_entry) // match your XML file name

// Bind the views
        expenseNameEditText = findViewById(R.id.editTextText)
        expenseAmountEditText = findViewById(R.id.editTextNumber)
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
            categorySpinner.selectedItem?.toString() ?: ""

            if (name.isEmpty() || amountText.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amount = amountText.toDoubleOrNull()
            if (amount == null) {
                Toast.makeText(this, "Enter a valid number for the amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
//database work  past this point, any issues? lmk

        }
    }
}