package com.fake.st10262898_budgetbunny_poepart2.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ExpenseDao {

    @Insert
    suspend fun insertExpense(expense: Expense)

    @Query("SELECT * FROM expense_table WHERE username = :username")
    suspend fun getExpenseForUser(username: String): List<Expense>

    @Query("DELETE FROM expense_table WHERE id = :id")
    suspend fun deleteExpense(id: Int)

}