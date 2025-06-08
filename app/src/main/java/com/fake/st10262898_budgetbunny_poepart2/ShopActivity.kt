package com.fake.st10262898_budgetbunny_poepart2

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fake.st10262898_budgetbunny_poepart2.data.ShopItem
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class ShopActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ShopItemAdapter
    private var userCoins = 0
    private lateinit var userId: String
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shop)

        userId = getSharedPreferences("UserPrefs", MODE_PRIVATE)
            .getString("username", "") ?: return

        setupViews()
        loadUserData()
    }

    private fun setupViews() {
        recyclerView = findViewById(R.id.recyclerViewShop)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        findViewById<Button>(R.id.btnReturnToBunny).setOnClickListener {
            finish()
        }
    }

    private fun loadUserData() {
        // Load coins and purchased items
        firestore.collection("UserCoins").document(userId).get()
            .addOnSuccessListener { coinDoc ->
                // Only use currentBalance - no fallback to coins
                userCoins = coinDoc.getLong("currentBalance")?.toInt() ?: 0
                updateCoinDisplay()

                firestore.collection("UserPurchases").document(userId).get()
                    .addOnSuccessListener { purchasesDoc ->
                        val purchasedItems = purchasesDoc.get("items") as? List<String> ?: emptyList()
                        loadShopItems(purchasedItems)
                    }
            }
    }

    private fun loadShopItems(purchasedItems: List<String>) {
        firestore.collection("ShopItems").get()
            .addOnSuccessListener { querySnapshot ->
                val availableItems = querySnapshot.documents.mapNotNull { doc ->
                    ShopItem(
                        id = doc.getString("id") ?: "",
                        imageName = doc.getString("imageName") ?: "",
                        price = doc.getLong("price")?.toInt() ?: 0,
                        category = doc.getString("category") ?: ""
                    )
                }.filter { it.id !in purchasedItems }

                adapter = ShopItemAdapter(availableItems) { item -> attemptBuy(item) }
                recyclerView.adapter = adapter
            }
    }

    private fun attemptBuy(item: ShopItem) {
        if (userCoins < item.price) {
            Toast.makeText(this, "Not enough coins!", Toast.LENGTH_LONG).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Confirm Purchase")
            .setMessage("Buy ${item.id} for ${item.price} coins?")
            .setPositiveButton("Buy") { _, _ ->
                // Show loading dialog
                val progressDialog = ProgressDialog(this).apply {
                    setMessage("Processing...")
                    setCancelable(false)
                    show()
                }

                purchaseItem(item)
                progressDialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun purchaseItem(item: ShopItem) {
        val userId = getSharedPreferences("UserPrefs", MODE_PRIVATE)
            .getString("username", "") ?: return

        val userPurchasesRef = firestore.collection("UserPurchases").document(userId)
        val userCoinsRef = firestore.collection("UserCoins").document(userId)

        // First ensure the purchases document exists
        userPurchasesRef.get().addOnSuccessListener { document ->
            if (!document.exists()) {
                // Create the document if it doesn't exist
                userPurchasesRef.set(mapOf("items" to emptyList<String>()))
                    .addOnSuccessListener {
                        // Now proceed with the purchase
                        executePurchase(item, userPurchasesRef, userCoinsRef)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to initialize purchases", Toast.LENGTH_SHORT).show()
                        Log.e("ShopActivity", "Error creating purchases doc", e)
                    }
            } else {
                // Document exists, proceed with purchase
                executePurchase(item, userPurchasesRef, userCoinsRef)
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Failed to check purchases", Toast.LENGTH_SHORT).show()
            Log.e("ShopActivity", "Error checking purchases doc", e)
        }
    }


    private fun executePurchase(item: ShopItem,
                                userPurchasesRef: DocumentReference,
                                userCoinsRef: DocumentReference) {
        val progressDialog = ProgressDialog(this).apply {
            setMessage("Processing purchase...")
            setCancelable(false)
            show()
        }

        firestore.runTransaction { transaction ->
            val coinDoc = transaction.get(userCoinsRef)

            // Get current balance (prefer currentBalance, fall back to coins for legacy)
            val currentBalance = coinDoc.getLong("currentBalance")
                ?: coinDoc.getLong("coins")
                ?: 0

            if (currentBalance < item.price) {
                throw Exception("Not enough coins")
            }

            // Update both documents - maintain full structure
            transaction.update(userPurchasesRef, "items", FieldValue.arrayUnion(item.id))

            // Create complete update with all required fields
            transaction.set(userCoinsRef,
                mapOf(
                    "userId" to userId,
                    "currentBalance" to (currentBalance - item.price),
                    "totalEarned" to (coinDoc.getLong("totalEarned") ?: currentBalance),
                    "lastUpdated" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )

            currentBalance - item.price
        }.addOnSuccessListener { newBalance ->
            userCoins = newBalance.toInt()
            updateCoinDisplay()
            adapter.removeItem(item)
            progressDialog.dismiss()
            setResult(RESULT_OK, Intent().putExtra("NEW_COINS", newBalance.toInt()))
        }.addOnFailureListener { e ->
            progressDialog.dismiss()
            Toast.makeText(this, "Purchase failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }


    private fun updateCoinDisplay() {
        findViewById<TextView>(R.id.coinCountTextShop).text = "Coins: $userCoins"
    }




}