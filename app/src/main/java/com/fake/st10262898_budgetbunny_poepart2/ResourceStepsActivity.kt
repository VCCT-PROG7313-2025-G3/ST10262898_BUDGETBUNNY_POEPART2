package com.fake.st10262898_budgetbunny_poepart2

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ResourceStepsActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private var currentStep = 0
    private lateinit var topic: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resource_steps)

        topic = intent.getStringExtra("topic") ?: "save_money"
        imageView = findViewById(R.id.iv_step_content)

        // Load the first step
        showStep(currentStep)

        // Tap to advance
        imageView.setOnClickListener {
            currentStep++
            if (currentStep < getTotalSteps(topic)) {
                showStep(currentStep)
            } else {
                finish()
            }
        }
    }

    private fun showStep(step: Int) {
        val resourceId = when (topic) {
            "save_money" -> when (step) {
                0 -> R.drawable.save_money_1
                1 -> R.drawable.save_money_2
                2 -> R.drawable.save_money_3
                3 -> R.drawable.save_money_4
                else -> R.drawable.save_money_4
            }
            "invest_wisely" -> when (step) {
                0 -> R.drawable.invest_1
                1 -> R.drawable.invest_2
                2 -> R.drawable.invest_3
                3 -> R.drawable.invest_4
                4 -> R.drawable.invest_5
                else -> R.drawable.invest_5
            }
            "budgeting_tips" -> when (step) {
                0 -> R.drawable.rb_01
                1 -> R.drawable.rb_02
                2 -> R.drawable.rb_03
                3 -> R.drawable.rb_04
                4 -> R.drawable.rb_05
                else -> R.drawable.rb_05
            }
            else -> R.drawable.default_step
        }
        imageView.setImageResource(resourceId)
    }

    private fun getTotalSteps(topic: String): Int {
        return when (topic) {
            "save_money" -> 4
            "invest_wisely" -> 5
            "budgeting_tips" -> 5
            else -> 0
        }
    }
}