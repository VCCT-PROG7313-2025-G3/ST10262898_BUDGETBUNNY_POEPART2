package com.fake.st10262898_budgetbunny_poepart2.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class BudgetFirestoreDao {
    private val db: FirebaseFirestore = Firebase.firestore
    private val budgetsCollection = db.collection("budgets")

    suspend fun insertBudget(budget: BudgetFirestore): String {
        val document = budgetsCollection.add(budget).await()
        return document.id
    }

    suspend fun getBudgetForUser(username: String): List<BudgetFirestore> {
        return budgetsCollection
            .whereEqualTo("username", username)
            .get()
            .await()
            .toObjects(BudgetFirestore::class.java)
            .mapIndexed { index, budget ->
                budget.copy(id = budget.id ?: "") // Ensure ID is set
            }
    }

    suspend fun getMinTotalBudgetGoalForUser(username: String): Double {
        val budgets = getBudgetForUser(username)
        return budgets.firstOrNull()?.minTotalBudgetGoal ?: 0.0
    }

    suspend fun updateMinTotalBudgetGoalForUser(username: String, minTotalBudgetGoal: Double) {
        budgetsCollection
            .whereEqualTo("username", username)
            .get()
            .await()
            .documents
            .forEach { document ->
                document.reference.update("minTotalBudgetGoal", minTotalBudgetGoal).await()
            }
    }

    suspend fun getBudgetsInDateRange(username: String, start: Long, end: Long): List<BudgetFirestore> {
        return budgetsCollection
            .whereEqualTo("username", username)
            .whereGreaterThanOrEqualTo("budgetDate", start)
            .whereLessThanOrEqualTo("budgetDate", end)
            .get()
            .await()
            .toObjects(BudgetFirestore::class.java)
    }

    suspend fun deleteBudget(id: String) {
        budgetsCollection.document(id).delete().await()
    }

    suspend fun getCategoryTotalsForDateRange(
        username: String,
        startDate: Long,
        endDate: Long
    ): List<CategoryTotalFirestore> {
        val budgets = getBudgetsInDateRange(username, startDate, endDate)
        return budgets.groupBy { it.budgetCategory }
            .map { (category, budgets) ->
                CategoryTotalFirestore(
                    budgetCategory = category,
                    total = budgets.sumOf { it.budgetAmount }
                )
            }
    }

    suspend fun getAllBudgetsForUser(username: String): List<BudgetFirestore> {
        return getBudgetForUser(username)
    }

    suspend fun getCategoryTotals(username: String): List<CategoryTotalFirestore> {
        val budgets = getBudgetForUser(username)
        return budgets.groupBy { it.budgetCategory }
            .map { (category, budgets) ->
                CategoryTotalFirestore(
                    budgetCategory = category,
                    total = budgets.sumOf { it.budgetAmount }
                )
            }
    }
}