package com.fake.st10262898_budgetbunny_poepart2.data

class ExpenseRepository (private val expenseDao: ExpenseDao){

    suspend fun insertExpense(expense: Expense) {
        expenseDao.insertExpense(expense)
    }

    suspend fun getExpensesForUser(username: String): List<Expense> {
        return expenseDao.getExpenseForUser(username)
    }

    suspend fun deleteExpense(id: Int) {
        expenseDao.deleteExpense(id)
    }

    suspend fun getExpensesBetweenDates(username: String, startDate: Long, endDate: Long): List<Expense> {
        return expenseDao.getExpensesBetweenDates(username, startDate, endDate)
    }

}