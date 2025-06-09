package com.fake.st10262898_budgetbunny_poepart2

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions


//I used this class to change database strcuture of past entries in the datbase:
class MigrationActivity : AppCompatActivity() {
    private val TAG = "FirestoreMigration"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_migration)

        findViewById<Button>(R.id.btn_migrate).setOnClickListener {
            Log.d(TAG, "Migrate button clicked")
            migrateUserCoins()
        }
    }

    private fun migrateUserCoins() {
        val db = FirebaseFirestore.getInstance()
        val statusText = findViewById<TextView>(R.id.status_text)
        statusText.text = "Migration started..."
        Log.d(TAG, "Starting migration process")

        db.collection("UserCoins").get()
            .addOnSuccessListener { documents ->
                Log.d(TAG, "Successfully fetched ${documents.size()} documents")
                if (documents.isEmpty) {
                    statusText.text = "No users to migrate"
                    Log.w(TAG, "No documents found to migrate")
                    return@addOnSuccessListener
                }

                val batch = db.batch()
                var processedCount = 0
                var failedCount = 0

                documents.forEach { document ->
                    try {
                        Log.d(TAG, "Processing document ${document.id}")

                        val currentCoins = document.getLong("coins") ?: 0L
                        val userId = document.getString("userId") ?: document.id
                        val lastUpdated = document.getTimestamp("lastUpdated") ?: FieldValue.serverTimestamp()

                        Log.d(TAG, "Document data: " +
                                "coins=$currentCoins, " +
                                "userId=$userId, " +
                                "lastUpdated=$lastUpdated")

                        // Create new document with userId as document ID
                        val newDocRef = db.collection("UserCoins").document(userId)
                        Log.d(TAG, "New document reference: ${newDocRef.path}")

                        val newData = mapOf(
                            "userId" to userId,
                            "totalEarned" to currentCoins,
                            "currentBalance" to currentCoins,
                            "lastUpdated" to lastUpdated
                        )

                        Log.d(TAG, "Setting new data: $newData")
                        batch.set(newDocRef, newData)

                        // Optionally delete old document if ID was different
                        if (document.id != userId) {
                            Log.d(TAG, "Deleting old document ${document.id}")
                            batch.delete(document.reference)
                        }

                        processedCount++
                        Log.d(TAG, "Successfully processed user $userId ($processedCount/${documents.size()})")
                    } catch (e: Exception) {
                        failedCount++
                        Log.e(TAG, "Error processing document ${document.id}", e)
                    }
                }

                Log.d(TAG, "Committing batch with $processedCount operations")
                batch.commit()
                    .addOnSuccessListener {
                        val msg = "Migration complete! Success: $processedCount, Failed: $failedCount"
                        statusText.text = msg
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                        Log.i(TAG, msg)

                        // Verify migration by checking a sample document
                        if (processedCount > 0) {
                            verifyMigration(db)
                        }
                    }
                    .addOnFailureListener { e ->
                        val msg = "Batch commit failed"
                        statusText.text = msg
                        Toast.makeText(this, "$msg: ${e.message}", Toast.LENGTH_LONG).show()
                        Log.e(TAG, msg, e)
                    }
            }
            .addOnFailureListener { e ->
                val msg = "Failed to get documents"
                statusText.text = msg
                Toast.makeText(this, "$msg: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e(TAG, msg, e)
            }
    }

    private fun verifyMigration(db: FirebaseFirestore) {
        Log.d(TAG, "Starting migration verification")

        // Get the first 5 documents to verify
        db.collection("UserCoins")
            .limit(5)
            .get()
            .addOnSuccessListener { documents ->
                Log.d(TAG, "Verification fetched ${documents.size()} documents")
                documents.forEach { doc ->
                    Log.d(TAG, "Verified document ${doc.id}: " +
                            "totalEarned=${doc.getLong("totalEarned")}, " +
                            "currentBalance=${doc.getLong("currentBalance")}, " +
                            "userId=${doc.getString("userId")}, " +
                            "lastUpdated=${doc.getTimestamp("lastUpdated")}")
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Verification failed", e)
            }
    }
}