package com.fake.st10262898_budgetbunny_poepart2

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
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
        setContentView(R.layout.activity_budget_goals_overview)

        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val minGoalMarker = findViewById<View>(R.id.minGoalMarker)
        val maxGoalMarker = findViewById<View>(R.id.maxGoalMarker)
        val minGoalLabel = findViewById<TextView>(R.id.minGoalLabel)
        val maxGoalLabel = findViewById<TextView>(R.id.maxGoalLabel)
        val budgetText = findViewById<TextView>(R.id.budgetForMonth)

        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val username = sharedPreferences.getString("USERNAME", "") ?: return

        lifecycleScope.launch {
            val budgetGoals = withContext(Dispatchers.IO) {
                val db = BudgetBunnyDatabase.getDatabase(this@BudgetGoalsOverviewActivity)
                db.budgetDao().getBudgetForUser(username)
            }

            val minGoal = budgetGoals.minOfOrNull { it.totalBudgetGoal } ?: 0.0
            val totalBudgetGoal = budgetGoals.sumOf { it.totalBudgetGoal }
            val currentSavedAmount = 0.0 // Placeholder

            progressBar.max = totalBudgetGoal.toInt()
            progressBar.progress = currentSavedAmount.toInt()
            budgetText.text = "R${currentSavedAmount.toInt()} of R${totalBudgetGoal.toInt()} saved"

            progressBar.post {
                val progressBarWidth = progressBar.width
                val minGoalMarkerPosition = (minGoal / totalBudgetGoal * progressBarWidth).toInt()
                val maxGoalMarkerPosition = progressBarWidth // always at the end

                // Position markers
                minGoalMarker.layoutParams = (minGoalMarker.layoutParams as FrameLayout.LayoutParams).apply {
                    leftMargin = minGoalMarkerPosition
                }

                maxGoalMarker.layoutParams = (maxGoalMarker.layoutParams as FrameLayout.LayoutParams).apply {
                    leftMargin = maxGoalMarkerPosition
                }

                // Update labels
                minGoalLabel.text = "Min Goal: R${minGoal.toInt()}"
                maxGoalLabel.text = "Max Goal: R${totalBudgetGoal.toInt()}"

                // Position labels after layout pass
                minGoalLabel.post {
                    minGoalLabel.layoutParams = (minGoalLabel.layoutParams as FrameLayout.LayoutParams).apply {
                        leftMargin = minGoalMarkerPosition - (minGoalLabel.width / 2)
                    }
                }

                maxGoalLabel.post {
                    maxGoalLabel.layoutParams = (maxGoalLabel.layoutParams as FrameLayout.LayoutParams).apply {
                        leftMargin = maxGoalMarkerPosition - (maxGoalLabel.width / 2)
                    }
                }
            }
        }
    }


}