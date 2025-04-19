package com.fake.st10262898_budgetbunny_poepart2

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application)
{
    private val userDao = BudgetBunnyDatabase.getDatabase(application).userDao()
    private val repository = UserRepository(userDao)


    fun registerUser(username: String, password: String)
    {
        val user = User(username = username, password = password)
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(user)
        }
    }



    fun loginUser(username: String, password: String, onResult: (User?) -> Unit)
    {
        viewModelScope.launch(Dispatchers.IO) {
            val user = repository.getUser(username, password)
            onResult(user)
        }

    }
}