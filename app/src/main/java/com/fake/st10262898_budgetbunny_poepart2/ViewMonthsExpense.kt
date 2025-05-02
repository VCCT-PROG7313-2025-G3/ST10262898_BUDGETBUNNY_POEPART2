package com.fake.st10262898_budgetbunny_poepart2

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

import android.widget.*
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.fake.st10262898_budgetbunny_poepart2.databinding.ActivityViewMonthsExpenseBinding
import com.fake.st10262898_budgetbunny_poepart2.viewmodel.ExpenseViewModel

class ViewMonthsExpense : AppCompatActivity() {

    private lateinit var expenseViewModel: ExpenseViewModel
    private lateinit var binding: ActivityViewMonthsExpenseBinding
    private var startDate: Long? = null
    private var endDate: Long? = null
    private lateinit var expenseAdapter: ExpenseDateAdapter

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
            val username = getCurrentUsername() // Implement this method to get current user
            if (startDate != null && endDate != null && username != null) {
                expenseViewModel.loadExpensesBetweenDates(username, startDate!!, endDate!!)
            }
        }

        // Observe the filtered expenses
        expenseViewModel.filteredExpenses.observe(this) { expenses ->
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
        // Implement this to return the current logged-in username
        // For example, from SharedPreferences or your authentication system
        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return sharedPref.getString("username", null)
    }

}