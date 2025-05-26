package com.fake.st10262898_budgetbunny_poepart2.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fake.st10262898_budgetbunny_poepart2.data.BudgetFirestore
import com.fake.st10262898_budgetbunny_poepart2.data.BudgetFirestoreDao
import com.fake.st10262898_budgetbunny_poepart2.data.BudgetFirestoreRepository
import com.fake.st10262898_budgetbunny_poepart2.data.CategoryTotalFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BudgetViewModel(application: Application) : AndroidViewModel(application) {

    // Firestore implementations only
    private val repository = BudgetFirestoreRepository(BudgetFirestoreDao())

    private val _budgets = MutableLiveData<List<BudgetFirestore>>()
    val budgets: LiveData<List<BudgetFirestore>> get() = _budgets

    private val _budgetSaved = MutableLiveData<Boolean>()
    val budgetSaved: LiveData<Boolean> get() = _budgetSaved

    private val _minTotalBudgetGoalSaved = MutableLiveData<Boolean>()
    val minTotalBudgetGoalSaved: LiveData<Boolean> get() = _minTotalBudgetGoalSaved

    private val _updateStatus = MutableLiveData<Pair<Boolean, String?>>()
    val updateStatus: LiveData<Pair<Boolean, String?>> = _updateStatus


    fun addBudget(
        totalBudgetGoal: Double,
        budgetCategory: String,
        budgetAmount: Double,
        username: String,
        minTotalBudgetGoal: Double,
        budgetDate: Long,
        budgetIncome: Double
    ) {
        val budget = BudgetFirestore(
            // Don't set ID here - let Firestore generate it
            totalBudgetGoal = totalBudgetGoal,
            budgetCategory = budgetCategory,
            budgetAmount = budgetAmount,
            username = username,
            minTotalBudgetGoal = minTotalBudgetGoal,
            budgetDate = budgetDate,
            budgetIncome = budgetIncome
        )

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val newId = repository.insertBudget(budget) // Now returns the auto-generated ID
                repository.updateMinTotalBudgetGoalForUser(username, minTotalBudgetGoal)
                loadBudgets(username)
                _budgetSaved.postValue(true)
                Log.d("BudgetVM", "Budget saved with ID: $newId")
            } catch (e: Exception) {
                _budgetSaved.postValue(false)
                Log.e("BudgetVM", "Save failed", e)
            }
        }
    }

    fun loadBudgets(username: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val data = repository.getBudgetsForUser(username)
                _budgets.postValue(data)
                Log.d("BudgetViewModel", "Loaded ${data.size} budgets from Firestore")
            } catch (e: Exception) {
                Log.e("BudgetViewModel", "Firestore error loading budgets", e)
            }
        }
    }

    fun deleteBudget(id: String, username: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.deleteBudget(id)
                loadBudgets(username)
                Log.d("BudgetViewModel", "Deleted budget $id from Firestore")
            } catch (e: Exception) {
                Log.e("BudgetViewModel", "Firestore error deleting budget", e)
            }
        }
    }

    fun getMinTotalBudgetGoalForUser(username: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val minGoal = repository.getMinTotalBudgetGoalForUser(username)
                Log.d("BudgetViewModel", "Firestore Min Total Budget Goal: $minGoal")
            } catch (e: Exception) {
                Log.e("BudgetViewModel", "Firestore error getting min goal", e)
            }
        }
    }

    fun getCategoryTotals(username: String, onResult: (List<CategoryTotalFirestore>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val totals = repository.getCategoryTotals(username)
                onResult(totals)
            } catch (e: Exception) {
                Log.e("BudgetViewModel", "Error getting category totals", e)
                onResult(emptyList())
            }
        }
    }

    fun getCategoryTotalsByDateRange(
        username: String,
        startDate: Long,
        endDate: Long,
        onResult: (List<CategoryTotalFirestore>) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.getCategoryTotalsByDateRange(username, startDate, endDate) { totals ->
                    onResult(totals)
                }
            } catch (e: Exception) {
                Log.e("BudgetViewModel", "Error getting category totals by date", e)
                onResult(emptyList())
            }
        }
    }

    fun updateBudgetIncome(budgetId: String, amount: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val success = repository.updateBudgetIncome(budgetId, amount)
                _updateStatus.postValue(success to if (success) null else "Update failed silently")

                // Force refresh regardless of success
                _budgets.value?.firstOrNull()?.username?.let { loadBudgets(it) }
            } catch (e: Exception) {
                _updateStatus.postValue(false to e.message)
                Log.e("BudgetVM", "Update crashed", e)
            }
        }
    }
}