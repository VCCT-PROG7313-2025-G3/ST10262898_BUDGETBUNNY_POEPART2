package com.fake.st10262898_budgetbunny_poepart2

import com.fake.st10262898_budgetbunny_poepart2.Adapters.ExpenseAdapter
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fake.st10262898_budgetbunny_poepart2.data.BudgetBunnyDatabase
import kotlinx.coroutines.launch
import android.widget.TextView

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

            // Step 1: Group by category and calculate total for each
            val categoryTotals = expenses.groupBy { it.expenseCategory }
                .mapValues { entry ->
                    entry.value.sumOf { it.expenseAmount }
                }
                .toList()
                .sortedByDescending { it.second }

            // Step 2: Prepare top 4 categories (or less)
            val topCategories = categoryTotals.take(4)

            // Step 3: Update the tile views
            val nameViews = listOf<TextView>(
                findViewById(R.id.nameOfExpense1),
                findViewById(R.id.nameOfExpense2),
                findViewById(R.id.nameOfExpense3),
                findViewById(R.id.nameOfExpense4)
            )
            val amountViews = listOf<TextView>(
                findViewById(R.id.expenseAmountOne),
                findViewById(R.id.expenseAmount2),
                findViewById(R.id.expenseAmounnt3),
                findViewById(R.id.expenseAmount4)
            )

            // Step 4: Populate tiles
            for (i in 0..3) {
                if (i < topCategories.size) {
                    val (category, total) = topCategories[i]
                    nameViews[i].text = category
                    amountViews[i].text = "Total: R%.2f".format(total)
                } else {
                    nameViews[i].text = "Add category"
                    amountViews[i].text = "Total: R0"
                }
            }
        }



    }
}