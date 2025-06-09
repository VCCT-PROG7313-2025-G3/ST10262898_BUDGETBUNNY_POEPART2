package com.fake.st10262898_budgetbunny_poepart2

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.fake.st10262898_budgetbunny_poepart2.databinding.ActivityViewMonthsExpenseBinding
import com.fake.st10262898_budgetbunny_poepart2.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ViewMonthsExpense : AppCompatActivity() {

    private lateinit var expenseViewModel: ExpenseViewModel
    private lateinit var binding: ActivityViewMonthsExpenseBinding
    private var startDate: Long? = null
    private var endDate: Long? = null
    private lateinit var expenseAdapter: ExpenseDateAdapter

    private val TAG = ViewMonthsExpense::class.java.simpleName
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewMonthsExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel()
        setupUI()
        setupObservers()
    }

    private fun setupViewModel() {
        expenseViewModel = ViewModelProvider(this).get(ExpenseViewModel::class.java)


        intent.getIntExtra("month", -1).takeIf { it != -1 }?.let { month ->
            setupMonthRange(month)
        }
    }

    private fun setupUI() {
        setupRecyclerView()

        binding.apply {
            btnStartDate.setOnClickListener { showDatePicker(true) }
            btnEndDate.setOnClickListener { showDatePicker(false) }
            btnViewExpenses.setOnClickListener { onViewExpensesClicked() }
        }
    }

    private fun setupMonthRange(month: Int) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
        }

        startDate = calendar.timeInMillis
        binding.tvStartDate.text = dateFormat.format(Date(startDate!!))

        calendar.add(Calendar.MONTH, 1)
        endDate = calendar.timeInMillis
        binding.tvEndDate.text = dateFormat.format(Date(endDate!!))

        // Auto-load expenses for this month
        loadExpensesIfReady()
    }

    private fun setupRecyclerView() {
        expenseAdapter = ExpenseDateAdapter(emptyList()) { expense ->
            // Handle item click if needed
        }
        binding.rvExpenses.apply {
            layoutManager = LinearLayoutManager(this@ViewMonthsExpense)
            adapter = expenseAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupObservers() {
        expenseViewModel.expenses.observe(this) { expenses ->
            Log.d(TAG, "Displaying ${expenses.size} expenses")
            if (expenses.isEmpty()) {
                toast(getString(R.string.no_expenses_found))
            }
            expenseAdapter.submitList(expenses)
        }

        expenseViewModel.errorMessage.observe(this) { message ->
            message?.let {
                toast(it)
                Log.e(TAG, "Error: $it")
            }
        }
    }

    private fun onViewExpensesClicked() {
        Log.d(TAG, "View Expenses button clicked")
        loadExpensesIfReady()
    }

    private fun loadExpensesIfReady() {
        val username = getCurrentUsername()

        when {
            username == null -> {
                Log.e(TAG, "No username found")
                toast(getString(R.string.user_not_logged_in))
            }
            startDate == null || endDate == null -> {
                Log.w(TAG, "Dates not selected")
                toast(getString(R.string.select_both_dates))
            }
            startDate!! > endDate!! -> {
                Log.w(TAG, "Invalid date range")
                toast(getString(R.string.invalid_date_range))
            }
            else -> {
                Log.d(TAG, "Loading expenses between ${Date(startDate!!)} and ${Date(endDate!!)}")
                expenseViewModel.loadExpensesBetweenDates(username, startDate!!, endDate!!)
            }
        }
    }

    private fun showDatePicker(isStartDate: Boolean) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                val selectedDate = Calendar.getInstance().apply {
                    set(year, month, day, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                if (isStartDate) {
                    startDate = selectedDate
                    binding.tvStartDate.text = dateFormat.format(Date(selectedDate))
                } else {
                    endDate = selectedDate
                    binding.tvEndDate.text = dateFormat.format(Date(selectedDate))
                }

                binding.btnViewExpenses.isEnabled = startDate != null && endDate != null
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {

            if (!isStartDate) {
                datePicker.maxDate = System.currentTimeMillis()
            }
        }.show()
    }

    private fun getCurrentUsername(): String? {
        return getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            .getString("username", null)
            .also { username ->
                Log.d(TAG, "Retrieved username: $username")
            }
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {

        const val EXTRA_MONTH = "month"
    }
}