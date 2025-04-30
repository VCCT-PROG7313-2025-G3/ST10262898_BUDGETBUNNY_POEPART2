package com.fake.st10262898_budgetbunny_poepart2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class bugetGoalsPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_buget_goals_page)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Find the items on the xml:
        val btn_next = findViewById<Button>(R.id.next_button)
        val amountEditText = findViewById<EditText>(R.id.amountEditText)

        //Sends the user to the next page when clicking next
        btn_next.setOnClickListener {
            val intent = Intent(this,BunnyNameActivity::class.java)
            startActivity(intent)
        }
    }

}