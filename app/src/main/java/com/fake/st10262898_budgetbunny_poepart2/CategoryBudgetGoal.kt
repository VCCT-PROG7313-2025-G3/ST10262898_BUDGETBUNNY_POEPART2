package com.fake.st10262898_budgetbunny_poepart2

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

class CategoryBudgetGoal : AppCompatActivity() {

    private val budgetViewModel: BudgetViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_budget_goal)

        val categoryName = intent.getStringExtra("CATEGORY_NAME") ?: "Custom"
        val totalGoal = intent.getDoubleExtra("TOTAL_BUDGET_GOAL", 0.0)
        val username = intent.getStringExtra("USERNAME") ?: ""

        val questionText = findViewById<TextView>(R.id.budgetGoalsQ1)
        questionText.text = getString(R.string.budget_goal_question, categoryName)

        val amountEditText = findViewById<EditText>(R.id.amountEditText)

        findViewById<Button>(R.id.next_button).setOnClickListener {
            val amountText = amountEditText.text.toString().trim()

            // Log the raw input text for debugging purposes
            Log.d("CategoryBudgetGoal", "Raw Amount Input: $amountText")

            // Try to parse the amount using DecimalFormat to handle decimal points correctly
            val amount = parseAmount(amountText)

            Log.d("CategoryBudgetGoal", "Parsed Amount: $amount")

            // Ensure the amount is not null and username is provided
            if (amount != null && username.isNotBlank()) {
                Log.d("CategoryBudgetGoal", "Proceeding to save budget with Amount: $amount, Category: $categoryName, Total Goal: $totalGoal")
                budgetViewModel.addBudget(
                    totalBudgetGoal = totalGoal,
                    budgetCategory = categoryName,
                    budgetAmount = amount,
                    username = username
                )
            }
        }

        budgetViewModel.budgetSaved.observe(this) { saved ->
            if (saved) {
                Toast.makeText(this, "Budget saved successfully!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, BunnyNameActivity::class.java)
                startActivity(intent)
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