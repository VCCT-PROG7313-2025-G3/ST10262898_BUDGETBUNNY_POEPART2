package com.fake.st10262898_budgetbunny_poepart2

import android.os.Bundle
import android.widget.Button
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fake.st10262898_budgetbunny_poepart2.Adapters.CategoryTotalAdapter
import com.fake.st10262898_budgetbunny_poepart2.data.BudgetBunnyDatabase
import com.fake.st10262898_budgetbunny_poepart2.data.CategoryTotal
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class ViewBudgetByDate : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_view_budget_by_date)

        val startDatePicker: DatePicker = findViewById(R.id.datePickerStart)
        val endDatePicker: DatePicker = findViewById(R.id.datePickerEnd)
        val submitButton: Button = findViewById(R.id.btn_submit_dates)
        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewTransactions)

        submitButton.setOnClickListener {
            val startDate =
                "${startDatePicker.year}-${startDatePicker.month + 1}-${startDatePicker.dayOfMonth}"
            val endDate =
                "${endDatePicker.year}-${endDatePicker.month + 1}-${endDatePicker.dayOfMonth}"

            val startDateMillis = convertDateToMillis(startDate)
            val endDateMillis = convertDateToMillis(endDate)

            lifecycleScope.launch {
                val db = BudgetBunnyDatabase.getDatabase(this@ViewBudgetByDate)
                val username = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("USERNAME", "")
                val categoryTotals = db.budgetDao().getCategoryTotalsForDateRange(
                    username ?: "", startDateMillis, endDateMillis
                )

                // Update RecyclerView with the fetched data
                updateRecyclerView(categoryTotals)
            }

            // For now, just show a Toast
            Toast.makeText(this, "From $startDate to $endDate", Toast.LENGTH_LONG).show()
        }
    }

    private fun convertDateToMillis(dateString: String): Long {
        // Convert date string to a long (milliseconds since epoch)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = dateFormat.parse(dateString)
        return date?.time ?: 0L
    }

    private fun updateRecyclerView(categoryTotals: List<CategoryTotal>) {
        val adapter = CategoryTotalAdapter(categoryTotals)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }
}