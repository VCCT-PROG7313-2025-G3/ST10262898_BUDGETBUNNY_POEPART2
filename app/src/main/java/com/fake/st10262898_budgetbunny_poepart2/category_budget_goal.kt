package com.fake.st10262898_budgetbunny_poepart2

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.EditText
import android.content.Intent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class BudgetGoalActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_budget_goal)

        val categoryName = intent.getStringExtra("CATEGORY_NAME") ?: "Custom"
        findViewById<TextView>(R.id.budgetGoalsQ1).text = "What is your $categoryName budget goal?"

        findViewById<Button>(R.id.next_button).setOnClickListener {
            val amount = findViewById<EditText>(R.id.amountEditText).text.toString()
            // Validate amount here if needed

            val resultIntent = Intent().apply {
                putExtra("CATEGORY_NAME", categoryName)
                putExtra("BUDGET_AMOUNT", amount)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }
}