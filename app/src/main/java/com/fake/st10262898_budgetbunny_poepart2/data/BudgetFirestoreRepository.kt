package com.fake.st10262898_budgetbunny_poepart2.data

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.util.Date

class BudgetFirestoreRepository(private val budgetFirestoreDao: BudgetFirestoreDao) {
    private val db = Firebase.firestore
    private val TAG = "BudgetFirestoreRepo"


    suspend fun insertBudget(budget: BudgetFirestore): String {
        return try {
            // Create a new document with auto-generated ID (Firestore does this)
            val docRef = db.collection("budgets").document()

            // Set the data while preserving the auto-generated ID
            val budgetWithId = budget.copy(id = docRef.id)

            docRef.set(budgetWithId).await()
            Log.d(TAG, "Budget created with ID: ${docRef.id}")
            docRef.id
        } catch (e: Exception) {
            Log.e(TAG, "Error creating budget", e)
            throw e
        }
    }

    //Gets the budgets for a user
    suspend fun getBudgetsForUser(username: String): List<BudgetFirestore> {
        return try {
            db.collection("budgets")
                .whereEqualTo("username", username)
                .get()
                .await()
                .documents
                .map { doc ->
                    doc.toObject(BudgetFirestore::class.java)!!.copy(id = doc.id)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading budgets", e)
            emptyList()
        }
    }

    suspend fun deleteBudget(id: String) {
        budgetFirestoreDao.deleteBudget(id)
    }

    suspend fun updateMinTotalBudgetGoalForUser(username: String, minTotalBudgetGoal: Double) {
        budgetFirestoreDao.updateMinTotalBudgetGoalForUser(username, minTotalBudgetGoal)
    }

    suspend fun getMinTotalBudgetGoalForUser(username: String): Double {
        return budgetFirestoreDao.getMinTotalBudgetGoalForUser(username)
    }

    suspend fun getCategoryTotals(username: String): List<CategoryTotalFirestore> {
        return budgetFirestoreDao.getCategoryTotals(username)
    }


    //Allows user to see the totals they have for each category:
    fun getCategoryTotalsByDateRange(
        username: String,
        startDate: Long,
        endDate: Long,
        onResult: (List<CategoryTotalFirestore>) -> Unit
    ) {
        Log.d(TAG, "Querying budgets for $username from ${Date(startDate)} to ${Date(endDate)}")

        db.collection("budgets")
            .whereEqualTo("username", username)
            .whereGreaterThanOrEqualTo("budgetDate", startDate)
            .whereLessThanOrEqualTo("budgetDate", endDate)
            .get()
            .addOnSuccessListener { querySnapshot ->
                Log.d(TAG, "Found ${querySnapshot.size()} documents")

                val totals = mutableListOf<CategoryTotalFirestore>()
                val grouped = querySnapshot.documents.groupBy { document ->
                    document.getString("budgetCategory") ?: "Uncategorized".also {
                        Log.d(TAG, "Document: ${document.data}")
                    }
                }

                grouped.forEach { (category, documents) ->
                    val total = documents.sumOf { document ->
                        document.getDouble("budgetAmount") ?: 0.0.also {
                            Log.d(TAG, "Processing $category: ${document.data}")
                        }
                    }
                    totals.add(CategoryTotalFirestore(category, total))
                }

                Log.d(TAG, "Returning ${totals.size} category totals")
                onResult(totals)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error getting category totals", exception)
                onResult(emptyList())
            }
    }

    //This method updates the income from the budgets table
    suspend fun updateBudgetIncome(budgetId: String, additionalIncome: Double): Boolean {
        if (budgetId.isBlank()) {
            Log.w(TAG, "Attempted update with empty budget ID")
            return false
        }

        return try {
            val document = db.collection("budgets").document(budgetId)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(document)
                val current = snapshot.getDouble("budgetIncome") ?: 0.0
                transaction.update(document, "budgetIncome", current + additionalIncome)
            }.await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Update failed for budget $budgetId", e)
            false
        }
    }

    //Gets total income and converts it into coins (R10 = 1)
    suspend fun calculateAndUpdateCoins(username: String) {
        try {
            val budgets = db.collection("budgets")
                .whereEqualTo("username", username)
                .get()
                .await()

            val totalIncome = budgets.sumOf { it.getDouble("budgetIncome") ?: 0.0 }
            val earnedCoins = (totalIncome / 10).toInt()

            val userCoinsRef = db.collection("UserCoins").document(username)
            val existingDoc = userCoinsRef.get().await()

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
            Log.e("CoinCalc", "Error calculating coins", e)
            throw e
        }
    }


    //Now that the coins have been made the application can now get the coins to display them
    suspend fun getUserCoins(username: String): Int {
        return try {
            Log.d("CoinDebug", "Getting coins for $username")
            val doc = db.collection("UserCoins").document(username).get().await()
            val coins = doc.getLong("currentBalance")?.toInt() ?: 0
            Log.d("CoinDebug", "Firestore coins: $coins")
            coins
        } catch (e: Exception) {
            Log.e("CoinCalc", "Error getting coins", e)
            0
        }
    }


}