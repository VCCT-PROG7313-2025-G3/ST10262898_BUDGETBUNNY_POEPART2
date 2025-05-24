package com.fake.st10262898_budgetbunny_poepart2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.text.TextWatcher
import android.text.Editable
import com.fake.st10262898_budgetbunny_poepart2.viewmodel.BudgetViewModel

class bugetGoalsPage : AppCompatActivity() {

    private val budgetViewModel: BudgetViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_buget_goals_page)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val holidayButton = findViewById<Button>(R.id.holiday_button)
        val houseButton = findViewById<Button>(R.id.house_button)
        val tuitionButton = findViewById<Button>(R.id.tuition_button)
        val investmentsButton = findViewById<Button>(R.id.investments_button)
        val babyButton = findViewById<Button>(R.id.baby_button)
        val amountEditText = findViewById<EditText>(R.id.amountEditText)
        val minBudgetEditText = findViewById<EditText>(R.id.minBudgetEditText)

        // Initialize SharedPreferences
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "") ?: ""

        // Helper function to launch CategoryBudgetGoal (without saving)
        fun openCategoryGoalPage(category: String) {
            val goalText = amountEditText.text.toString().trim()
            val minGoalText = minBudgetEditText.text.toString().trim()

            if (goalText.isBlank() || minGoalText.isBlank()) {
                Toast.makeText(this, "Please enter both the total and min budget goals.", Toast.LENGTH_SHORT).show()
                return
            }

            val intent = Intent(this, CategoryBudgetGoal::class.java).apply {
                putExtra("CATEGORY_NAME", category)
                putExtra("USERNAME", username)
                // Pass goals via intent but don't save to prefs here
                putExtra("TOTAL_BUDGET_GOAL", goalText.toDoubleOrNull() ?: 0.0)
                putExtra("MIN_GOAL", minGoalText.toDoubleOrNull() ?: 0.0)
            }
            startActivity(intent)
        }

        // Set category button click listeners
        holidayButton.setOnClickListener { openCategoryGoalPage("Holiday") }
        houseButton.setOnClickListener { openCategoryGoalPage("House") }
        tuitionButton.setOnClickListener { openCategoryGoalPage("Tuition") }
        investmentsButton.setOnClickListener { openCategoryGoalPage("Investments") }
        babyButton.setOnClickListener { openCategoryGoalPage("Baby") }

        val btn_next = findViewById<Button>(R.id.next_button)
        btn_next.isEnabled = false

        // Enable Next button only when both goals are entered
        minBudgetEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(editable: Editable?) {
                val goalText = amountEditText.text.toString().trim()
                val minGoalText = minBudgetEditText.text.toString().trim()
                btn_next.isEnabled = goalText.isNotEmpty() && minGoalText.isNotEmpty()
            }
        })

        // Handle Next button click - single save point
        btn_next.setOnClickListener {
            val goalText = amountEditText.text.toString().trim()
            val minGoalText = minBudgetEditText.text.toString().trim()

            if (goalText.isNotEmpty() && minGoalText.isNotEmpty()) {
                // Save goals to SharedPreferences (only here)
                sharedPreferences.edit().apply {
                    putFloat("MIN_GOAL", minGoalText.toFloat())
                    putFloat("TOTAL_BUDGET_GOAL", goalText.toFloat())
                    apply()
                }
                Log.d("BudgetDebug", "Goals saved - Min: $minGoalText, Max: $goalText")
            }

            startActivity(Intent(this, BunnyNameActivity::class.java))
        }
    }
}