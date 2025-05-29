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
                finish()  // End of steps
            }
        }
    }

    private fun showStep(step: Int) {
        val resourceId = when (topic) {
            "save_money" -> when (step) {
                0 -> R.drawable.save_money_step1
                1 -> R.drawable.save_money_step2
                else -> R.drawable.save_money_step3
            }
            // Add other topics here
            else -> R.drawable.default_step
        }
        imageView.setImageResource(resourceId)
    }

    private fun getTotalSteps(topic: String): Int {
        return when (topic) {
            "save_money" -> 3  // Example: 3 steps for "save money"
            else -> 0
        }
    }
}