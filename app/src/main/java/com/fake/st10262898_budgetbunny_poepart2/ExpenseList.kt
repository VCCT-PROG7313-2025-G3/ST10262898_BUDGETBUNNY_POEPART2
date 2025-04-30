package com.fake.st10262898_budgetbunny_poepart2

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ExpenseList : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_expense_list)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val nextButton = findViewById<Button>(R.id.button)
            val billsButton = findViewById<ImageButton>(R.id.bills)
            val carButton = findViewById<ImageButton>(R.id.carButton)
            val emergenciesButton = findViewById<ImageButton>(R.id.emergenciesButton)
            val entertainmentButton = findViewById<ImageButton>(R.id.entertainmentButton)
            val foodButton = findViewById<ImageButton>(R.id.foodButton)
            val hobbiesButton = findViewById<ImageButton>(R.id.hobbiesButton)
            val rentButton = findViewById<ImageButton>(R.id.rentButton)
            val customButton = findViewById<ImageButton>(R.id.customButton)





            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets


        }
    }
}