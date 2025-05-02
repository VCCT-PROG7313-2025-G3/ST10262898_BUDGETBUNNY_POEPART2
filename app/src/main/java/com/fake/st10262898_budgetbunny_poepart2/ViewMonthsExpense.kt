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




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewMonthsExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize ViewModel
        expenseViewModel = ViewModelProvider(this).get(ExpenseViewModel::class.java)
        setupRecyclerView()




        // Set click listeners for date pickers
        binding.btnStartDate.setOnClickListener { showDatePicker(true) }
        binding.btnEndDate.setOnClickListener { showDatePicker(false) }

        // Set click listener for view expenses button
        binding.btnViewExpenses.setOnClickListener {
            Log.d(TAG, "View Expenses button clicked")

            val username = getCurrentUsername()
            Log.d(TAG, "Current username: ${username ?: "null"}")

            if (startDate == null || endDate == null) {
                Log.w(TAG, "Dates not selected - Start: $startDate, End: $endDate")
                toast("Please select both dates")
                return@setOnClickListener
            }

            if (username == null) {
                Log.e(TAG, "No username found")
                toast("User not logged in")
                return@setOnClickListener
            }

            Log.d(TAG, "Loading expenses between ${Date(startDate!!)} and ${Date(endDate!!)}")
            expenseViewModel.loadExpensesBetweenDates(username, startDate!!, endDate!!)
        }

        // Observe the filtered expenses
        expenseViewModel.filteredExpenses.observe(this) { expenses ->
            Log.d(TAG, "Observed ${expenses.size} expenses in LiveData")
            if (expenses.isEmpty()) {
                Log.w(TAG, "Empty expense list received")
                toast("No expenses found for selected period") // Add this extension function
            }
            expenseAdapter.submitList(expenses)
        }
    }

    private fun setupRecyclerView() {
        expenseAdapter = ExpenseDateAdapter(emptyList())  // Initialize with empty list
        binding.rvExpenses.apply {
            layoutManager = LinearLayoutManager(this@ViewMonthsExpense)
            adapter = expenseAdapter
        }
    }

    private fun showDatePicker(isStartDate: Boolean) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                val selectedDate = Calendar.getInstance().apply {
                    set(year, month, day)
                }.timeInMillis

                if (isStartDate) {
                    startDate = selectedDate
                    binding.tvStartDate.text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(selectedDate))
                } else {
                    endDate = selectedDate
                    binding.tvEndDate.text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(selectedDate))
                }

                // Enable the view button only when both dates are selected
                binding.btnViewExpenses.isEnabled = startDate != null && endDate != null
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun getCurrentUsername(): String? {
        // Change from "user_prefs" to "UserPrefs" to match MainActivity
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val username = sharedPref.getString("username", null)

        Log.d(TAG, "Retrieving username from SharedPreferences: $username")
        Log.d(TAG, "All SharedPreferences contents: ${sharedPref.all}")

        return username
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}