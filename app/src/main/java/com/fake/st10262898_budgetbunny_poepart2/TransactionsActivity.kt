package com.fake.st10262898_budgetbunny_poepart2

import ExpenseAdapter
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fake.st10262898_budgetbunny_poepart2.data.BudgetBunnyDatabase
import kotlinx.coroutines.launch

class TransactionsActivity : AppCompatActivity() {


    private lateinit var expenseAdapter: ExpenseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_transactions)

        // Step 1: Setup RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.rv_transaction_list)
        recyclerView.layoutManager = LinearLayoutManager(this)
        expenseAdapter = ExpenseAdapter(emptyList())
        recyclerView.adapter = expenseAdapter

        // Step 2: Get current user from SharedPreferences
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val currentUserId = sharedPreferences.getString("username", "") ?: ""

        // Step 3: Fetch expenses from DB
        val db = BudgetBunnyDatabase.getDatabase(this)
        val expenseDao = db.expenseDao()

        lifecycleScope.launch {
            val expenses = expenseDao.getExpenseForUser(currentUserId)
            expenseAdapter.updateExpenses(expenses)
        }



    }
}