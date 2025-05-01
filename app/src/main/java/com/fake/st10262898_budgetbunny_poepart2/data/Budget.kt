package com.fake.st10262898_budgetbunny_poepart2.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budget_table")
data class Budget (

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val totalBudgetGoal: Double,
    val minTotalBudgetGoal: Double,
    val budgetCategory: String,
    val budgetAmount: Double,
    val budgetDate: Long,
    val budgetIncome: Double,
    val username: String // This will link the budget category to a user


)
