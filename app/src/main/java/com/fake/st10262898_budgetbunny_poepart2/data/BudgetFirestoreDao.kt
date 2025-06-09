package com.fake.st10262898_budgetbunny_poepart2.data

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.util.Date

class BudgetFirestoreDao {
    private val db: FirebaseFirestore = Firebase.firestore
    private val budgetsCollection = db.collection("budgets")
    private val userCoinsCollection = db.collection("UserCoins")



    suspend fun insertBudget(budget: BudgetFirestore): String {
        val document = budgetsCollection.add(budget).await()
        return document.id
    }

    //This gets the budget for a specific user
    suspend fun getBudgetForUser(username: String): List<BudgetFirestore> {
        return budgetsCollection
            .whereEqualTo("username", username)
            .get()
            .await()
            .toObjects(BudgetFirestore::class.java)
            .mapIndexed { index, budget ->
                budget.copy(id = budget.id ?: "")
            }
    }

    //This method gets the minimum total budget goal for a user
    suspend fun getMinTotalBudgetGoalForUser(username: String): Double {
        val budgets = getBudgetForUser(username)
        return budgets.firstOrNull()?.minTotalBudgetGoal ?: 0.0
    }

    //This method updates the minimum total of a user
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

    //This method will get the budgets in a date range
    suspend fun getBudgetsInDateRange(username: String, start: Long, end: Long): List<BudgetFirestore> {
        return budgetsCollection
            .whereEqualTo("username", username)
            .whereGreaterThanOrEqualTo("budgetDate", start)
            .whereLessThanOrEqualTo("budgetDate", end)
            .get()
            .await()
            .toObjects(BudgetFirestore::class.java)
    }

    //This allows the user to be able to delete a buget they do not use
    suspend fun deleteBudget(id: String) {
        budgetsCollection.document(id).delete().await()
    }

    //Allows user to get budget category totals within a specific date
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

    //This gets all the budgets for a specific user
    suspend fun getAllBudgetsForUser(username: String): List<BudgetFirestore> {
        return getBudgetForUser(username)
    }

    //This gets the category totals for a specific user
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

    //This method gets teh total income of a user, then converts the total income to coins (R10 = 1)
    suspend fun calculateAndUpdateCoins(username: String) {
        try {
            val budgets = budgetsCollection
                .whereEqualTo("username", username)
                .get()
                .await()

            val totalIncome = budgets.sumOf { it.getDouble("budgetIncome") ?: 0.0 }
            val earnedCoins = (totalIncome / 10).toInt()

            val userCoinsRef = userCoinsCollection.document(username)
            val existingDoc = userCoinsRef.get().await()

            // For new users, set both totalEarned and currentBalance to earnedCoins
            // For existing users, add the difference between new earnedCoins and previous totalEarned to currentBalance
            if (!existingDoc.exists()) {
                // New user - initialize both fields
                userCoinsRef.set(
                    mapOf(
                        "userId" to username,
                        "totalEarned" to earnedCoins,
                        "currentBalance" to earnedCoins,
                        "lastUpdated" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                // Existing user - calculate the difference and update
                val previousTotalEarned = existingDoc.getLong("totalEarned") ?: 0
                val currentBalance = existingDoc.getLong("currentBalance") ?: 0
                val newCoins = earnedCoins - previousTotalEarned

                userCoinsRef.set(
                    mapOf(
                        "userId" to username,
                        "totalEarned" to earnedCoins,
                        "currentBalance" to (currentBalance + newCoins).coerceAtLeast(0),
                        "lastUpdated" to FieldValue.serverTimestamp()
                    ),
                    SetOptions.merge()
                ).await()
            }
        } catch (e: Exception) {
            throw Exception("Failed to calculate and update coins", e)
        }
    }

    //After calculating the coins this gets the amount of coins has.
    suspend fun getUserCoins(username: String): Int {
        return try {
            val doc = userCoinsCollection.document(username).get().await()
            // Only use currentBalance - remove coins fallback
            doc.getLong("currentBalance")?.toInt() ?: 0
        } catch (e: Exception) {
            throw Exception("Failed to get user coins", e)
        }
    }


}