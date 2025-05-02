package com.fake.st10262898_budgetbunny_poepart2

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SignUpReward : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up_reward)



        val closeButton = findViewById<Button>(R.id.closeButton)

        closeButton.setOnClickListener {

            val intent = Intent(this, MainActivity::class.java)

            // Clear the back stack so user can't return to this reward screen
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK

            // Start the activity and finish current one
            startActivity(intent)
            finish()

            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }




        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}

