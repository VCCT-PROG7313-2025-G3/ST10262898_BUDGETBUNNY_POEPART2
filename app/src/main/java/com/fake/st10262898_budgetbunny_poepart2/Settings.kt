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

        // This is for the navigation bar
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)


        bottomNavigationView.selectedItemId = R.id.nav_budgetGoal

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Takes user to home page
                    val intent = Intent(this, HomePageActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(0, 0) // Smooth transition
                    true
                }
                R.id.nav_transactions -> {
                    // Takes user to transactions page
                    val intent = Intent(this, TransactionsActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_budgetGoal -> {
                    // Already on budget goals page, do nothing
                    true
                }
                R.id.nav_settings -> {
                    // Takes user to settings page
                    val intent = Intent(this, Settings::class.java)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }

    }
}
