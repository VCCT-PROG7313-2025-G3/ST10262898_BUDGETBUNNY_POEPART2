package com.fake.st10262898_budgetbunny_poepart2

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class CustomExpense : AppCompatActivity() {
    private lateinit var editExpenseCustom: EditText
    private lateinit var nextButton: Button
    private lateinit var selectedIcon: ImageView
    private var chosenIconResId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_expense)

        // Initialize the views
        editExpenseCustom = findViewById(R.id.editExpenseCustom)
        nextButton =findViewById(R.id.button)
        selectedIcon = findViewById(R.id.imageView3)

        val iconButtons = listOf(
            R.id.imageButton to R.drawable.flower,
            R.id.imageButton2 to R.drawable.phone,
            R.id.imageButton3 to R.drawable.pin,
            R.id.imageButton4 to R.drawable.sun,
            R.id.imageButton5 to R.drawable.plane
        )

        iconButtons.forEach { (buttonId, iconRes) ->
            val iconButton = findViewById<ImageButton>(buttonId)
            iconButton.setOnClickListener {
                selectedIcon.setImageResource(iconRes) // Show selected icon
                chosenIconResId = iconRes // Store for saving
            }
        }

        nextButton.setOnClickListener {
            val expenseName = editExpenseCustom.text.toString()

            if (expenseName.isBlank()) {
                Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (chosenIconResId == null) {
                Toast.makeText(this, "Please choose an icon", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
//database and saving work from here




//og code for reference or safety
            enableEdgeToEdge()
            setContentView(R.layout.activity_custom_expense)
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }
    }
}