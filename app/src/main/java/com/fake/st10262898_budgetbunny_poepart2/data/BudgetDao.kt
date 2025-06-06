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

    @Query("SELECT * FROM budget_table WHERE username = :username AND budgetDate BETWEEN :start AND :end")
    suspend fun getBudgetsInDateRange(username: String, start: Long, end: Long): List<Budget>


    @Query("DELETE FROM budget_table WHERE id = :id")
    suspend fun deleteBudget(id: Int)

    //This is to be able to display the total amount spent on each category
    @Query("""
        SELECT budgetCategory, SUM(budgetAmount) as total 
        FROM budget_table 
        WHERE username = :username 
        AND budgetDate BETWEEN :startDate AND :endDate 
        GROUP BY budgetCategory
    """)
    suspend fun getCategoryTotalsForDateRange(
        username: String,
        startDate: Long,
        endDate: Long
    ): List<CategoryTotal>

    @Query("SELECT * FROM budget_table WHERE username = :username")
    suspend fun getAllBudgetsForUser(username: String): List<Budget>

    @Query("SELECT * FROM budget_table")
    fun getAllBudgets(): List<Budget>

    @Query("""
        SELECT budgetCategory, SUM(budgetAmount) as total 
        FROM budget_table 
        WHERE username = :username 
        GROUP BY budgetCategory
    """)
    suspend fun getCategoryTotals(username: String): List<CategoryTotal>

}