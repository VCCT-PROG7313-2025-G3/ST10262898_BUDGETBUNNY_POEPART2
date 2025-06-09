package com.fake.st10262898_budgetbunny_poepart2.data

import com.google.firebase.firestore.DocumentId

data class BudgetFirestore(
    var id: String = "", //Firestore will auto generate this field
    val totalBudgetGoal: Double = 0.0, //These are set to 0 because they will be added by code
    val minTotalBudgetGoal: Double = 0.0,
    val budgetCategory: String = "",
    val budgetAmount: Double = 0.0,
    val budgetDate: Long = 0L,
    val budgetIncome: Double = 0.0,
    val username: String = ""
) {
    // Helper function to convert to Room entity
    fun toRoomBudget(): Budget {
        return Budget(
            id = 0, // Room will auto-generate this
            totalBudgetGoal = totalBudgetGoal,
            minTotalBudgetGoal = minTotalBudgetGoal,
            budgetCategory = budgetCategory,
            budgetAmount = budgetAmount,
            budgetDate = budgetDate,
            budgetIncome = budgetIncome,
            username = username
        )
    }

    companion object {
        // Helper function to convert from Room entity
        fun fromRoomBudget(budget: Budget): BudgetFirestore {
            return BudgetFirestore(
                id = budget.id.toString(),
                totalBudgetGoal = budget.totalBudgetGoal,
                minTotalBudgetGoal = budget.minTotalBudgetGoal,
                budgetCategory = budget.budgetCategory,
                budgetAmount = budget.budgetAmount,
                budgetDate = budget.budgetDate,
                budgetIncome = budget.budgetIncome,
                username = budget.username
            )
        }
    }
}