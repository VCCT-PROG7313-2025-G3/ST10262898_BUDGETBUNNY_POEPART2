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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class BudgetViewModel(application: Application) : AndroidViewModel(application) {

    // Firestore implementations:
    private val repository = BudgetFirestoreRepository(BudgetFirestoreDao())
    private val firestore = FirebaseFirestore.getInstance()

    //Implementation for coins:
    private val _userCoins = MutableLiveData<Int>()
    val userCoins: LiveData<Int> get() = _userCoins

    //Implementation for budgets
    private val _budgets = MutableLiveData<List<BudgetFirestore>>()
    val budgets: LiveData<List<BudgetFirestore>> get() = _budgets

    private val _budgetSaved = MutableLiveData<Boolean>()
    val budgetSaved: LiveData<Boolean> get() = _budgetSaved

    private val _minTotalBudgetGoalSaved = MutableLiveData<Boolean>()
    val minTotalBudgetGoalSaved: LiveData<Boolean> get() = _minTotalBudgetGoalSaved

    private val _updateStatus = MutableLiveData<Pair<Boolean, String?>>()
    val updateStatus: LiveData<Pair<Boolean, String?>> = _updateStatus


    //This allows the app to add a budget into Firestore
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
                val newId = repository.insertBudget(budget)
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

    //This allows the application to be able to get the budgets in the database
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

    //This allows a user to be able to delete budgets they do not need anymore:
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

    //Gets the min goal that the user has entered:
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

    //Gets the totals for the categories for a budgets for a specific user
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

    //This does the same as the method above but now a user can also filter by date
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

    //This allows a user to be able to enter update their income for budget entries
    fun updateBudgetIncome(budgetId: String, amount: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val success = repository.updateBudgetIncome(budgetId, amount)
                _updateStatus.postValue(success to if (success) null else "Update failed silently")


                _budgets.value?.firstOrNull()?.username?.let { loadBudgets(it) }
            } catch (e: Exception) {
                _updateStatus.postValue(false to e.message)
                Log.e("BudgetVM", "Update crashed", e)
            }
        }
    }

    //This gets teh total income for a user across all budgets
    private suspend fun getTotalIncomeForUser(username: String): Double {
        return try {
            val querySnapshot = firestore.collection("Budgets")
                .whereEqualTo("username", username)
                .get()
                .await()

            val totalIncome = querySnapshot.documents.sumOf { doc ->
                doc.getDouble("budgetIncome") ?: 0.0
            }
            totalIncome
        } catch (e: Exception) {
            Log.e("Firestore", "Failed to sum budgetIncome", e)
            0.0
        }
    }

    //This turns total income into coins (R10 = 1)
    fun calculateAndUpdateCoins(username: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.calculateAndUpdateCoins(username)
                loadUserCoins(username)
            } catch (e: Exception) {
                Log.e("BudgetVM", "Error updating coins", e)
            }
        }
    }

    //After creating coins now the application can load them
    fun loadUserCoins(username: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val coins = repository.getUserCoins(username)
                _userCoins.postValue(coins)
            } catch (e: Exception) {
                Log.e("BudgetVM", "Error loading coins", e)
            }
        }
    }

    //This is for incase coins decrease or increase
    fun refreshCoins(username: String) {
        viewModelScope.launch {
            try {
                Log.d("CoinDebug", "Refreshing coins for $username")
                val coins = repository.getUserCoins(username)
                Log.d("CoinDebug", "Retrieved coins: $coins")
                _userCoins.postValue(coins)
            } catch (e: Exception) {
                Log.e("BudgetVM", "Error refreshing coins", e)
            }
        }
    }


}