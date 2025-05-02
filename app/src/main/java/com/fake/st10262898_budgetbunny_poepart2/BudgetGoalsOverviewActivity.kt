package com.fake.st10262898_budgetbunny_poepart2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
        val tvMinGoalValue = findViewById<TextView>(R.id.tv_minGoalValue)
        val tvMaxGoalValue = findViewById<TextView>(R.id.tv_maxGoalValue)
        val goalsContainer = findViewById<LinearLayout>(R.id.goalsContainer)


        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "") ?: return

        // Set the date and time
        val tvDateTime: TextView = findViewById(R.id.tv_dateTime)
        val currentDateTime = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault()).format(
            Date()
        )
        tvDateTime.text = currentDateTime

        lifecycleScope.launch {
            val budgetGoals = withContext(Dispatchers.IO) {
                val db = BudgetBunnyDatabase.getDatabase(this@BudgetGoalsOverviewActivity)
                val goals = db.budgetDao().getBudgetForUser(username)
                Log.d("BudgetDebug", "Retrieved ${goals.size} goals for user $username")
                goals.forEach { Log.d("BudgetDebug", "Goal: ${it.budgetCategory} - R${it.budgetAmount}") }
                goals
            }



            val minGoal = budgetGoals.minOfOrNull { it.totalBudgetGoal } ?: 0.0
            val totalBudgetGoal = budgetGoals.sumOf { it.totalBudgetGoal }
            val currentSavedAmount = 0.0

            progressBar.max = totalBudgetGoal.toInt()
            progressBar.progress = currentSavedAmount.toInt()
            budgetText.text = "R${currentSavedAmount.toInt()} of R${totalBudgetGoal.toInt()} saved"

            tvMinGoalValue.text = "Min Goal: R${minGoal.toInt()}"
            tvMaxGoalValue.text = "Max Goal: R${totalBudgetGoal.toInt()}"

            maxGoalMarker.visibility = View.VISIBLE
            maxGoalMarker.bringToFront()

            //Send users to the date selector page when pressing the button:
            val viewByDateButton: Button = findViewById(R.id.btn_view_by_date)
            viewByDateButton.setOnClickListener {
                val intent = Intent(this@BudgetGoalsOverviewActivity, ViewBudgetByDate::class.java)
                startActivity(intent)
            }

            progressBar.post {
                val progressBarWidth = progressBar.width
                val minGoalMarkerPosition = (minGoal / totalBudgetGoal * progressBarWidth).toInt()

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

            // 🔽 Inflate item_goal_card for each budget goal
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
    }


}