package com.fake.st10262898_budgetbunny_poepart2.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ExpenseDao {

    @Insert
    suspend fun insertExpense(expense: Expense)

    @Query("SELECT * FROM expense_table WHERE username = :username")
    suspend fun getExpenseForUser(username: String): List<Expense>

    @Query("DELETE FROM expense_table WHERE id = :id")
    suspend fun deleteExpense(id: Int)

    @Query("SELECT * FROM expense_table WHERE username = :username AND expenseDate BETWEEN :startDate AND :endDate ORDER BY expenseDate DESC")
    suspend fun getExpensesBetweenDates(username: String, startDate: Long, endDate: Long): List<Expense>

    @Query("SELECT * FROM expense_table WHERE Id = :id LIMIT 1")
    suspend fun getExpenseById(id: Int): Expense

    @Update
    suspend fun updateExpense(expense: Expense)

}