package com.fake.st10262898_budgetbunny_poepart2

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application)
{
    private val userDao = BudgetBunnyDatabase.getDatabase(application).userDao()
    private val repository = UserRepository(userDao)

    //This variable is going to be used to be able to check the registration result:
    val registrationResult = MutableLiveData<Boolean>()

    fun registerUser(username: String, password: String)
    {
        val user = User(username = username, password = password)


        viewModelScope.launch(Dispatchers.IO) {
            try{
                repository.insert(user)
                registrationResult.postValue(true)
            }
            catch (e: Exception)
            {
                registrationResult.postValue(false)
            }
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