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
import com.fake.st10262898_budgetbunny_poepart2.data.Budget
import com.fake.st10262898_budgetbunny_poepart2.data.BudgetBunnyDatabase
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class GoalEntry : AppCompatActivity() {

    private lateinit var dateEditText: EditText
    private lateinit var amountEditText: EditText
    private lateinit var incomeEditText: EditText
    private lateinit var categorySpinner: Spinner
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goal_entry)

        // Initialize views
        dateEditText = findViewById(R.id.editTextDate)
        amountEditText = findViewById(R.id.editTextNumber)
        incomeEditText = findViewById(R.id.editTextNumber2)
        categorySpinner = findViewById(R.id.mySpinner)
        val nextButton = findViewById<Button>(R.id.button2)

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
            updateDateInView()
        }

        dateEditText.setOnClickListener {
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

    private fun updateDateInView() {
        val dateFormat = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(dateFormat, Locale.getDefault())
        dateEditText.setText(sdf.format(calendar.time))
    }

    private fun saveBudgetGoal() {
        val category = categorySpinner.selectedItem.toString()
        val amountText = amountEditText.text.toString()
        val incomeText = incomeEditText.text.toString()
        val dateText = dateEditText.text.toString()

        if (category.isEmpty() || amountText.isEmpty() || incomeText.isEmpty() || dateText.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountText.toDoubleOrNull()
        val income = incomeText.toDoubleOrNull()

        if (amount == null || income == null) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show()
            return
        }

        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "") ?: ""

        lifecycleScope.launch {
            val db = BudgetBunnyDatabase.getDatabase(this@GoalEntry)
            val budgetDao = db.budgetDao()

            val budget = Budget(
                totalBudgetGoal = amount,
                minTotalBudgetGoal = income,
                budgetCategory = category,
                budgetAmount = 0.0, // Initial amount saved is 0
                budgetDate = calendar.timeInMillis,
                budgetIncome = income,
                username = username
            )

            try {
                budgetDao.insertBudget(budget)
                Toast.makeText(this@GoalEntry, "Budget goal saved", Toast.LENGTH_SHORT).show()

                // Return to BudgetGoalsOverviewActivity which will refresh the list
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@GoalEntry, "Failed to save budget goal", Toast.LENGTH_SHORT).show()
            }
        }
    }
}