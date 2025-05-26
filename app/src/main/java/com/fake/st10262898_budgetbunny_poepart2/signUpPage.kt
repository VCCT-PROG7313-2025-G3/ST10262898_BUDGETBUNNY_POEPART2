package com.fake.st10262898_budgetbunny_poepart2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class signUpPage : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up_page)

        auth = FirebaseAuth.getInstance()

        val et_username = findViewById<EditText>(R.id.et_username)
        val et_password = findViewById<EditText>(R.id.et_password)
        val btn_signUp = findViewById<Button>(R.id.btn_signUp)
        val passwordRequirementsText = findViewById<TextView>(R.id.passwordListRequirements)

        fun isPasswordValid(password: String): Boolean {
            val hasMinLength = password.length >= 8
            val hasSymbol = password.any { !it.isLetterOrDigit() }
            val hasNumber = password.any { it.isDigit() }
            return hasMinLength && hasSymbol && hasNumber
        }

        btn_signUp.setOnClickListener {
            val username = et_username.text.toString().trim()
            val password = et_password.text.toString().trim()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                if (!isPasswordValid(password)) {
                    Toast.makeText(this, "Password must be at least 8 characters long and contain a symbol and a number", Toast.LENGTH_LONG).show()
                    passwordRequirementsText.setTextColor(resources.getColor(R.color.mordant_red_19))
                } else {
                    passwordRequirementsText.setTextColor(resources.getColor(android.R.color.white))

                    // Firebase requires email for signup, so we create a fake email here:
                    val email = "$username@fakeemail.com"

                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                //Saves username to SharedPreferences
                                val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                                val editor = sharedPreferences.edit()
                                editor.putString("username", username)
                                editor.apply()

                                Toast.makeText(this, "Sign up successful", Toast.LENGTH_SHORT).show()

                                val intent = Intent(this, SignUpThanks::class.java)
                                intent.putExtra("username", username)
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(this, "Sign up failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                }
            } else {
                Toast.makeText(this, "Please fill in all the required fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
