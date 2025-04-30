package com.fake.st10262898_budgetbunny_poepart2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.activity.viewModels
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
        val amountEditText = findViewById<EditText>(R.id.amountEditText) // ← MOVE HERE
        val username = intent.getStringExtra("USERNAME") ?: ""

        // Helper function to launch CategoryBudgetGoal
        fun openCategoryGoalPage(category: String) {

            val goalText = amountEditText.text.toString()
            val goal = goalText.toDoubleOrNull()

            if (goal != null && username.isNotBlank()) {
                budgetViewModel.addBudget(
                    totalBudgetGoal = goal,
                    budgetCategory = category,
                    budgetAmount = 0.0, // for now, budgetAmount can be filled in on the next screen
                    username = username
                )
            }


            val intent = Intent(this, CategoryBudgetGoal::class.java)
            intent.putExtra("CATEGORY_NAME", category)
            intent.putExtra("USERNAME", username)
            intent.putExtra("TOTAL_BUDGET_GOAL", goal)
            startActivity(intent)


        }

        // Set click listeners
        holidayButton.setOnClickListener { openCategoryGoalPage("Holiday") }
        houseButton.setOnClickListener { openCategoryGoalPage("House") }
        tuitionButton.setOnClickListener { openCategoryGoalPage("Tuition") }
        investmentsButton.setOnClickListener { openCategoryGoalPage("Investments") }
        babyButton.setOnClickListener { openCategoryGoalPage("Baby") }


        //Find the items on the xml:
        val btn_addCustomButton = findViewById<Button>(R.id.addCustom_button)
        val btn_next = findViewById<Button>(R.id.next_button)


        //sends user to custom budget when its clicked
        btn_addCustomButton.setOnClickListener{
            val intent = Intent(this, customBudgetPage::class.java)
            startActivity(intent)
        }

        //Sends the user to the next page when clicking next
        btn_next.setOnClickListener {
            val intent = Intent(this,BunnyNameActivity::class.java)
            startActivity(intent)
        }
    }


}