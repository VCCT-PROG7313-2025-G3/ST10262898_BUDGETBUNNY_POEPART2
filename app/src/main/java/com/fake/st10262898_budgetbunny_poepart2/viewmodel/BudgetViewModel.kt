package com.fake.st10262898_budgetbunny_poepart2.viewmodel

import android.app.Application
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

    fun addBudget(totalBudgetGoal: Double, budgetCategory: String, budgetAmount: Double, username: String) {
        val budget = Budget(
            totalBudgetGoal = totalBudgetGoal,
            budgetCategory = budgetCategory,
            budgetAmount = budgetAmount,
            username = username
        )
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertBudget(budget)
            loadBudgets(username)
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