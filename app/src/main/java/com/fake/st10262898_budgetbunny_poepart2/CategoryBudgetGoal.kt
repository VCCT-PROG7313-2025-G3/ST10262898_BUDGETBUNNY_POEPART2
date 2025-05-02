package com.fake.st10262898_budgetbunny_poepart2

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.EditText
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import com.fake.st10262898_budgetbunny_poepart2.viewmodel.BudgetViewModel
import java.text.DecimalFormat
import java.text.ParseException
import java.util.Calendar

class CategoryBudgetGoal : AppCompatActivity() {

    private val budgetViewModel: BudgetViewModel by viewModels()
    private var selectedDate: Long = 0L


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_budget_goal)

        val categoryName = intent.getStringExtra("CATEGORY_NAME") ?: "Custom"
        val totalGoal = intent.getDoubleExtra("TOTAL_BUDGET_GOAL", 0.0)
        val username = intent.getStringExtra("USERNAME") ?: ""
        val minGoal = intent.getDoubleExtra("MIN_GOAL", 0.0)

        val questionText = findViewById<TextView>(R.id.budgetGoalsQ1)
        questionText.text = getString(R.string.budget_goal_question, categoryName)

        val amountEditText = findViewById<EditText>(R.id.amountEditText)

        val dateTextView = findViewById<TextView>(R.id.dateTextView)
        val calendar = Calendar.getInstance()

        dateTextView.setOnClickListener {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val dateString = "${selectedDay}/${selectedMonth + 1}/${selectedYear}"
                dateTextView.text = dateString

                val sdf = java.text.SimpleDateFormat("d/M/yyyy")
                val date = sdf.parse(dateString)
                selectedDate = date?.time ?: 0L
            }, year, month, day)

            datePickerDialog.show()
        }

        findViewById<Button>(R.id.next_button).setOnClickListener {
            val amountText = amountEditText.text.toString().trim()

            if (amountText.isEmpty()) {
                Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amount = parseAmount(amountText) ?: run {
                Toast.makeText(this, "Invalid amount format", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (amount <= 0) {
                Toast.makeText(this, "Amount must be positive", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (username.isNotBlank() && selectedDate != 0L) {
                budgetViewModel.addBudget(
                    totalBudgetGoal = totalGoal,
                    budgetCategory = categoryName,
                    budgetAmount = amount,
                    username = username,
                    minTotalBudgetGoal = minGoal,
                    budgetDate = selectedDate,
                    budgetIncome = 0.0
                )
            } else {
                Toast.makeText(this, "Please select a valid date.", Toast.LENGTH_SHORT).show()
            }
        }

        budgetViewModel.budgetSaved.observe(this) { saved ->
            if (saved) {
                Toast.makeText(this, "Budget saved successfully!", Toast.LENGTH_SHORT).show()
                finish() // Go back to bugetGoalsPage to allow selecting another category
            } else {
                Toast.makeText(this, "Failed to save budget. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }

    }

    // Helper function to parse the amount
    private fun parseAmount(amountText: String): Double? {
        val df = DecimalFormat("#,###.##")
        return try {
            df.parse(amountText)?.toDouble()
        } catch (e: ParseException) {
            null
        }
    }
}