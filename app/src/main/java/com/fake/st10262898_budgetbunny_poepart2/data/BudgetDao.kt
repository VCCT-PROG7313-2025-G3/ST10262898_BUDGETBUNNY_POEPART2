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


    @Query("SELECT minTotalBudgetGoal FROM budget_table WHERE username = :username")
    suspend fun getMinTotalBudgetGoalForUser(username: String): Double

    @Query("UPDATE budget_table SET minTotalBudgetGoal = :minTotalBudgetGoal WHERE username = :username")
    suspend fun updateMinTotalBudgetGoalForUser(username: String, minTotalBudgetGoal: Double)


    @Query("DELETE FROM budget_table WHERE id = :id")
    suspend fun deleteBudget(id: Int)
}