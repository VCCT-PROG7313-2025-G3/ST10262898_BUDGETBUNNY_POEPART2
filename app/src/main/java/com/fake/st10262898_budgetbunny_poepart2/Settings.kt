package com.fake.st10262898_budgetbunny_poepart2

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class Settings : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set click listener for migration button
        findViewById<TextView>(R.id.tv_migration).setOnClickListener {
            val intent = Intent(this, MigrationActivity::class.java)
            startActivity(intent)
        }

        // Set the date and time
        val tvDateTime: TextView = findViewById(R.id.tv_dateTime)
        val currentDateTime = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault()).format(Date())
        tvDateTime.text = currentDateTime


        setupBottomNavigation()

    }

    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.nav_settings // Highlight settings since we're in Settings activity

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this@Settings, HomePageActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_transactions -> {
                    startActivity(Intent(this@Settings, TransactionsActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_bunny -> {
                    startActivity(Intent(this@Settings, BunnyActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_budgetGoal -> {
                    startActivity(Intent(this@Settings, BudgetGoalsOverviewActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_settings -> true // Already on settings, do nothing
                else -> false
            }
        }
    }
}
