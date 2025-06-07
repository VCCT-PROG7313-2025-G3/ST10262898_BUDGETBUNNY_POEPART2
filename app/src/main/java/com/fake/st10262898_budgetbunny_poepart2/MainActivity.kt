package com.fake.st10262898_budgetbunny_poepart2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        //Some extracoding for firebase:
        FirebaseFirestore.getInstance().firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()

        auth = FirebaseAuth.getInstance()

        val usernameEditText = findViewById<EditText>(R.id.inputUserName)
        val passwordEditText = findViewById<EditText>(R.id.inputPassword)
        val btn_login = findViewById<Button>(R.id.Login)
        val btn_signUp = findViewById<ImageButton>(R.id.SignUp)

        btn_signUp.setOnClickListener {
            val intent = Intent(this, signUpPage::class.java)
            startActivity(intent)
        }

        btn_login.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val email = "$username@fakeemail.com"  // Same fake email logic as signup

            if (username.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(applicationContext, "Login successful!", Toast.LENGTH_SHORT).show()

                            val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            editor.putString("username", username)
                            editor.apply()
                            val username = sharedPreferences.getString("username", null)
                            Toast.makeText(this, "Logged in as: $username", Toast.LENGTH_SHORT).show()

                            val intent = Intent(this@MainActivity, HomePageActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(applicationContext, "Invalid username or password", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
