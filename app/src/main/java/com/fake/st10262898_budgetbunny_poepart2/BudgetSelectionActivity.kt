package com.fake.st10262898_budgetbunny_poepart2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fake.st10262898_budgetbunny_poepart2.viewmodel.BudgetViewModel

class BudgetSelectionActivity : AppCompatActivity() {
    private val budgetViewModel: BudgetViewModel by viewModels()
    private lateinit var adapter: BudgetAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget_selection)

        // Initialize RecyclerView and adapter
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = BudgetAdapter(emptyList()) { selectedBudget ->
            if (selectedBudget.id.isNotBlank()) {
                startActivity(Intent(this, AddIncomeActivity::class.java).apply {
                    putExtra("BUDGET_ID", selectedBudget.id)
                    putExtra("BUDGET_NAME", selectedBudget.budgetCategory)
                })
            } else {
                Toast.makeText(this, "Invalid budget selected", Toast.LENGTH_SHORT).show()
            }
        }
        recyclerView.adapter = adapter

        findViewById<Button>(R.id.btn_back).setOnClickListener { finish() }

        loadUserBudgets()
    }

    //This is what happens when you resume the application
    override fun onResume() {
        super.onResume()
        loadUserBudgets()
    }

    //This allows the user to select a budget from what he has entered before
    private fun loadUserBudgets() {
        val username = getSharedPreferences("UserPrefs", MODE_PRIVATE)
            .getString("username", "") ?: return

        budgetViewModel.budgets.observe(this) { budgets ->
            val userBudgets = budgets.filter { it.username == username }
            Log.d("BudgetDebug", "Loaded ${userBudgets.size} budgets")
            adapter.updateData(userBudgets)
        }

        budgetViewModel.loadBudgets(username)
    }
}
