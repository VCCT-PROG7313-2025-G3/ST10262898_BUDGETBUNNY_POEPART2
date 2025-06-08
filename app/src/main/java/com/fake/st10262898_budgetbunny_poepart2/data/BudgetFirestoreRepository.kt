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
            // Create a new document with auto-generated ID
            val docRef = db.collection("budgets").document()

            // Set the data while preserving the auto-generated ID
            val budgetWithId = budget.copy(id = docRef.id)

            docRef.set(budgetWithId).await()
            Log.d(TAG, "Budget created with ID: ${docRef.id}")
            docRef.id // Return the new document ID
        } catch (e: Exception) {
            Log.e(TAG, "Error creating budget", e)
            throw e // Re-throw to handle in ViewModel
        }
    }

    suspend fun getBudgetsForUser(username: String): List<BudgetFirestore> {
        return try {
            db.collection("budgets")
                .whereEqualTo("username", username)
                .get()
                .await()
                .documents
                .map { doc ->
                    doc.toObject(BudgetFirestore::class.java)!!.copy(id = doc.id)  // Manually set ID
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

    suspend fun updateBudgetIncome(budgetId: String, additionalIncome: Double): Boolean {
        if (budgetId.isBlank()) {
            Log.w(TAG, "Attempted update with empty budget ID")
            return false
        }

        return try {
            val document = db.collection("budgets").document(budgetId) // Fixed reference
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

    suspend fun calculateAndUpdateCoins(username: String) {
        try {
            // Get all budgets for user and sum income
            val budgets = db.collection("budgets")
                .whereEqualTo("username", username)
                .get()
                .await()

            val totalIncome = budgets.sumOf { it.getDouble("budgetIncome") ?: 0.0 }
            val earnedCoins = (totalIncome / 10).toInt()

            // Update coins in Firestore with NEW structure
            val userCoinsRef = db.collection("UserCoins").document(username)

            // Get existing document to preserve currentBalance if it exists
            val existingDoc = userCoinsRef.get().await()
            val currentBalance = existingDoc.getLong("currentBalance") ?: earnedCoins

            userCoinsRef.set(
                mapOf(
                    "userId" to username,
                    "totalEarned" to earnedCoins,
                    "currentBalance" to currentBalance, // Preserve existing balance
                    "lastUpdated" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge() // Only update specified fields
            ).await()
        } catch (e: Exception) {
            Log.e("CoinCalc", "Error calculating coins", e)
            throw e
        }
    }


    suspend fun getUserCoins(username: String): Int {
        return try {
            Log.d("CoinDebug", "Getting coins for $username")
            val doc = db.collection("UserCoins").document(username).get().await()
            val coins = doc.getLong("coins")?.toInt() ?: 0
            Log.d("CoinDebug", "Firestore coins: $coins")
            coins
        } catch (e: Exception) {
            Log.e("CoinCalc", "Error getting coins", e)
            0
        }
    }


}