package com.fake.st10262898_budgetbunny_poepart2

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.room.Room
import androidx.lifecycle.lifecycleScope
import android.widget.Toast
import android.widget.Button
import android.widget.EditText
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope

class MainActivity : AppCompatActivity()
{

    private lateinit var db: BudgetBunnyDatabase

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        //Declaring the button so we can add functionality to it:
        val usernameEditText = findViewById<EditText>(R.id.inputUserName)
        val passwordEditText = findViewById<EditText>(R.id.inputPassword)
        val btn_login = findViewById<Button>(R.id.Login)
        val btn_signUp = findViewById<ImageButton>(R.id.SignUp)


        // Initialize database
        db = Room.databaseBuilder(
            applicationContext,
            BudgetBunnyDatabase::class.java,
            "user_database"
        ).build()


        //This allows the buttons to be clickable and there to be functionality:
        btn_signUp.setOnClickListener{
            //When the user clicks the button, it will take them to the sign up page:
            val intent = Intent(this, signUpPage::class.java)
            startActivity(intent)
        }



        btn_login.setOnClickListener{
            val username = usernameEditText.text.toString() //Saves what the user has entered to this variable
            val password = passwordEditText.text.toString()

            //This allows the user to login if they have signed up
            lifecycleScope.launch {
                val user = db.userDao().getUser(username, password) //Fetches the user from the database
                if(user != null)
                {
                    //If compiler can find the user this is what happens
                    Toast.makeText(applicationContext, "Login successful!", Toast.LENGTH_SHORT).show()
                }
                else
                {
                    //If compiler cannot find the users details this is what happens
                    Toast.makeText(applicationContext, "Invalid username or password", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}