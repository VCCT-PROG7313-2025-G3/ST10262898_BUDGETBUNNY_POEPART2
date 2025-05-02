package com.fake.st10262898_budgetbunny_poepart2

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.fake.st10262898_budgetbunny_poepart2.data.Budget
import com.fake.st10262898_budgetbunny_poepart2.data.BudgetBunnyDatabase
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BudgetGoalsOverviewActivity : AppCompatActivity() {

    private var currentSavedAmount = 0.0
    private var maxGoalValue = 0.0
    private lateinit var progressBar: ProgressBar
    private lateinit var budgetText: TextView
    private lateinit var goalsContainer: LinearLayout
    private lateinit var tvMinGoalValue: TextView
    private lateinit var tvMaxGoalValue: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget_goals_overview)

        // Initialize all views
        progressBar = findViewById(R.id.progressBar)
        budgetText = findViewById(R.id.budgetForMonth)
        goalsContainer = findViewById(R.id.goalsContainer)
        tvMinGoalValue = findViewById(R.id.tv_minGoalValue)
        tvMaxGoalValue = findViewById(R.id.tv_maxGoalValue)


        setupUI()
    }

    private fun setupUI() {

        val addIncomeBtn = Button(this).apply {

        }

        val addGoalButton = findViewById<Button>(R.id.btn_add_goal)
        addGoalButton.setOnClickListener {
            val intent = Intent(this, GoalEntry::class.java)
            startActivity(intent)
        }

        // Add button to the top of the card
        val cardView = findViewById<CardView>(R.id.card_budgetGoal)
        (cardView.getChildAt(0) as LinearLayout).addView(addIncomeBtn, 1)

        // Setup navigation bar...
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.nav_budgetGoal
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, HomePageActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_transactions -> {
                    val intent = Intent(this, TransactionsActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_budgetGoal -> {

                    true
                }
                R.id.nav_settings -> {
                    val intent = Intent(this, Settings::class.java)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false

            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadBudgetGoals()
    }

    private fun loadBudgetGoals() {
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "") ?: return

        lifecycleScope.launch {
            val budgetGoals = withContext(Dispatchers.IO) {
                val db = BudgetBunnyDatabase.getDatabase(this@BudgetGoalsOverviewActivity)
                db.budgetDao().getBudgetForUser(username)
            }

            updateUI(budgetGoals)
        }
    }

    private fun updateUI(budgetGoals: List<Budget>) {
        // Calculate values
        val minGoal = budgetGoals.minOfOrNull { it.totalBudgetGoal } ?: 0.0
        maxGoalValue = budgetGoals.sumOf { it.totalBudgetGoal }
        currentSavedAmount = budgetGoals.sumOf { it.budgetAmount }

        // Update progress bar
        progressBar.max = maxGoalValue.toInt()
        progressBar.progress = currentSavedAmount.toInt()
        budgetText.text = "R${currentSavedAmount.toInt()} of R${maxGoalValue.toInt()} saved"

        // Update min/max labels
        tvMinGoalValue.text = "Min Goal: R${minGoal.toInt()}"
        tvMaxGoalValue.text = "Max Goal: R${maxGoalValue.toInt()}"

        // Clear and rebuild goals container
        goalsContainer.removeAllViews()
        val inflater = LayoutInflater.from(this)

        // Group by category and add to UI
        budgetGoals.groupBy { it.budgetCategory }.forEach { (category, budgets) ->
            budgets.forEach { budget ->
                val cardView = inflater.inflate(R.layout.item_goal_card, goalsContainer, false)
                val tvCategory = cardView.findViewById<TextView>(R.id.tv_category)
                val tvAmount = cardView.findViewById<TextView>(R.id.tv_amount)

                tvCategory.text = budget.budgetCategory
                tvAmount.text = "Goal: R${budget.totalBudgetGoal} (Saved: R${budget.budgetAmount})"
                goalsContainer.addView(cardView)
            }
        }

        // Update marker positions (if needed)
        updateMarkerPositions(minGoal)
    }

    private fun updateMarkerPositions(minGoal: Double) {
        val minGoalMarker = findViewById<View>(R.id.minGoalMarker)
        val maxGoalMarker = findViewById<View>(R.id.maxGoalMarker)
        val minGoalLabel = findViewById<TextView>(R.id.minGoalLabel)
        val maxGoalLabel = findViewById<TextView>(R.id.maxGoalLabel)

        progressBar.post {
            val progressBarWidth = progressBar.width
            if (maxGoalValue > 0) {
                val minGoalMarkerPosition = (minGoal / maxGoalValue * progressBarWidth).toInt()

                minGoalMarker.layoutParams =
                    (minGoalMarker.layoutParams as RelativeLayout.LayoutParams).apply {
                        leftMargin = minGoalMarkerPosition
                    }

                maxGoalMarker.post {
                    val maxMarkerWidth = maxGoalMarker.width
                    val maxGoalMarkerPosition = progressBarWidth - maxMarkerWidth

                    maxGoalMarker.layoutParams =
                        (maxGoalMarker.layoutParams as RelativeLayout.LayoutParams).apply {
                            leftMargin = maxGoalMarkerPosition
                        }
                }

                minGoalLabel.post {
                    minGoalLabel.layoutParams =
                        (minGoalLabel.layoutParams as RelativeLayout.LayoutParams).apply {
                            leftMargin = minGoalMarkerPosition - (minGoalLabel.width / 2)
                        }
                }

                maxGoalLabel.post {
                    maxGoalLabel.layoutParams =
                        (maxGoalLabel.layoutParams as RelativeLayout.LayoutParams).apply {
                            leftMargin = progressBarWidth - (maxGoalLabel.width / 2)
                        }
                }
            }
        }
    }
}