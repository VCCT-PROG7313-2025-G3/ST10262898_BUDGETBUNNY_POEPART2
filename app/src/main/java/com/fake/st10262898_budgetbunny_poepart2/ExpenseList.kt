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


    private fun openMonthlyPayment(expenseName: String) {
        val intent = Intent(this, MonthlyPayment::class.java)
        intent.putExtra("EXPENSE_NAME", expenseName)
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_expense_list)


        val username = intent.getStringExtra("USERNAME") ?: ""

        // Save the username in SharedPreferences
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("USERNAME", username)
        editor.apply()

        // Use the username as needed in ExpenseList
        Toast.makeText(this, "Welcome, $username!", Toast.LENGTH_SHORT).show()

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
            val intent = Intent(this, bugetGoalsPage::class.java)
            val username = sharedPreferences.getString("USERNAME", "") ?: ""
            intent.putExtra("USERNAME", username)  // Pass the username to the next activity
            startActivity(intent)
        }

        customButton.setOnClickListener {
            val intent = Intent(this@ExpenseList, CustomExpense::class.java)
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
        customButton.setOnClickListener { openMonthlyPayment("Custom") }


        // Padding handling only
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



    }



}