package com.fake.st10262898_budgetbunny_poepart2.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fake.st10262898_budgetbunny_poepart2.data.BudgetBunnyDatabase
import com.fake.st10262898_budgetbunny_poepart2.data.Expense
import com.fake.st10262898_budgetbunny_poepart2.data.ExpenseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ExpenseViewModel {

    class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

        private val expenseDao = BudgetBunnyDatabase.getDatabase(application).expenseDao()
        private val repository = ExpenseRepository(expenseDao)

        private val _expenses = MutableLiveData<List<Expense>>()
        val expenses: LiveData<List<Expense>> get() = _expenses

        fun addExpense(
            expenseName: String,
            expenseAmount: Double,
            username: String,
            expenseCategory: String?,
            expenseDate: Long,
            expenseImage: ByteArray?
        ) {
            val expense = Expense(
                expenseName = expenseName,
                expenseAmount = expenseAmount,
                username = username,
                expenseCategory = expenseCategory,
                expenseDate = expenseDate,
                expenseImage = expenseImage
            )
            viewModelScope.launch(Dispatchers.IO) {
                repository.insertExpense(expense)
                loadExpenses(username)
            }
        }

        fun loadExpenses(username: String) {
            viewModelScope.launch(Dispatchers.IO) {
                val data = repository.getExpensesForUser(username)
                _expenses.postValue(data)
            }
        }

        fun deleteExpense(expenseId: Int, username: String) {
            viewModelScope.launch(Dispatchers.IO) {
                repository.deleteExpense(expenseId)
                loadExpenses(username)
            }
        }
    }
}