package com.fake.st10262898_budgetbunny_poepart2.data

class BudgetRepository(private val budgetDao: BudgetDao){

    suspend fun insertBudget(budget: Budget) {
        budgetDao.insertBudget(budget)
    }

    suspend fun getBudgetsForUser(username: String): List<Budget> {
        return budgetDao.getBudgetForUser(username)
    }

    suspend fun deleteBudget(id: Int) {
        budgetDao.deleteBudget(id)
    }

    // New method to update the minTotalBudgetGoal for a specific user
    suspend fun updateMinTotalBudgetGoalForUser(username: String, minTotalBudgetGoal: Double) {
        budgetDao.updateMinTotalBudgetGoalForUser(username, minTotalBudgetGoal)
    }

    // New method to get the minTotalBudgetGoal for a specific user
    suspend fun getMinTotalBudgetGoalForUser(username: String): Double {
        return budgetDao.getMinTotalBudgetGoalForUser(username)
    }

    suspend fun getCategoryTotals(username: String): List<CategoryTotal> {
        return budgetDao.getCategoryTotals(username)
    }

}