package com.fake.st10262898_budgetbunny_poepart2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class SignUpThanks : AppCompatActivity() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_sign_up_thanks)

            // Reference to the button
            val nextButton = findViewById<Button>(R.id.nextButtonThanks)

            // condition (replace with your real check if needed)
            val signUpCompleted = true

            // Set what happens when "Next" is clicked
            nextButton.setOnClickListener {
                if (signUpCompleted) {
                    // Go to next screen if sign-up is completed
                    val intent = Intent(this, ExpenseList::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Stay here and show message if not completed
                    Toast.makeText(this, "Please finish signing up", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }