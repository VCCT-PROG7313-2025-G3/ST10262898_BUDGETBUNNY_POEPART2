package com.fake.st10262898_budgetbunny_poepart2

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ResourcesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_resources)
        setupCardAnimations()
    }

    //This finds the cards in the xml view and sets up the animations for them
    private fun setupCardAnimations() {
        val cards = listOf<View>(
            findViewById<CardView>(R.id.card_save_money),
            findViewById<CardView>(R.id.card_invest),
            findViewById<CardView>(R.id.card_earn)
        )

        cards.forEach { card ->
            card.setOnClickListener { view ->
                view.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction {
                    view.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                    when (view.id) {
                        R.id.card_save_money -> openTopic("save_money")
                        R.id.card_invest -> openTopic("invest_wisely")
                        R.id.card_earn -> openTopic("budgeting_tips")
                    }
                }.start()
            }
        }
    }

    //allows for a card view to be open
    private fun openTopic(topic: String) {
        startActivity(Intent(this, ResourceStepsActivity::class.java).apply {
            putExtra("topic", topic)
        })
        overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left)
    }
}