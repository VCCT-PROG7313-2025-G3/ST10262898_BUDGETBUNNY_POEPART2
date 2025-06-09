package com.fake.st10262898_budgetbunny_poepart2

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fake.st10262898_budgetbunny_poepart2.data.CategoryTotalFirestore
import com.fake.st10262898_budgetbunny_poepart2.viewmodel.BudgetViewModel
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.activity.viewModels

class ViewBudgetByDate : AppCompatActivity() {

    private val TAG = "ViewBudgetByDate"
    private val budgetViewModel: BudgetViewModel by viewModels()

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CategoryTotalAdapter
    private lateinit var startDatePicker: DatePicker
    private lateinit var endDatePicker: DatePicker
    private lateinit var submitButton: Button
    private lateinit var headingTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_budget_by_date)

        // Initialize views
        startDatePicker = findViewById(R.id.datePickerStart)
        endDatePicker = findViewById(R.id.datePickerEnd)
        submitButton = findViewById(R.id.btn_submit_dates)
        recyclerView = findViewById(R.id.recyclerView)
        headingTextView = findViewById(R.id.tvHeading)

        // Set default dates (which is current month)
        val calendar = Calendar.getInstance()
        startDatePicker.init(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            1,
            null
        )


        adapter = CategoryTotalAdapter(emptyList()) { categoryTotal ->

            Toast.makeText(
                this,
                "${categoryTotal.budgetCategory}: R${"%.2f".format(categoryTotal.total)}",
                Toast.LENGTH_SHORT
            ).show()
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        submitButton.setOnClickListener {
            val username = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                .getString("username", "") ?: ""

            if (username.isEmpty()) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            val startDate = Calendar.getInstance().apply {
                set(startDatePicker.year, startDatePicker.month, startDatePicker.dayOfMonth)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val endDate = Calendar.getInstance().apply {
                set(endDatePicker.year, endDatePicker.month, endDatePicker.dayOfMonth)
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis

            Log.d(TAG, "Fetching budget data from ${Date(startDate)} to ${Date(endDate)}")

            budgetViewModel.getCategoryTotalsByDateRange(
                username,
                startDate,
                endDate
            ) { categoryTotals ->
                runOnUiThread {
                    if (categoryTotals.isNotEmpty()) {
                        headingTextView.text = buildHeading(Date(startDate), Date(endDate))
                        adapter.updateData(categoryTotals)
                        headingTextView.visibility = View.VISIBLE
                        recyclerView.visibility = View.VISIBLE
                    } else {
                        headingTextView.text = "No budget data found for selected period"
                        adapter.updateData(emptyList())
                        headingTextView.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                        Toast.makeText(
                            this,
                            "No budget categories found for this date range",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    private fun formatDate(date: Date): String {
        return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date)
    }

    private fun buildHeading(startDate: Date, endDate: Date): String {
        return "Budget Totals\n${formatDate(startDate)} - ${formatDate(endDate)}"
    }

    // This is function to convert dp to px
    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
}