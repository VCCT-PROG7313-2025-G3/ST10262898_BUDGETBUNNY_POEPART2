package com.fake.st10262898_budgetbunny_poepart2

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fake.st10262898_budgetbunny_poepart2.data.BudgetBunnyDatabase
import com.fake.st10262898_budgetbunny_poepart2.data.CategoryTotal
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date


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



        // Add this extension function:
        fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()

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

            val db = BudgetBunnyDatabase.getDatabase(applicationContext)

            Log.d(TAG, "Valid username found. Fetching category totals for date range...")

            lifecycleScope.launch {
                try {
                    // First test with the simple query (no date range)
                    val categoryTotals = db.budgetDao().getCategoryTotals(username)

                    Log.d(TAG, "Simple query result: ${categoryTotals.size} categories")
                    for (categoryTotal in categoryTotals) {
                        Log.d(TAG, "Category: ${categoryTotal.budgetCategory}, Total: ${categoryTotal.total}")
                    }

                    runOnUiThread {
                        if (categoryTotals.isNotEmpty()) {
                            headingTextView.text = "All Category Totals"
                            updateRecyclerView(categoryTotals)
                        } else {
                            headingTextView.text = "No budget data found for user"
                        }
                        headingTextView.visibility = View.VISIBLE
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in simple query", e)
                    runOnUiThread {
                        Toast.makeText(this@ViewBudgetByDate, "Error loading data", Toast.LENGTH_SHORT).show()
                    }
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