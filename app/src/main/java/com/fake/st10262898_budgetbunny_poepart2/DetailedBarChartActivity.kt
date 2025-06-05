package com.fake.st10262898_budgetbunny_poepart2

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.fake.st10262898_budgetbunny_poepart2.data.BudgetFirestore
import com.fake.st10262898_budgetbunny_poepart2.data.ExpenseFirebase
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class DetailedBarChartActivity : AppCompatActivity() {

    private lateinit var expensesChart: BarChart
    private lateinit var budgetsChart: BarChart
    private lateinit var periodSpinner: Spinner
    private lateinit var btnDateRange: Button
    private val db = Firebase.firestore
    private var startDate: Long = 0L
    private var endDate: Long = System.currentTimeMillis()

    // Initialize Firebase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detailed_bar_chart)

        expensesChart = findViewById(R.id.expensesChart)
        budgetsChart = findViewById(R.id.budgetsChart)
        periodSpinner = findViewById(R.id.periodSpinner)
        btnDateRange = findViewById(R.id.btnDateRange)

        setupPeriodSpinner()
        setupDateRangePicker()

    }

    private fun setupPeriodSpinner() {
        val periods = arrayOf("Last Week", "Last Month", "Last 3 Months", "Last 6 Months", "All Time")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, periods)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        periodSpinner.adapter = adapter


        var isInitialSetup = true
        var lastSelectionTime = 0L

        periodSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val now = System.currentTimeMillis()
                if (now - lastSelectionTime < 300) return
                lastSelectionTime = now

                if (isInitialSetup) {
                    isInitialSetup = false

                } else {
                    loadData(periods[position])
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }


        periodSpinner.setSelection(periods.indexOf("All Time"))
        loadData("All Time")
    }

    private fun setupDateRangePicker() {
        btnDateRange.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select Date Range")
                .setSelection(androidx.core.util.Pair(startDate, endDate))
                .build()

            datePicker.addOnPositiveButtonClickListener { selection ->
                startDate = selection.first ?: 0L
                endDate = selection.second ?: System.currentTimeMillis()
                loadData("Custom Range")
                btnDateRange.text = formatDateRange(startDate, endDate)
            }

            datePicker.show(supportFragmentManager, "DATE_RANGE_PICKER")
        }
    }

    private fun formatDateRange(start: Long, end: Long): String {
        val format = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return "${format.format(Date(start))} - ${format.format(Date(end))}"
    }

    private var isLoading = false

    private fun loadData(period: String) {


        if (isLoading) {
            Log.d("ChartDebug", "Load already in progress, skipping")
            return
        }
        isLoading = true


        runOnUiThread {
            expensesChart.setNoDataText("Loading expenses...")
            expensesChart.setNoDataTextColor(Color.BLACK)
            budgetsChart.setNoDataText("Loading budgets...")
            budgetsChart.setNoDataTextColor(Color.BLACK)
            expensesChart.invalidate()
            budgetsChart.invalidate()
        }

        val currentUserId = getSharedPreferences("UserPrefs", MODE_PRIVATE)
            .getString("username", "") ?: "".also {
            Log.e("ChartDebug", "No username found in SharedPreferences")
        }

        Log.d("ChartDebug", "Starting data load for user: $currentUserId, period: $period")

        CoroutineScope(Dispatchers.IO).launch {
            try {

                val expensesQuery = if (period == "All Time") {
                    Log.d("ChartDebug", "Querying ALL expenses")
                    db.collection("expenses")
                        .whereEqualTo("username", currentUserId)
                } else if (period == "Custom Range") {
                    Log.d("ChartDebug", "Querying CUSTOM RANGE expenses from ${Date(startDate)} to ${Date(endDate)}")
                    db.collection("expenses")
                        .whereEqualTo("username", currentUserId)
                        .whereGreaterThanOrEqualTo("expenseDate", Date(startDate))
                        .whereLessThanOrEqualTo("expenseDate", Date(endDate))
                } else {
                    val startMillis = getStartDateForPeriod(period)
                    Log.d("ChartDebug", "Querying $period expenses from ${Date(startMillis)}")
                    db.collection("expenses")
                        .whereEqualTo("username", currentUserId)
                        .whereGreaterThanOrEqualTo("expenseDate", Date(startMillis))
                        .whereLessThanOrEqualTo("expenseDate", Date(System.currentTimeMillis()))
                }


                val expensesDeferred = expensesQuery.get()
                val budgetsDeferred = db.collection("budgets")
                    .whereEqualTo("username", currentUserId)
                    .get()


                val expensesSnapshot = expensesDeferred.await()
                val budgetsSnapshot = budgetsDeferred.await()


                val expenses = expensesSnapshot.toObjects(ExpenseFirebase::class.java)
                val budgets = budgetsSnapshot.toObjects(BudgetFirestore::class.java)

                Log.d("ChartDebug", "Loaded ${expenses.size} expenses and ${budgets.size} budgets")


                runOnUiThread {
                    if (expenses.isEmpty()) {
                        Log.d("ChartDebug", "No expenses found for period: $period")
                        expensesChart.clear()
                        expensesChart.setNoDataText("No expenses in $period period")
                        expensesChart.setNoDataTextColor(Color.BLACK)
                    } else {
                        Log.d("ChartDebug", "Setting up chart with ${expenses.size} expenses")
                        setupExpensesChart(expenses, budgets)
                    }

                    if (budgets.isEmpty()) {
                        Log.d("ChartDebug", "No budgets found")
                        budgetsChart.clear()
                        budgetsChart.setNoDataText("No budgets configured")
                        budgetsChart.setNoDataTextColor(Color.BLACK)
                    } else {
                        Log.d("ChartDebug", "Setting up chart with ${budgets.size} budgets")
                        setupBudgetsChart(budgets)
                    }
                }

            } catch (e: Exception) {
                Log.e("DetailedBarChart", "Error loading data", e)
                runOnUiThread {
                    expensesChart.clear()
                    budgetsChart.clear()
                    val errorMsg = e.message?.take(50) ?: "Unknown error"
                    expensesChart.setNoDataText("Error: $errorMsg")
                    budgetsChart.setNoDataText("Error: $errorMsg")
                    expensesChart.setNoDataTextColor(Color.RED)
                    budgetsChart.setNoDataTextColor(Color.RED)
                }
            } finally {
                isLoading = false
                Log.d("ChartDebug", "Completed data load for period: $period")
            }
        }
    }

    private fun getStartDateForPeriod(period: String): Long {
        val calendar = Calendar.getInstance()
        when (period) {
            "Last Week" -> calendar.add(Calendar.WEEK_OF_YEAR, -1)
            "Last Month" -> calendar.add(Calendar.MONTH, -1)
            "Last 3 Months" -> calendar.add(Calendar.MONTH, -3)
            "Last 6 Months" -> calendar.add(Calendar.MONTH, -6)
            "All Time" -> return 0L
        }
        return calendar.timeInMillis
    }

    private fun setupExpensesChart(expenses: List<ExpenseFirebase>, budgets: List<BudgetFirestore>) {
        Log.d("ChartDebug", "Expenses count: ${expenses.size}")
        Log.d("ChartDebug", "Budgets count: ${budgets.size}")

        expenses.forEach { expense ->
            Log.d("ChartDebug", "Expense: ${expense.expenseCategory} - ${expense.expenseAmount}")
        }

        Log.d("ChartDebug", "Setting up chart with ${expenses.size} expenses")

        if (expenses.isEmpty()) {
            expensesChart.clear()
            expensesChart.setNoDataText("No expenses in selected period")
            expensesChart.setNoDataTextColor(Color.BLACK)
            expensesChart.invalidate()
            return
        }


        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val minGoal = sharedPreferences.getFloat("MIN_GOAL", 0f)
        val maxGoal = sharedPreferences.getFloat("TOTAL_BUDGET_GOAL", 0f)


        val expenseData = expenses.groupBy { it.expenseCategory }
            .mapValues { it.value.sumOf { expense -> expense.expenseAmount } }
            .toList()
            .sortedByDescending { it.second }


        val entries = expenseData.mapIndexed { index, (_, amount) ->
            BarEntry(index.toFloat(), amount.toFloat())
        }


        val dataSet = BarDataSet(entries, "Amount Spent").apply {
            colors = expenseData.map { (_, amount) ->
                when {
                    amount.toFloat() > maxGoal -> Color.parseColor("#F44336")
                    amount.toFloat() < minGoal -> Color.parseColor("#FFC107")
                    else -> Color.parseColor("#4CAF50")
                }
            }
            valueTextColor = Color.BLACK
            valueTextSize = 10f
            setDrawValues(true)
        }


        expensesChart.apply {

            setDrawBarShadow(false)
            setDrawValueAboveBar(true)
            description.isEnabled = false
            setPinchZoom(false)
            setDrawGridBackground(false)
            setBackgroundColor(Color.WHITE)
            animateY(800)


            data = BarData(dataSet).apply {
                barWidth = 0.5f
                setValueTextSize(10f)
            }


            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
                textColor = Color.BLACK
                textSize = 10f
                labelRotationAngle = -45f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return expenseData.getOrNull(value.toInt())?.first ?: ""
                    }
                }
            }


            axisLeft.apply {
                removeAllLimitLines()
                textColor = Color.BLACK
                textSize = 10f
                axisMinimum = 0f
                setDrawGridLines(true)
                gridColor = Color.parseColor("#EEEEEE")
                axisLineColor = Color.DKGRAY


                if (minGoal > 0) {
                    addLimitLine(
                        LimitLine(minGoal, "Min Goal: ${"%.2f".format(minGoal)}").apply {
                            lineColor = Color.RED
                            textColor = Color.RED
                            lineWidth = 1.5f
                            textSize = 10f
                            enableDashedLine(10f, 10f, 0f)
                        }
                    )
                }

                if (maxGoal > 0) {
                    addLimitLine(
                        LimitLine(maxGoal, "Max Goal: ${"%.2f".format(maxGoal)}").apply {
                            lineColor = Color.GREEN
                            textColor = Color.GREEN
                            lineWidth = 1.5f
                            textSize = 10f
                            enableDashedLine(10f, 10f, 0f)
                        }
                    )
                }
            }

            axisRight.isEnabled = false


            legend.apply {
                textColor = Color.DKGRAY
                textSize = 11f
                verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
                yOffset = 15f
                xOffset = 0f
            }

            setExtraOffsets(12f, 12f, 12f, 12f)
            setVisibleXRangeMaximum(5f)
            invalidate()
        }
    }

    private fun setupBudgetsChart(budgets: List<BudgetFirestore>) {
        if (budgets.isEmpty()) {
            budgetsChart.clear()
            budgetsChart.invalidate()
            return
        }


        val budgetData = budgets.groupBy { it.budgetCategory }
            .mapValues { it.value.sumOf { budget -> budget.budgetAmount } }
            .toList()
            .sortedByDescending { it.second }


        val entries = budgetData.mapIndexed { index, (_, amount) ->
            BarEntry(index.toFloat(), amount.toFloat())
        }

        // Configure dataset
        val dataSet = BarDataSet(entries, "Budget Amount").apply {
            colors = listOf(
                Color.parseColor("#2196F3"),  // Blue
                Color.parseColor("#4CAF50"),  // Green
                Color.parseColor("#FFC107"),  // Amber
                Color.parseColor("#9C27B0"),  // Purple
                Color.parseColor("#F44336")   // Red
            )
            valueTextColor = Color.BLACK
            valueTextSize = 10f
            setDrawValues(true)
        }

        // Configure chart
        budgetsChart.apply {
            // Basic setup
            setDrawBarShadow(false)
            setDrawValueAboveBar(true)
            description.isEnabled = false
            setPinchZoom(false)
            setDrawGridBackground(false)
            setBackgroundColor(Color.WHITE)
            animateY(800)


            data = BarData(dataSet).apply {
                barWidth = 0.5f
                setValueTextSize(10f)
            }


            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
                textColor = Color.BLACK
                textSize = 10f
                labelRotationAngle = -45f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return budgetData.getOrNull(value.toInt())?.first ?: ""
                    }
                }
            }


            axisLeft.apply {
                textColor = Color.BLACK
                textSize = 10f
                axisMinimum = 0f
                setDrawGridLines(true)
                gridColor = Color.parseColor("#EEEEEE")
                axisLineColor = Color.DKGRAY
            }


            axisRight.isEnabled = false


            legend.apply {
                textColor = Color.DKGRAY
                textSize = 11f
                verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
                yOffset = 15f
                xOffset = 0f
            }


            setExtraOffsets(12f, 12f, 12f, 12f)
            setVisibleXRangeMaximum(5f)

            invalidate()
        }
    }
}