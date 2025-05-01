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

    // New MutableLiveData to track saving of minTotalBudgetGoal
    private val _minTotalBudgetGoalSaved = MutableLiveData<Boolean>()
    val minTotalBudgetGoalSaved: LiveData<Boolean> get() = _minTotalBudgetGoalSaved

    // Method to add budget including minTotalBudgetGoal
    fun addBudget(
        totalBudgetGoal: Double,
        budgetCategory: String,
        budgetAmount: Double,
        username: String,
        minTotalBudgetGoal: Double // New parameter for the minimum total budget goal
    ) {
        val budget = Budget(
            totalBudgetGoal = totalBudgetGoal,
            budgetCategory = budgetCategory,
            budgetAmount = budgetAmount,
            username = username,
            minTotalBudgetGoal = minTotalBudgetGoal
        )

        Log.d("BudgetViewModel", "Adding Budget: $budget")

        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.insertBudget(budget)
                // Optionally, save minTotalBudgetGoal separately if needed
                updateMinTotalBudgetGoal(username, minTotalBudgetGoal) // New line to update minTotalBudgetGoal
                loadBudgets(username)
                _budgetSaved.postValue(true)  // Notify success
                Log.d("BudgetViewModel", "Budget saved successfully!")
            } catch (e: Exception) {
                _budgetSaved.postValue(false)  // Notify failure
                Log.e("BudgetViewModel", "Error saving budget: ${e.message}")
            }
        }
    }

    // New method to save the minTotalBudgetGoal
    private fun updateMinTotalBudgetGoal(username: String, minTotalBudgetGoal: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.updateMinTotalBudgetGoalForUser(username, minTotalBudgetGoal)
                _minTotalBudgetGoalSaved.postValue(true)  // Notify success
                Log.d("BudgetViewModel", "Min Total Budget Goal saved successfully!")
            } catch (e: Exception) {
                _minTotalBudgetGoalSaved.postValue(false)  // Notify failure
                Log.e("BudgetViewModel", "Error saving Min Total Budget Goal: ${e.message}")
            }
        }
    }

    // Load budgets for a specific user
    fun loadBudgets(username: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val data = repository.getBudgetsForUser(username)
            _budgets.postValue(data)
        }
    }

    // Delete a specific budget
    fun deleteBudget(budgetId: Int, username: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteBudget(budgetId)
            loadBudgets(username)
        }
    }

    // New method to get the minTotalBudgetGoal for the user (if you need to display or use it elsewhere)
    fun getMinTotalBudgetGoalForUser(username: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val minGoal = repository.getMinTotalBudgetGoalForUser(username)
                Log.d("BudgetViewModel", "Min Total Budget Goal for $username: $minGoal")
            } catch (e: Exception) {
                Log.e("BudgetViewModel", "Error retrieving Min Total Budget Goal: ${e.message}")
            }
        }
    }
}