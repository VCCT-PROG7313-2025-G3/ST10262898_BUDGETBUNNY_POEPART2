package com.fake.st10262898_budgetbunny_poepart2

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fake.st10262898_budgetbunny_poepart2.data.ExpenseFirebase
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransactionsActivity : AppCompatActivity() {

    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
        private const val CAMERA_REQUEST_CODE = 101
    }

    private lateinit var expenseAdapter: ExpenseAdapter
    private val db = Firebase.firestore

    private val addExpenseLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Log.d("Transactions", "Expense added/updated, refreshing list")
            loadExpenses()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_transactions)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set current date/time
        val tvDateTime: TextView = findViewById(R.id.dateText)
        tvDateTime.text = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault()).format(Date())

        // Initialize RecyclerView with enhanced click handling
        initializeRecyclerView()

        // Setup button click listeners
        findViewById<Button>(R.id.btn_add_expense).setOnClickListener {
            addExpenseLauncher.launch(Intent(this, ExpenseEntry::class.java))
        }

        // Load data and setup UI
        loadExpenses()
        setupBottomNavigation()
        setupMonthTiles()
    }

    private fun initializeRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.rv_transaction_list)
        recyclerView.layoutManager = LinearLayoutManager(this)

        expenseAdapter = ExpenseAdapter(emptyList()) { selectedExpense ->
            if (selectedExpense.id.isNotBlank()) {
                Intent(this, EditTransactionsActivity::class.java).apply {
                    putExtra("expenseId", selectedExpense.id)
                    startActivity(this)
                }
            } else {
                Toast.makeText(
                    this,
                    "Cannot edit this expense - missing ID",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("Transactions", "Attempted to edit expense with empty ID")
            }
        }
        recyclerView.adapter = expenseAdapter
    }

    private fun loadExpenses() {
        val currentUserId = getSharedPreferences("UserPrefs", MODE_PRIVATE)
            .getString("username", "") ?: ""

        db.collection("expenses")
            .whereEqualTo("username", currentUserId)
            .get()
            .addOnSuccessListener { documents ->
                val expenses = documents.mapNotNull { doc ->
                    try {
                        // Use the correct fromDocument method
                        ExpenseFirebase(
                            expenseName = doc.getString("expenseName") ?: "",
                            expenseAmount = doc.getDouble("expenseAmount") ?: 0.0,
                            username = doc.getString("username") ?: "",
                            expenseCategory = doc.getString("expenseCategory"),
                            expenseDate = doc.getLong("expenseDate") ?: 0L,
                            expenseImageBase64 = doc.getString("expenseImageBase64")
                        ).apply {
                            id = doc.id
                        }
                    } catch (e: Exception) {
                        Log.e("LoadExpenses", "Error parsing expense ${doc.id}", e)
                        null
                    }
                }
                expenseAdapter.updateExpenses(expenses)
                updateCategoryTiles(expenses)
            }
            .addOnFailureListener { e ->
                Log.e("Transactions", "Error loading expenses", e)
                Toast.makeText(this, "Error loading expenses", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateCategoryTiles(expenses: List<ExpenseFirebase>) {
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
        bottomNavigationView.selectedItemId = R.id.nav_transactions

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this@TransactionsActivity, HomePageActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_transactions -> true
                R.id.nav_budgetGoal -> {
                    startActivity(Intent(this@TransactionsActivity, BudgetGoalsOverviewActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this@TransactionsActivity, Settings::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }
    }

    private fun setupMonthTiles() {
        listOf(
            R.id.tileJanuary, R.id.tileFebruary, R.id.tileMarch,
            R.id.tileApril, R.id.tileMay, R.id.tileJune,
            R.id.tileJuly, R.id.tileAugust, R.id.tileSeptember,
            R.id.tileOctober, R.id.tileNovember, R.id.tileDecember
        ).forEachIndexed { index, tileId ->
            findViewById<LinearLayout>(tileId).setOnClickListener {
                startActivity(Intent(this, ViewMonthsExpense::class.java).apply {
                    putExtra("month", index + 1)
                })
            }
        }
    }

    private fun navigateTo(activity: Class<*>) {
        startActivity(Intent(this, activity))
        overridePendingTransition(0, 0)
    }

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