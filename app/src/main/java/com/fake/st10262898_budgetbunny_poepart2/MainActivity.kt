package com.fake.st10262898_budgetbunny_poepart2

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        //Declaring the button so we can add functionality to it:
        val btn_signUp = findViewById<ImageButton>(R.id.SignUp)

        //This allows the buttons to be clickable and there to be functionality:
        btn_signUp.setOnClickListener {
            //When the user clicks the button, it will take them to the sign up page:
            val intent = Intent(this, signUpPage::class.java)
            startActivity(intent)
        }
    }
}
