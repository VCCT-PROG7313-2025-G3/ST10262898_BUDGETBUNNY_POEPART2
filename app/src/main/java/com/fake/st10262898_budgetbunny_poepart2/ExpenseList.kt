package com.fake.st10262898_budgetbunny_poepart2

import android.content.Intent
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



            val nextButton = findViewById<Button>(R.id.btn_next)
            val billsButton = findViewById<ImageButton>(R.id.bills)
            val carButton = findViewById<ImageButton>(R.id.carButton)
            val emergenciesButton = findViewById<ImageButton>(R.id.emergenciesButton)
            val entertainmentButton = findViewById<ImageButton>(R.id.entertainmentButton)
            val foodButton = findViewById<ImageButton>(R.id.foodButton)
            val hobbiesButton = findViewById<ImageButton>(R.id.hobbiesButton)
            val rentButton = findViewById<ImageButton>(R.id.rentButton)
            val customButton = findViewById<ImageButton>(R.id.customButton)

            nextButton.setOnClickListener {
                val intent = Intent(this,bugetGoalsPage::class.java)
                startActivity(intent)
            }

        customButton.setOnClickListener {
            val intent = Intent(this@ExpenseList, CustomExpense::class.java)
            startActivity(intent)
        }

        //This is a single on click listener for all image buttons
        val goToMonthlyPayment = {
            val intent = Intent(this@ExpenseList, MonthlyPayment::class.java)
            startActivity(intent)
        }


        billsButton.setOnClickListener { goToMonthlyPayment() }
        carButton.setOnClickListener { goToMonthlyPayment() }
        emergenciesButton.setOnClickListener { goToMonthlyPayment() }
        entertainmentButton.setOnClickListener { goToMonthlyPayment() }
        foodButton.setOnClickListener { goToMonthlyPayment() }
        hobbiesButton.setOnClickListener { goToMonthlyPayment() }
        rentButton.setOnClickListener { goToMonthlyPayment() }



        // Padding handling only
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



    }
}