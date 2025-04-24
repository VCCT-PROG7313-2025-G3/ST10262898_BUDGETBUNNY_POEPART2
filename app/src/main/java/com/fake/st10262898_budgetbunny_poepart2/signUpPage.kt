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
import android.content.Intent
import android.widget.TextView

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
            val passwordRequirementsText = findViewById<TextView>(R.id.passwordListRequirements)

            //This is the method which will ensure all the restrictions which are needed are met:
            fun isPasswordValid(password: String): Boolean {
                val hasMinLength = password.length >= 8 //Checks length of password is equal or more than 8
                val hasSymbol = password.any { !it.isLetterOrDigit() } //Checks to ensure that there is value which is not a letter or digit
                val hasNumber = password.any { it.isDigit() } //Checks to ensure that there is a digit

                return hasMinLength && hasSymbol && hasNumber //Returns the boolean confirming if all requirements have been met
            }



            //This if statement will link the restrictions to the actualy input user inputs (and turn text red if wrong)
            if (username.isNotEmpty() && password.isNotEmpty())
            {
                        if (!isPasswordValid(password)) { //If password does not meet the requirments this is what will happen:

                            //This prompt will appear
                            Toast.makeText(this, "Password must be at least 8 characters long and contain a symbol and a number", Toast.LENGTH_LONG).show()

                            //This will make the text turn red
                            passwordRequirementsText.setTextColor(resources.getColor(R.color.mordant_red_19))
                        }
                        else
                        { //This is what happens when the password is valid

                            passwordRequirementsText.setTextColor(resources.getColor(android.R.color.white)) //It turns text back to white
                            userViewModel.registerUser(username, password) //The line saving the user details to the database is now here

                            //Observe for result
                            userViewModel.registrationResult.observe(this, Observer<Boolean> { isSuccess ->
                                if (isSuccess) {
                                    Toast.makeText(this, "Sign up successful", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this, MainActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                                else
                                {
                                    Toast.makeText(this, "Sign up failed", Toast.LENGTH_SHORT).show()
                                }
                            })
                        }//
            }
            else
            {
                Toast.makeText(this, "Please fill in all the required fields", Toast.LENGTH_SHORT).show()
            }




        }
    }
}

