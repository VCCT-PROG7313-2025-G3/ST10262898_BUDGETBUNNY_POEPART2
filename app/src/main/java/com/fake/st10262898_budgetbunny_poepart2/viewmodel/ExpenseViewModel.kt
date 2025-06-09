package com.fake.st10262898_budgetbunny_poepart2.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fake.st10262898_budgetbunny_poepart2.data.BudgetBunnyDatabase
import com.fake.st10262898_budgetbunny_poepart2.data.Expense
import com.fake.st10262898_budgetbunny_poepart2.data.ExpenseFirebase
import com.fake.st10262898_budgetbunny_poepart2.data.ExpenseRepository
import com.fake.st10262898_budgetbunny_poepart2.data.FirestoreExpenseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ExpenseViewModel : ViewModel() {

    //Allows Firestore to be used in this class
    private val repository = FirestoreExpenseRepository()

    private val _expenses = MutableLiveData<List<ExpenseFirebase>>()
    val expenses: LiveData<List<ExpenseFirebase>> get() = _expenses

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    //Allows for expenses to be entered into the datbase
    fun addExpense(expense: ExpenseFirebase) {
        repository.addExpense(expense) { success ->
            if (success) loadExpenses(expense.username)
        }
    }

    //Reads expenses off the database
    fun loadExpenses(username: String) {
        repository.getExpenses(username) { result ->
            _expenses.postValue(result)
        }
    }

    //Now users can delete their expenses which they do not want recorded
    fun deleteExpense(expenseId: String, username: String) {
        repository.deleteExpense(expenseId) { success ->
            if (success) loadExpenses(username)
        }
    }

    //Select a date and be able to see expenses between that
    fun loadExpensesBetweenDates(username: String, startDate: Long, endDate: Long) {
        repository.getExpensesBetweenDates(username, startDate, endDate) { expenses ->
            if (expenses != null) {
                _expenses.postValue(expenses)
            } else {
                _errorMessage.postValue("Error loading expenses")
            }
        }
    }


}
