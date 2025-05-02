package com.fake.st10262898_budgetbunny_poepart2

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fake.st10262898_budgetbunny_poepart2.data.BudgetBunnyDatabase
import com.fake.st10262898_budgetbunny_poepart2.data.Expense
import kotlinx.coroutines.launch
import com.google.android.material.bottomnavigation.BottomNavigationView

class TransactionsActivity : AppCompatActivity() {

    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
        private const val CAMERA_REQUEST_CODE = 101
    }

    private lateinit var expenseAdapter: ExpenseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_transactions)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.rv_transaction_list)
        recyclerView.layoutManager = LinearLayoutManager(this)
        expenseAdapter = ExpenseAdapter(emptyList()) { selectedExpense ->
            val intent = Intent(this, EditTransactionsActivity::class.java)
            intent.putExtra("expenseId", selectedExpense.id)
            startActivity(intent)
        }
        recyclerView.adapter = expenseAdapter

        // Load data
        loadExpenses()

        // Setup bottom navigation
        setupBottomNavigation()

        // Setup month tiles
        setupMonthTiles()
    }

    private fun loadExpenses() {
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val currentUserId = sharedPreferences.getString("username", "") ?: ""
        val db = BudgetBunnyDatabase.getDatabase(this)
        val expenseDao = db.expenseDao()

        lifecycleScope.launch {
            val expenses = expenseDao.getExpenseForUser(currentUserId)
            expenseAdapter.updateExpenses(expenses)
            updateCategoryTiles(expenses)
        }
    }

    private fun updateCategoryTiles(expenses: List<Expense>) {
        val categoryTotals = expenses.groupBy { it.expenseCategory }
            .mapValues { entry -> entry.value.sumOf { it.expenseAmount } }
            .toList()
            .sortedByDescending { it.second }
            .take(4)

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

        for (i in 0..3) {
            if (i < categoryTotals.size) {
                val (category, total) = categoryTotals[i]
                nameViews[i].text = category
                amountViews[i].text = "Total: R%.2f".format(total)
            } else {
                nameViews[i].text = "Add category"
                amountViews[i].text = "Total: R0"
            }
        }
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this@TransactionsActivity, HomePageActivity::class.java))
                    true
                }
                R.id.nav_transactions -> true
                R.id.nav_budgetGoal -> {
                    startActivity(Intent(this@TransactionsActivity, BudgetGoalsOverviewActivity::class.java))
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this@TransactionsActivity, Settings::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun setupMonthTiles() {
        val monthTileIds = listOf(
            R.id.tileJanuary, R.id.tileFebruary, R.id.tileMarch,
            R.id.tileApril, R.id.tileMay, R.id.tileJune,
            R.id.tileJuly, R.id.tileAugust, R.id.tileSeptember,
            R.id.tileOctober, R.id.tileNovember, R.id.tileDecember
        )

        monthTileIds.forEachIndexed { index, tileId ->
            findViewById<LinearLayout>(tileId).setOnClickListener {
                val intent = Intent(this, ViewMonthsExpense::class.java)
                intent.putExtra("month", index + 1)
                startActivity(intent)
            }
        }
    }

    // Camera permission functions (keep these if you need them)
    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_CODE
        )
    }
}