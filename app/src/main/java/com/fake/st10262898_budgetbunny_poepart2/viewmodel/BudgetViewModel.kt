package com.fake.st10262898_budgetbunny_poepart2.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fake.st10262898_budgetbunny_poepart2.data.Budget
import com.fake.st10262898_budgetbunny_poepart2.data.BudgetBunnyDatabase
import com.fake.st10262898_budgetbunny_poepart2.data.BudgetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class BudgetViewModel (application: Application) : AndroidViewModel(application) {

    private val budgetDao = BudgetBunnyDatabase.getDatabase(application).budgetDao()
    private val repository = BudgetRepository(budgetDao)

    private val _budgets = MutableLiveData<List<Budget>>()
    val budgets: LiveData<List<Budget>> get() = _budgets


    private val _budgetSaved = MutableLiveData<Boolean>()
    val budgetSaved: LiveData<Boolean> get() = _budgetSaved

    fun addBudget(totalBudgetGoal: Double, budgetCategory: String, budgetAmount: Double, username: String) {
        val budget = Budget(
            totalBudgetGoal = totalBudgetGoal,
            budgetCategory = budgetCategory,
            budgetAmount = budgetAmount,
            username = username
        )

        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.insertBudget(budget)
                loadBudgets(username)
                _budgetSaved.postValue(true)  // Notify success
            } catch (e: Exception) {
                _budgetSaved.postValue(false)  // Notify failure
                // Log the error for debugging purposes
                Log.e("BudgetViewModel", "Error saving budget: ${e.message}")
            }
        }
    }

    fun loadBudgets(username: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val data = repository.getBudgetsForUser(username)
            _budgets.postValue(data)
        }
    }

    fun deleteBudget(budgetId: Int, username: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteBudget(budgetId)
            loadBudgets(username)
        }
    }
}