package com.fake.st10262898_budgetbunny_poepart2

import android.os.Bundle
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity


class BunnyNameActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_bunny_name)



        //Find the edit text which contains the bunny's name:
        val bunnyName = findViewById<EditText>(R.id.bunnyName)
    }
}