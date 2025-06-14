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
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.Locale
import com.fake.st10262898_budgetbunny_poepart2.data.BudgetFirestore
import com.fake.st10262898_budgetbunny_poepart2.viewmodel.BudgetViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class BudgetGoalsOverviewActivity : AppCompatActivity() {

    private val budgetViewModel: BudgetViewModel by viewModels()
    private var maxGoalValue = 0.0
    private lateinit var progressBar: ProgressBar
    private lateinit var budgetText: TextView
    private lateinit var goalsContainer: LinearLayout
    private lateinit var tvMinGoalValue: TextView
    private lateinit var tvMaxGoalValue: TextView
    private lateinit var minGoalMarker: View
    private lateinit var maxGoalMarker: View
    private lateinit var minGoalLabel: TextView
    private lateinit var maxGoalLabel: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget_goals_overview)

        // Initialize all views
        progressBar = findViewById(R.id.progressBar)
        budgetText = findViewById(R.id.budgetForMonth)
        goalsContainer = findViewById(R.id.goalsContainer)
        tvMinGoalValue = findViewById(R.id.tv_minGoalValue)
        tvMaxGoalValue = findViewById(R.id.tv_maxGoalValue)
        minGoalMarker = findViewById(R.id.minGoalMarker)
        maxGoalMarker = findViewById(R.id.maxGoalMarker)
        minGoalLabel = findViewById(R.id.minGoalLabel)
        maxGoalLabel = findViewById(R.id.maxGoalLabel)

        setupUI()
        loadBudgetGoals()
    }

    //This setups all my User Interface
    private fun setupUI() {

        val addIncomeBtn = Button(this).apply {
            text = "Add Income"
            setTextColor(Color.WHITE)
            backgroundTintList = ContextCompat.getColorStateList(
                this@BudgetGoalsOverviewActivity,
                R.color.dark_pastel_purple
            )
            setOnClickListener {
                startActivity(Intent(this@BudgetGoalsOverviewActivity, BudgetSelectionActivity::class.java))
            }
        }


        val addGoalButton = findViewById<Button>(R.id.btn_add_goal).apply {
            setOnClickListener {
                startActivity(Intent(this@BudgetGoalsOverviewActivity, GoalEntry::class.java))
            }
        }

        val cardView = findViewById<CardView>(R.id.card_budgetGoal)
        (cardView.getChildAt(0) as LinearLayout).addView(addIncomeBtn, 1)

        findViewById<TextView>(R.id.tv_dateTime).text =
            SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault())
                .format(System.currentTimeMillis())

        findViewById<Button>(R.id.btn_view_by_date).setOnClickListener {
            startActivity(Intent(this, ViewBudgetByDate::class.java))
        }

        setupBottomNavigation()
    }





    //This is for the navigation at the bottom
    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.nav_budgetGoal

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this@BudgetGoalsOverviewActivity, HomePageActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_transactions -> {
                    startActivity(Intent(this@BudgetGoalsOverviewActivity, TransactionsActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_bunny -> {
                    startActivity(Intent(this, BunnyActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_budgetGoal -> true
                R.id.nav_settings -> {
                    startActivity(Intent(this@BudgetGoalsOverviewActivity, Settings::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }
    }

    //When you basically come back to the application from entering something then this is how it would resume
    override fun onResume() {
        super.onResume()
        loadBudgetGoals()


        budgetViewModel.updateStatus.observe(this) { (success, _) ->
            if (success) loadBudgetGoals()
        }
    }

    //This gets the budget goals for the specific user
    private fun loadBudgetGoals() {
        getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("username", "")?.let { username ->
            budgetViewModel.budgets.observe(this) { updateUI(it) }
            budgetViewModel.loadBudgets(username)
        }
    }

    //This is all implementation for the progress bar:
    private fun updateUI(budgetGoals: List<BudgetFirestore>) {
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val currentUserId = sharedPreferences.getString("username", "") ?: return


        val userMinGoal = sharedPreferences.getFloat("MIN_GOAL", 0f).toDouble()
        maxGoalValue = sharedPreferences.getFloat("TOTAL_BUDGET_GOAL", 0f).toDouble()


        val userBudgets = budgetGoals.filter { it.username == currentUserId }
        val totalIncome = userBudgets.sumOf { it.budgetIncome }


        progressBar.max = maxGoalValue.toInt()
        progressBar.progress = totalIncome.toInt()
        budgetText.text = "R${totalIncome.toInt()} of R${maxGoalValue.toInt()} saved"


        tvMinGoalValue.text = "Min Goal: R${userMinGoal.toInt()}"
        tvMaxGoalValue.text = "Max Goal: R${maxGoalValue.toInt()}"


        goalsContainer.removeAllViews()
        userBudgets.groupBy { it.budgetCategory }.forEach { (category, budgets) ->
            LayoutInflater.from(this).inflate(R.layout.item_goal_card, goalsContainer, false).apply {
                findViewById<TextView>(R.id.tv_category).text = category ?: "Uncategorized"
                findViewById<TextView>(R.id.tv_amount).text =
                    "Goal: R${budgets.sumOf { it.totalBudgetGoal }} (Income: R${budgets.sumOf { it.budgetIncome }})"
                goalsContainer.addView(this)
            }
        }

        updateMarkerPositions(userMinGoal)




    }

    //This updates the min and max goals according to what the user has entered
    private fun updateMarkerPositions(minGoal: Double) {
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