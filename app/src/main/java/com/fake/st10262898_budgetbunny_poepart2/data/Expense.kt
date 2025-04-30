package com.fake.st10262898_budgetbunny_poepart2.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expense_table")
class Expense (

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val expenseName: String,
    val expenseAmount: Double,
    val username: String //This will be how the user is linked to an expense.
)