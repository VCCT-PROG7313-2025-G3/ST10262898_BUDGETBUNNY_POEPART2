package com.fake.st10262898_budgetbunny_poepart2

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.fake.st10262898_budgetbunny_poepart2.data.BudgetBunnyDatabase
import com.fake.st10262898_budgetbunny_poepart2.data.Expense
import com.fake.st10262898_budgetbunny_poepart2.data.ExpenseDao
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MonthlyPayment : AppCompatActivity() {

    private lateinit var expenseDao: ExpenseDao
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_monthly_payment)

        expenseDao = BudgetBunnyDatabase.getDatabase(this).expenseDao()
        val expenseCategory = intent.getStringExtra("EXPENSE_CATEGORY") ?: "Unknown Category"
        val categoryTextView = findViewById<TextView>(R.id.categoryTextView)
        categoryTextView.text = "Expense Category: $expenseCategory"

        val expenseNameEditText = findViewById<EditText>(R.id.enterExpenseName)
        val amountEditText = findViewById<EditText>(R.id.enterAmount)
        val dateEditText = findViewById<EditText>(R.id.enterDate)

        // Set up the DatePickerDialog
        val datePickerListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, day)

            val dateFormat = "dd/MM/yyyy"
            val sdf = SimpleDateFormat(dateFormat, Locale.getDefault())
            dateEditText.setText(sdf.format(calendar.time))
        }

        // Configure date EditText for single-click response
        dateEditText.apply {
            isFocusable = false
            isClickable = true
            setOnClickListener {
                DatePickerDialog(
                    this@MonthlyPayment,
                    datePickerListener,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        }

        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "") ?: ""

        val saveButton = findViewById<Button>(R.id.button)
        saveButton.setOnClickListener {
            val expenseName = expenseNameEditText.text.toString()
            val amountString = amountEditText.text.toString()
            val dateString = dateEditText.text.toString()

            if (expenseName.isNotEmpty() && amountString.isNotEmpty() && dateString.isNotEmpty()) {
                val amount = amountString.toDoubleOrNull()
                if (amount != null) {
                    val expenseDate = calendar.timeInMillis
                    saveExpenseToDatabase(expenseCategory, expenseName, amount, username, expenseDate)
                } else {
                    Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveExpenseToDatabase(expenseCategory: String, expenseName: String, amount: Double, username: String, expenseDate: Long) {
        val expense = Expense(
            expenseCategory = expenseCategory,
            expenseName = expenseName,
            expenseAmount = amount,
            username = username,
            expenseDate = expenseDate,
            expenseImage = null
        )

        lifecycleScope.launch {
            try {
                expenseDao.insertExpense(expense)
                Toast.makeText(this@MonthlyPayment, "Expense saved", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@MonthlyPayment, "Failed to save expense", Toast.LENGTH_SHORT).show()
            }
        }
    }
}