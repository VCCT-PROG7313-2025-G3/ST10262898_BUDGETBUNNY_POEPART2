package com.fake.st10262898_budgetbunny_poepart2

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
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

class MonthlyPayment : AppCompatActivity() {

    private lateinit var expenseDao: ExpenseDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_monthly_payment)

        // Initialize your DAO here (assuming you've set it up correctly)
        expenseDao = BudgetBunnyDatabase.getDatabase(this).expenseDao()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Retrieve the expense name passed from the previous activity
        val expenseName = intent.getStringExtra("EXPENSE_NAME") ?: "Unknown Expense"

        // Get the EditText where the user will input the amount
        val amountEditText = findViewById<EditText>(R.id.enterAnAmount)

        // Define the username
        val username = "User1"  // This should come from the logged-in session or similar.

        // Setup the button that saves the data
        val saveButton = findViewById<Button>(R.id.button)
        saveButton.setOnClickListener {
            val amountString = amountEditText.text.toString()

            // Validate if the amount is not empty
            if (amountString.isNotEmpty()) {
                val amount = amountString.toDoubleOrNull()
                if (amount != null) {
                    // Now save the expense (name and amount) to the database
                    saveExpenseToDatabase(expenseName, amount, username)
                } else {
                    Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show()
            }
        }

        // Padding handling
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun saveExpenseToDatabase(expenseName: String, amount: Double, username: String) {
        // Create an Expense object using the correct parameter names
        val expense = Expense(expenseName = expenseName, expenseAmount = amount, username = username)

        // Use lifecycleScope to launch a coroutine for DB operations
        lifecycleScope.launch {
            try {
                // Insert the expense into the database
                expenseDao.insertExpense(expense) // This should be a suspend function
                // Show confirmation message
                Toast.makeText(this@MonthlyPayment, "Expense saved", Toast.LENGTH_SHORT).show()
                finish() // Close the activity after saving
            } catch (e: Exception) {
                // Handle any errors
                Toast.makeText(this@MonthlyPayment, "Failed to save expense", Toast.LENGTH_SHORT).show()
            }
        }
    }
}