package com.fake.st10262898_budgetbunny_poepart2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.activity.viewModels
import com.fake.st10262898_budgetbunny_poepart2.viewmodel.BudgetViewModel
import android.text.TextWatcher
import android.text.Editable

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
        val minBudgetEditText = findViewById<EditText>(R.id.minBudgetEditText) // minTotalGoalBudget field

        // Save the username in SharedPreferences
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val username = sharedPreferences.getString("USERNAME", "") ?: ""

        val editor = sharedPreferences.edit()
        editor.putString("USERNAME", username)
        editor.apply()

        // Use the username as needed in ExpenseList
        Toast.makeText(this, "Welcome, $username!", Toast.LENGTH_SHORT).show()

        // Helper function to launch CategoryBudgetGoal
        fun openCategoryGoalPage(category: String) {
            val goalText = amountEditText.text.toString().trim()
            val minGoalText = minBudgetEditText.text.toString().trim()

            if (goalText.isBlank() || minGoalText.isBlank()) {
                // Show toast if either the total budget goal or min budget goal is not entered
                Toast.makeText(this, "Please enter both the total and min budget goals.", Toast.LENGTH_SHORT).show()
                return
            }

            val goal = goalText.toDoubleOrNull()
            val minGoal = minGoalText.toDoubleOrNull()

            if (goal != null && minGoal != null && username.isNotBlank()) {
                // Save the minTotalBudget goal to the database
                budgetViewModel.addBudget(
                    totalBudgetGoal = goal,
                    budgetCategory = category,
                    budgetAmount = goal,  // Assuming budgetAmount is the same as totalGoal for now
                    username = username,
                    minTotalBudgetGoal = minGoal // Save the minTotalBudgetGoal here as well
                )

                // Proceed with category-specific page
                val intent = Intent(this, CategoryBudgetGoal::class.java)
                intent.putExtra("CATEGORY_NAME", category)
                intent.putExtra("USERNAME", username)
                intent.putExtra("TOTAL_BUDGET_GOAL", goal)
                intent.putExtra("MIN_GOAL", minGoal)  // Pass minGoal here
                startActivity(intent)
            } else {
                Toast.makeText(this, "Please enter valid amounts.", Toast.LENGTH_SHORT).show()
            }
        }

        // Set click listeners
        holidayButton.setOnClickListener { openCategoryGoalPage("Holiday") }
        houseButton.setOnClickListener { openCategoryGoalPage("House") }
        tuitionButton.setOnClickListener { openCategoryGoalPage("Tuition") }
        investmentsButton.setOnClickListener { openCategoryGoalPage("Investments") }
        babyButton.setOnClickListener { openCategoryGoalPage("Baby") }

        // Find the items on the XML
        val btn_addCustomButton = findViewById<Button>(R.id.addCustom_button)
        val btn_next = findViewById<Button>(R.id.next_button)

        // Disable Next button until the user enters a valid minTotalBudget
        btn_next.isEnabled = false // Initially disabled

        // Enable the Next button only when both goals are entered
        minBudgetEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(editable: Editable?) {
                val goalText = amountEditText.text.toString().trim()
                val minGoalText = minBudgetEditText.text.toString().trim()

                // Enable/Disable the Next button based on both fields being filled
                btn_next.isEnabled = goalText.isNotEmpty() && minGoalText.isNotEmpty()
            }
        })

        // Sends user to custom budget when clicked
        btn_addCustomButton.setOnClickListener {
            val intent = Intent(this, customBudgetPage::class.java)
            startActivity(intent)
        }

        // Sends the user to the next page when clicking next
        btn_next.setOnClickListener {
            val intent = Intent(this, BunnyNameActivity::class.java)
            startActivity(intent)
        }
    }

}