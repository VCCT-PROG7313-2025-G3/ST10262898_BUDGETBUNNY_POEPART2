package com.fake.st10262898_budgetbunny_poepart2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ExpenseList : AppCompatActivity() {


    private fun openMonthlyPayment(expenseCategory: String) {
        val intent = Intent(this, MonthlyPayment::class.java)
        intent.putExtra("EXPENSE_CATEGORY", expenseCategory)
        startActivity(intent)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_expense_list)


        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val username = sharedPreferences.getString("username", null)

        if (username == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        } else {
            Toast.makeText(this, "Welcome, $username!", Toast.LENGTH_SHORT).show()
        }



            val nextButton = findViewById<Button>(R.id.btn_next)
            val billsButton = findViewById<ImageButton>(R.id.bills)
            val carButton = findViewById<ImageButton>(R.id.carButton)
            val emergenciesButton = findViewById<ImageButton>(R.id.emergenciesButton)
            val entertainmentButton = findViewById<ImageButton>(R.id.entertainmentButton)
            val foodButton = findViewById<ImageButton>(R.id.foodButton)
            val hobbiesButton = findViewById<ImageButton>(R.id.hobbiesButton)
            val rentButton = findViewById<ImageButton>(R.id.rentButton)


        nextButton.setOnClickListener {
            val intent = Intent(this, bugetGoalsPage::class.java)
            val username = sharedPreferences.getString("username", "") ?: ""
            intent.putExtra("username", username) // Pass the username to the next activity
            startActivity(intent)
        }



        //this ensures that when an image button is clicked that in the table this name is saved:
        billsButton.setOnClickListener { openMonthlyPayment("Bills") }
        carButton.setOnClickListener { openMonthlyPayment("Car") }
        emergenciesButton.setOnClickListener { openMonthlyPayment("Emergencies") }
        entertainmentButton.setOnClickListener { openMonthlyPayment("Entertainment") }
        foodButton.setOnClickListener { openMonthlyPayment("Food") }
        hobbiesButton.setOnClickListener { openMonthlyPayment("Hobbies") }
        rentButton.setOnClickListener { openMonthlyPayment("Rent") }





        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



    }



}