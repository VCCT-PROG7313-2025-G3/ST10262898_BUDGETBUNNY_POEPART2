package com.fake.st10262898_budgetbunny_poepart2

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class GoalEntry : AppCompatActivity() {

    private lateinit var spinner: Spinner
    private lateinit var editTextDate: EditText
    private lateinit var editTextAmount: EditText
    private lateinit var editTextIncome: EditText
    private lateinit var buttonNext: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goal_entry)

        // Initialize views
        spinner = findViewById(R.id.mySpinner)
        editTextDate = findViewById(R.id.editTextDate)
        editTextAmount = findViewById(R.id.editTextNumber)
        editTextIncome = findViewById(R.id.editTextNumber2)
        buttonNext = findViewById(R.id.button2)

        // Setup Spinner
        val categories = arrayOf("Select Category", "Food", "Transport", "Entertainment", "Bills","Rent","Car","Hobbies","Emergencies","Custom")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // Spinner selection listener
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (position == 0) {
                    // Handle "Select Category" case
                    Toast.makeText(this@GoalEntry, "Please select a valid category", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }

        // Button click listener
        buttonNext.setOnClickListener {
            val category = spinner.selectedItem.toString()
            val date = editTextDate.text.toString()
            val amount = editTextAmount.text.toString()
            val income = editTextIncome.text.toString()

            // Validate inputs
            if (category == "Select Category") {
                Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (date.isEmpty()) {
                editTextDate.error = "Date is required"
                return@setOnClickListener
            }
            if (amount.isEmpty()) {
                editTextAmount.error = "Amount is required"
                return@setOnClickListener
            }
            if (income.isEmpty()) {
                editTextIncome.error = "Income is required"
                return@setOnClickListener
            }

            // Process the data
            saveBudgetData(category, date, amount.toDoubleOrNull() ?: 0.0, income.toDoubleOrNull() ?: 0.0)

            Toast.makeText(this, "Budget saved successfully", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveBudgetData(category: String, date: String, amount: Double, income: Double) {
        // replace with database probably
        val sharedPreferences = getSharedPreferences("BudgetPrefs", MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("category", category)
            putString("date", date)
            putFloat("amount", amount.toFloat())
            putFloat("income", income.toFloat())
            apply()
        }
    }
}