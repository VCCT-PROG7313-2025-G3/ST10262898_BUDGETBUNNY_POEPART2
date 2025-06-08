package com.fake.st10262898_budgetbunny_poepart2

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.fake.st10262898_budgetbunny_poepart2.data.BudgetFirestore
import com.fake.st10262898_budgetbunny_poepart2.viewmodel.BudgetViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.activity.viewModels
import java.util.Date

class GoalEntry : AppCompatActivity() {

    private val budgetViewModel: BudgetViewModel by viewModels()
    private lateinit var dateButton: Button
    private lateinit var amountEditText: EditText
    private lateinit var incomeEditText: EditText
    private lateinit var categorySpinner: Spinner
    private val calendar = Calendar.getInstance()
    private var selectedDateInMillis: Long = System.currentTimeMillis()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goal_entry)

        // Initialize views
        dateButton = findViewById(R.id.dateButton)
        amountEditText = findViewById(R.id.editTextNumber)
        incomeEditText = findViewById(R.id.editTextNumber2)
        categorySpinner = findViewById(R.id.mySpinner)
        val nextButton = findViewById<Button>(R.id.button2)


        //Set initial date:
        updateDateButtonText()


        // Setup category spinner
        val categories = arrayOf(
            "Holiday", "House", "Tuition",
            "Investments", "Baby", "Custom"
        )
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categories
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        // Date picker setup
        val datePickerListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, day)
            updateDateButtonText()
        }

        dateButton.setOnClickListener {
            DatePickerDialog(
                this,
                datePickerListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Next button click handler
        nextButton.setOnClickListener {
            saveBudgetGoal()
        }
    }

    /*private fun updateDateInView() {
        val dateFormat = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(dateFormat, Locale.getDefault())
        dateEditText.setText(sdf.format(calendar.time))
    }*/

    private fun updateDateButtonText() {
        val dateFormat = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(dateFormat, Locale.getDefault())
        dateButton.text = "Selected Date: ${sdf.format(Date(selectedDateInMillis))}"
    }

    private fun saveBudgetGoal() {
        val category = categorySpinner.selectedItem.toString()
        val amountText = amountEditText.text.toString()
        val incomeText = incomeEditText.text.toString()

        // Removed dateText validation since we're using the calendar picker
        if (category.isEmpty() || amountText.isEmpty() || incomeText.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountText.toDoubleOrNull()
        val income = incomeText.toDoubleOrNull()

        if (amount == null || income == null) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show()
            return
        }

        if (amount <= 0 || income <= 0) {
            Toast.makeText(this, "Amounts must be positive", Toast.LENGTH_SHORT).show()
            return
        }

        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "") ?: ""

        if (username.isEmpty()) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                budgetViewModel.addBudget(
                    totalBudgetGoal = amount,
                    budgetCategory = category,
                    budgetAmount = 0.0, // Initial amount saved is 0
                    username = username,
                    minTotalBudgetGoal = income,
                    budgetDate = selectedDateInMillis, // Using the stored date
                    budgetIncome = income
                )

                runOnUiThread {
                    Toast.makeText(
                        this@GoalEntry,
                        "Budget goal saved successfully!",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this@GoalEntry,
                        "Failed to save budget: ${e.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}