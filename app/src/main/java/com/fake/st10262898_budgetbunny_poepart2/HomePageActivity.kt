package com.fake.st10262898_budgetbunny_poepart2

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fake.st10262898_budgetbunny_poepart2.data.BudgetBunnyDatabase
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

//Added:

class HomePageActivity : AppCompatActivity() {

    private lateinit var expenseAdapter: ExpenseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home_page)

        //Pie Chart code:

        //Initialise the Pie chart and the bar graph:
        val pieChart : PieChart = findViewById(R.id.pieChart)


        // Retrieve the username from SharedPreferences
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val currentUserId = sharedPreferences.getString("username", "") ?: ""



        //This is the section where the  data is prepeared for the pie chart:
        val pieEntries = ArrayList<PieEntry>()

        val db = BudgetBunnyDatabase.getDatabase(this)
        val expenseDao = db.expenseDao()

        lifecycleScope.launch {
            val expenses = expenseDao.getExpenseForUser(currentUserId)

            // Group by category and calculate totals
            val categoryTotals = expenses.groupBy { it.expenseCategory }
                .mapValues { entry ->
                    entry.value.sumOf { it.expenseAmount }
                }
                .toList()
                .sortedByDescending { it.second }

            // Take top 4 categories
            val topCategories = categoryTotals.take(4)

            for ((category, total) in topCategories) {
                pieEntries.add(PieEntry(total.toFloat(), category))
            }

            // Set Pie chart data dynamically after DB call
            val pieDataSet = PieDataSet(pieEntries, "")
            pieDataSet.setColors(
                Color.rgb(0, 100, 0),
                Color.rgb(139, 0, 0),
                Color.rgb(128, 0, 128),
                Color.rgb(255, 165, 0)
            )
            pieDataSet.sliceSpace = 5f
            pieDataSet.valueTextColor = Color.BLACK
            pieDataSet.valueTextSize = 14f
            pieDataSet.valueFormatter = NoDecimalPercentFormatter()

            val pieData = PieData(pieDataSet)
            pieChart.data = pieData
            pieChart.invalidate() // Refresh
        }


        //Create a dataset for the Pie chart:
        val pieDataSet = PieDataSet(pieEntries, "")
        pieDataSet.setColors(
            Color.rgb(0,100,0), //This makes budget Goals dark green
            Color.rgb(139,0,0), //This makes Expenses dark red
            Color.rgb(128,0,128) //This makes Savings dark purple
        )

        pieDataSet.sliceSpace = 2f
        pieDataSet.valueTextColor = Color.BLACK
        pieDataSet.valueTextSize = 14f
        pieDataSet.valueFormatter = NoDecimalPercentFormatter() //referring to the class that makes whole numbers



        //Create Pie data object:
        val pieData = PieData(pieDataSet)
        pieChart.data = pieData



        pieChart.description.isEnabled = false  // No description (No space for description)
        pieChart.setDrawEntryLabels(false)
        pieChart.setDrawCenterText(false)
        pieChart.isDrawHoleEnabled = true
        pieChart.setHoleColor(Color.TRANSPARENT)
        pieChart.setBackgroundColor(Color.TRANSPARENT)
        pieChart.setUsePercentValues(true)
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.animateY(1000)

        // The legend of the pie chart
        val legend = pieChart.legend
        legend.isEnabled = true
        legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        legend.orientation = Legend.LegendOrientation.HORIZONTAL
        legend.setDrawInside(false)
        legend.textColor = Color.WHITE
        legend.textSize = 8f //
        legend.form = Legend.LegendForm.CIRCLE
        legend.formSize = 8f //
        legend.yOffset = 4f //
        legend.xEntrySpace = 8f //
        legend.yEntrySpace = 0f //


        // Refresh
        pieChart.invalidate()



        //Bar Chart Work:

        //This is where the barchart in the xml
        val barChart: com.github.mikephil.charting.charts.BarChart = findViewById(R.id.barChart)

        lifecycleScope.launch {
            val expenses = expenseDao.getExpenseForUser(currentUserId)

            val categoryTotals = expenses.groupBy { it.expenseCategory }
                .mapValues { entry -> entry.value.sumOf { it.expenseAmount } }
                .toList()
                .sortedByDescending { it.second }
                .take(5)

            val entries = ArrayList<com.github.mikephil.charting.data.BarEntry>()
            val labels = ArrayList<String>()

            categoryTotals.forEachIndexed { index, (category, total) ->
                if (category != null) {
                    entries.add(com.github.mikephil.charting.data.BarEntry(index.toFloat(), total.toFloat()))
                    labels.add(category)
                }
            }

            val barDataSet = com.github.mikephil.charting.data.BarDataSet(entries, "Top Categories")
            barDataSet.setColors(
                Color.rgb(0, 100, 0),
                Color.rgb(139, 0, 0),
                Color.rgb(255, 165, 0),
                Color.rgb(75, 0, 130),
                Color.rgb(0, 191, 255)
            )
            barDataSet.valueTextColor = Color.BLACK
            barDataSet.valueTextSize = 12f

            val data = com.github.mikephil.charting.data.BarData(barDataSet)
            barChart.data = data

            // X-axis labels
            val xAxis = barChart.xAxis
            xAxis.setDrawLabels(true)
            xAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return labels.getOrNull(value.toInt()) ?: ""
                }
            }
            xAxis.granularity = 1f
            xAxis.labelRotationAngle = -45f
            xAxis.textColor = Color.WHITE

            // Axis Styling
            barChart.axisLeft.textColor = Color.WHITE
            barChart.axisLeft.setDrawGridLines(false)
            barChart.axisRight.isEnabled = false
            barChart.description.isEnabled = false
            barChart.setFitBars(true)
            barChart.setBackgroundColor(Color.TRANSPARENT)
            barChart.animateY(1000)
            barChart.invalidate()
        }








        //Find the text view on the xml here:
        val tvDateTime = findViewById<TextView>(R.id.tv_dateTime)

        //Specifying the format in which the date and time will be in:
        val currentDateTime = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault()).format(Date())
        tvDateTime.text = currentDateTime



        val recyclerView = findViewById<RecyclerView>(R.id.rv_transactions)
        recyclerView.layoutManager = LinearLayoutManager(this)



        lifecycleScope.launch {
            val expenses = expenseDao.getExpenseForUser(currentUserId)
            expenseAdapter = ExpenseAdapter(expenses) { expense ->
                // Handle the item click here, for example:
                val intent = Intent(this@HomePageActivity, EditTransactionsActivity::class.java)
                intent.putExtra("expenseId", expense.id)
                startActivity(intent)

            }
            recyclerView.adapter = expenseAdapter
        }











        //For Navigation bar functionality:
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Already on Home, do nothing
                    true
                }
                R.id.nav_transactions -> {
                    val intent = Intent(this, TransactionsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_budgetGoal -> {
                    val intent = Intent(this, BudgetGoalsOverviewActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_settings -> {
                    val intent = Intent(this, Settings::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }


    }

    //This class allows there to not be a decimal on the pie chart and its just whole numbers.
    class NoDecimalPercentFormatter : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return "${value.toInt()}%"
        }
    }
}