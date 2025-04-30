package com.fake.st10262898_budgetbunny_poepart2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity


class BunnyNameActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_bunny_name)


        //Find the the elements on the page
        val bunnyName = findViewById<EditText>(R.id.bunnyName)
        val buttonNext = findViewById<Button>(R.id.btn_next)

        
        buttonNext.setOnClickListener {
            val intent = Intent(this,SignUpReward::class.java)
            startActivity(intent)
        }
    }
}