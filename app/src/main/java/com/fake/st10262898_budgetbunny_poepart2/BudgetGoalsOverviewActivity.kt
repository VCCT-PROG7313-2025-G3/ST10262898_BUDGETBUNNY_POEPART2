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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget_goals_overview)

        progressBar = findViewById(R.id.progressBar)
        budgetText = findViewById(R.id.budgetForMonth)
        val minGoalMarker = findViewById<View>(R.id.minGoalMarker)
        val maxGoalMarker = findViewById<View>(R.id.maxGoalMarker)
        val minGoalLabel = findViewById<TextView>(R.id.minGoalLabel)
        val maxGoalLabel = findViewById<TextView>(R.id.maxGoalLabel)
        val tvMinGoalValue = findViewById<TextView>(R.id.tv_minGoalValue)
        val tvMaxGoalValue = findViewById<TextView>(R.id.tv_maxGoalValue)
        val goalsContainer = findViewById<LinearLayout>(R.id.goalsContainer)

        // Add Income Button
        val addIncomeBtn = Button(this).apply {
            text = "Add Income"
            setTextColor(Color.WHITE)
            backgroundTintList = ContextCompat.getColorStateList(this@BudgetGoalsOverviewActivity, R.color.dark_pastel_purple)
            setOnClickListener {
                val input = EditText(this@BudgetGoalsOverviewActivity).apply {
                    inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
                }
                AlertDialog.Builder(this@BudgetGoalsOverviewActivity)
                    .setTitle("Add Income")
                    .setView(input)
                    .setPositiveButton("Add") { _, _ ->
                        val amount = input.text.toString().toDoubleOrNull() ?: 0.0
                        if (amount > 0) {
                            currentSavedAmount += amount
                            // Update both progress and max value
                            progressBar.max = maxGoalValue.toInt()
                            progressBar.progress = currentSavedAmount.toInt()
                            budgetText.text = "R${currentSavedAmount.toInt()} of R${maxGoalValue.toInt()} saved"
                            Toast.makeText(this@BudgetGoalsOverviewActivity, "Added R$amount", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }

        //Button to add goals
        val addGoalButton = findViewById<Button>(R.id.btn_add_goal)
        addGoalButton.setOnClickListener {
            val intent = Intent(this, GoalEntry::class.java)
            startActivity(intent)
        }

        // Add button to the top of the card
        val cardView = findViewById<CardView>(R.id.card_budgetGoal)
        (cardView.getChildAt(0) as LinearLayout).addView(addIncomeBtn, 1)

        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "") ?: return

        val tvDateTime: TextView = findViewById(R.id.tv_dateTime)
        tvDateTime.text = java.text.SimpleDateFormat("dd MMMM yyyy, HH:mm", java.util.Locale.getDefault())
            .format(java.util.Date())

        lifecycleScope.launch {
            val budgetGoals = withContext(Dispatchers.IO) {
                val db = BudgetBunnyDatabase.getDatabase(this@BudgetGoalsOverviewActivity)
                val goals = db.budgetDao().getBudgetForUser(username)
                Log.d("BudgetDebug", "Retrieved ${goals.size} goals for user $username")
                goals.forEach { Log.d("BudgetDebug", "Goal: ${it.budgetCategory} - R${it.budgetAmount}") }
                goals
            }

            val minGoal = budgetGoals.minOfOrNull { it.totalBudgetGoal } ?: 0.0
            maxGoalValue = budgetGoals.sumOf { it.totalBudgetGoal } // Store max value in our variable

            progressBar.max = maxGoalValue.toInt()
            progressBar.progress = currentSavedAmount.toInt()
            budgetText.text = "R${currentSavedAmount.toInt()} of R${maxGoalValue.toInt()} saved"

            tvMinGoalValue.text = "Min Goal: R${minGoal.toInt()}"
            tvMaxGoalValue.text = "Max Goal: R${maxGoalValue.toInt()}"

            maxGoalMarker.visibility = View.VISIBLE
            maxGoalMarker.bringToFront()

            val viewByDateButton: Button = findViewById(R.id.btn_view_by_date)
            viewByDateButton.setOnClickListener {
                val intent = Intent(this@BudgetGoalsOverviewActivity, ViewBudgetByDate::class.java)
                startActivity(intent)
            }

            progressBar.post {
                val progressBarWidth = progressBar.width
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

            val inflater = LayoutInflater.from(this@BudgetGoalsOverviewActivity)
            goalsContainer.removeAllViews()

            for (goal in budgetGoals) {
                val cardView = inflater.inflate(R.layout.item_goal_card, goalsContainer, false)
                val tvCategory = cardView.findViewById<TextView>(R.id.tv_category)
                val tvAmount = cardView.findViewById<TextView>(R.id.tv_amount)

                tvCategory.text = goal.budgetCategory
                tvAmount.text = "Amount: R${goal.budgetAmount}"

                goalsContainer.addView(cardView)
            }
        }

        // Initialize navigation bar
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.nav_budgetGoal // Highlight current tab
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, MainActivity::class.java)
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
                    // Already on this page
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
}