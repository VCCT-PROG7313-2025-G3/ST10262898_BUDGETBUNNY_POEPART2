package com.fake.st10262898_budgetbunny_poepart2.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query


@Dao
interface BudgetDao {

    @Insert
    suspend fun insertBudget(budget: Budget)

    @Query("SELECT * FROM budget_table WHERE username = :username")
    suspend fun getBudgetForUser(username: String): List<Budget>

    @Query("DELETE FROM budget_table WHERE id = :id")
    suspend fun deleteBudget(id: Int)
}