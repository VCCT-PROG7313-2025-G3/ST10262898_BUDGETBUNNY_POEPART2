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
        enableEdgeToEdge()
        setContentView(R.layout.activity_budget_goals_overview)

        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val minGoalMarker = findViewById<View>(R.id.minGoalMarker)
        val maxGoalMarker = findViewById<View>(R.id.maxGoalMarker)
        val minGoalLabel = findViewById<TextView>(R.id.minGoalLabel)
        val maxGoalLabel = findViewById<TextView>(R.id.maxGoalLabel)
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

            // Get the minimum and maximum budget goals
            val minGoal = budgetGoals.minOfOrNull { it.totalBudgetGoal } ?: 0.0
            val totalBudgetGoal = budgetGoals.sumOf { it.totalBudgetGoal }

            // Set progress bar info
            val currentSavedAmount = 0.0 // Placeholder for now
            progressBar.max = totalBudgetGoal.toInt()
            progressBar.progress = currentSavedAmount.toInt()

            // Update the progress text
            budgetText.text = "R${currentSavedAmount.toInt()} of R${totalBudgetGoal.toInt()} saved"

            // Post the changes to ensure the ProgressBar is fully laid out
            progressBar.post {
                // Calculate the position of the Min Goal Marker
                val progressBarWidth = progressBar.width
                val minGoalMarkerPosition = (minGoal / totalBudgetGoal * progressBarWidth).toInt()
                val maxGoalMarkerPosition = (totalBudgetGoal / totalBudgetGoal * progressBarWidth).toInt()

                // Set the position of the markers
                minGoalMarker.layoutParams = (minGoalMarker.layoutParams as FrameLayout.LayoutParams).apply {
                    leftMargin = minGoalMarkerPosition
                }

                maxGoalMarker.layoutParams = (maxGoalMarker.layoutParams as FrameLayout.LayoutParams).apply {
                    leftMargin = maxGoalMarkerPosition
                }

                // Set the label texts
                minGoalLabel.text = "Min Goal: R${minGoal.toInt()}"
                maxGoalLabel.text = "Max Goal: R${totalBudgetGoal.toInt()}"
            }
        }
    }


}