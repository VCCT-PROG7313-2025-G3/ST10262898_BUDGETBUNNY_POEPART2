package com.fake.st10262898_budgetbunny_poepart2

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.fake.st10262898_budgetbunny_poepart2.data.BudgetBunnyDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BudgetGoalsOverviewActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_budget_goals_overview)

        // Layout where cards will go
        val goalsContainer = findViewById<LinearLayout>(R.id.goalsContainer)

        // Progress bar and text
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val budgetText = findViewById<TextView>(R.id.budgetForMonth)

        // Get username from SharedPreferences
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val username = sharedPreferences.getString("USERNAME", "") ?: return

        // Load budget goals
        lifecycleScope.launch {
            val budgetGoals = withContext(Dispatchers.IO) {
                val db = BudgetBunnyDatabase.getDatabase(this@BudgetGoalsOverviewActivity)
                db.budgetDao().getBudgetForUser(username)
            }

            // 🔹 Set progress bar info
            val totalBudgetGoal = budgetGoals.sumOf { it.totalBudgetGoal }
            val currentSavedAmount = 0.0 // Placeholder for now

            progressBar.max = totalBudgetGoal.toInt()
            progressBar.progress = currentSavedAmount.toInt()

            budgetText.text = "R${currentSavedAmount.toInt()} of R${totalBudgetGoal.toInt()} saved"

            // 🔹 Dynamically add category cards
            for (goal in budgetGoals) {
                val card = layoutInflater.inflate(R.layout.item_goal_card, goalsContainer, false) as CardView
                val categoryText = card.findViewById<TextView>(R.id.tv_category)
                val amountText = card.findViewById<TextView>(R.id.tv_amount)

                categoryText.text = goal.budgetCategory
                amountText.text = "Goal: R${goal.budgetAmount}"

                goalsContainer.addView(card)
            }
        }


    }


}