package com.fake.st10262898_budgetbunny_poepart2

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.collection.emptyLongSet
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Observer

class signUpPage : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up_page)


        //Declaring the variables so we can add functionality to them:
        val et_username = findViewById<EditText>(R.id.et_username)
        val et_password = findViewById<EditText>(R.id.et_password)
        val btn_signUp = findViewById<Button>(R.id.btn_signUp)
        val userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)


        //This method now will add the functionality to the button (therefore saving details of user on database):
        btn_signUp.setOnClickListener {
            val username = et_username.text.toString()
            val password = et_password.text.toString()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                userViewModel.registerUser(username, password)

                //observe for the result:
                userViewModel.registrationResult.observe(this, Observer<Boolean> { isSuccess ->
                    if (isSuccess) {
                        Toast.makeText(this, "Sign up successful", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Sign up failed", Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                Toast.makeText(this, "PLease fill in all the required fields", Toast.LENGTH_SHORT)
                    .show()
            }

        }
    }
}