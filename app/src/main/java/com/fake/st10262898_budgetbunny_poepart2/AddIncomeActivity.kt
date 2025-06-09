package com.fake.st10262898_budgetbunny_poepart2

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.fake.st10262898_budgetbunny_poepart2.viewmodel.BudgetViewModel
import java.text.NumberFormat
import java.util.Locale

class AddIncomeActivity : AppCompatActivity() {
    private val budgetViewModel: BudgetViewModel by viewModels() //Now can use the methods in the budgetViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_income)

        //Gets the budget Id in which user is about to add income into
        val budgetId = intent.getStringExtra("BUDGET_ID")?.takeIf { it.isNotBlank() } ?: run {
            Toast.makeText(this, "Invalid budget reference", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        //Confirmation for user whether it was a success or not
        budgetViewModel.updateStatus.observe(this) { (success, error) ->
            if (success) {
                Toast.makeText(this, "Update successful!", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            } else {
                Toast.makeText(this, error ?: "Update failed", Toast.LENGTH_LONG).show()
            }
        }

        //When user clicks btn_save there is error handeling to ensure that value entered is positive and then rewrite the income
        findViewById<Button>(R.id.btn_save).setOnClickListener {
            val amount = findViewById<EditText>(R.id.et_amount).text.toString().toDoubleOrNull()
            when {
                amount == null -> Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show()
                amount <= 0 -> Toast.makeText(this, "Amount must be positive", Toast.LENGTH_SHORT).show()
                else -> budgetViewModel.updateBudgetIncome(budgetId, amount)
            }
        }
    }
}