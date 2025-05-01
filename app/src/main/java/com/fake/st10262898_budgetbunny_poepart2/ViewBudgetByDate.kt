package com.fake.st10262898_budgetbunny_poepart2

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.DatePicker
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.fake.st10262898_budgetbunny_poepart2.Adapters.CategoryTotalAdapter
import com.fake.st10262898_budgetbunny_poepart2.data.Budget
import com.fake.st10262898_budgetbunny_poepart2.data.BudgetBunnyDatabase
import com.fake.st10262898_budgetbunny_poepart2.data.CategoryTotal
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class ViewBudgetByDate : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CategoryTotalAdapter
    private lateinit var startDatePicker: DatePicker
    private lateinit var endDatePicker: DatePicker
    private lateinit var submitButton: Button
    private lateinit var headingTextView: TextView

    private val TAG = "ViewBudgetByDate"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_budget_by_date)

        // Initialize views
        startDatePicker = findViewById(R.id.datePickerStart)
        endDatePicker = findViewById(R.id.datePickerEnd)
        submitButton = findViewById(R.id.btn_submit_dates)
        recyclerView = findViewById(R.id.recyclerView)
        headingTextView = findViewById(R.id.tvHeading)

        // Setup RecyclerView
        adapter = CategoryTotalAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        submitButton.setOnClickListener {
            Log.d(TAG, "Checking SharedPreferences for username")

            val username = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("username", null)

            if (username == null) {
                Log.d(TAG, "Username is null")
                Toast.makeText(this, "Username not found!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
                Log.d(TAG, "Username retrieved: $username")
            }

            val startDate = Calendar.getInstance().apply {
                set(startDatePicker.year, startDatePicker.month, startDatePicker.dayOfMonth)
            }
            val endDate = Calendar.getInstance().apply {
                set(endDatePicker.year, endDatePicker.month, endDatePicker.dayOfMonth)
            }

            val startDateMillis = startDate.timeInMillis
            val endDateMillis = endDate.timeInMillis

            // Debug: Print start and end dates
            Log.d(TAG, "StartMillis: $startDateMillis (${SimpleDateFormat("yyyy-MM-dd").format(Date(startDateMillis))})")
            Log.d(TAG, "EndMillis: $endDateMillis (${SimpleDateFormat("yyyy-MM-dd").format(Date(endDateMillis))})")

            val db = Room.databaseBuilder(
                applicationContext,
                BudgetBunnyDatabase::class.java, "budget_database"
            ).build()

            Log.d(TAG, "Valid username found. Fetching category totals for date range...")

            lifecycleScope.launch {
                // ✅ Step 1: Log all budget entries for this user
                val allBudgets = db.budgetDao().getAllBudgetsForUser(username)
                Log.d(TAG, "----- All Budget Entries for $username -----")
                for (budget in allBudgets) {
                    val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(budget.budgetDate))
                    Log.d(TAG, "Category: ${budget.budgetCategory}, Amount: ${budget.budgetAmount}, Date: $formattedDate")
                }
                Log.d(TAG, "----- End of Budget Entries -----")

                // ✅ Step 2: Run actual query
                val categoryTotals: List<CategoryTotal> =
                    db.budgetDao().getCategoryTotalsForDateRange(
                        username,
                        startDateMillis,
                        endDateMillis
                    )

                Log.d(TAG, "Query result: ${categoryTotals.size} categories returned")
                for (categoryTotal in categoryTotals) {
                    Log.d(TAG, "Category: ${categoryTotal.budgetCategory}, Total: ${categoryTotal.total}")
                }

                runOnUiThread {
                    if (categoryTotals.isNotEmpty()) {
                        headingTextView.text = "Totals by Category"
                        headingTextView.visibility = TextView.VISIBLE
                    } else {
                        headingTextView.text = "No data found in this range."
                        headingTextView.visibility = TextView.VISIBLE
                    }

                    updateRecyclerView(categoryTotals)
                }
            }
        }
    }

    private fun updateRecyclerView(categoryTotals: List<CategoryTotal>) {
        adapter = CategoryTotalAdapter(categoryTotals)
        recyclerView.adapter = adapter
        adapter.notifyDataSetChanged()
    }
}