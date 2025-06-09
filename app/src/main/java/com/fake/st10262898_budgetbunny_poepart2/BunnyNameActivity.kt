package com.fake.st10262898_budgetbunny_poepart2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class BunnyNameActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_bunny_name)

        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()

        // Find the elements on the page
        val bunnyNameEditText = findViewById<EditText>(R.id.bunnyName)
        val buttonNext = findViewById<Button>(R.id.btn_next)

        buttonNext.setOnClickListener {
            val bunnyName = bunnyNameEditText.text.toString().trim()

            if (bunnyName.isNotEmpty()) {
                val userId = auth.currentUser?.uid

                if (userId != null) {
                    // Save to Firestore
                    val userDoc = firestore.collection("users").document(userId)
                    val data = hashMapOf("bunnyName" to bunnyName)
                    userDoc.set(data, SetOptions.merge())
                        .addOnSuccessListener {

                            getSharedPreferences("UserPrefs", MODE_PRIVATE).edit()
                                .putString("bunnyName", bunnyName)
                                .apply()

                            Toast.makeText(this, "Bunny named!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, SignUpReward::class.java))
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to save bunny name", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter a name for your bunny", Toast.LENGTH_SHORT).show()
            }
        }
    }
}