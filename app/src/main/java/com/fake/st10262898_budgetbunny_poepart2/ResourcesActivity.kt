package com.fake.st10262898_budgetbunny_poepart2

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class ResourcesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_resources)

        // Corrected with proper parenthesis
        findViewById<View>(R.id.card_save_money).setOnClickListener {
            startActivity(Intent(this, ResourceStepsActivity::class.java).apply {
                putExtra("topic", "save_money")
            })
        }

        findViewById<View>(R.id.card_invest).setOnClickListener {
            startActivity(Intent(this, ResourceStepsActivity::class.java).apply {
                putExtra("topic", "invest")
            })
        }

        findViewById<View>(R.id.card_earn).setOnClickListener {
            startActivity(Intent(this, ResourceStepsActivity::class.java).apply {
                putExtra("topic", "earn")
            })
        }
    }
}