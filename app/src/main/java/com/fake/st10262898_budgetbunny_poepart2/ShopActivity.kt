package com.fake.st10262898_budgetbunny_poepart2

import ShopItemAdapter
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

        val btnReturnToBunny = findViewById<Button>(R.id.btnReturnToBunny)


        userId = getSharedPreferences("UserPrefs", MODE_PRIVATE)
            .getString("username", "") ?: return

        btnReturnToBunny.setOnClickListener {
            val intent = Intent(this, BunnyActivity::class.java)
            startActivity(intent)
            finish()
        }

        setupViews()
        loadUserData()
    }

    private fun setupViews() {
        recyclerView = findViewById(R.id.recyclerViewShop)
        recyclerView.layoutManager = GridLayoutManager(this, 2) // 2 items per row

        // Add consistent spacing between items
        val spacing = resources.getDimensionPixelSize(R.dimen.item_spacing)
        recyclerView.addItemDecoration(
            GridSpacingItemDecoration(2, spacing, true)
        )
    }

    private fun loadUserData() {
        // Load coins and purchased items
        firestore.collection("UserCoins").document(userId).get()
            .addOnSuccessListener { coinDoc ->
                userCoins = coinDoc.getLong("currentBalance")?.toInt() ?: 0
                updateCoinDisplay()

                firestore.collection("UserPurchases").document(userId).get()
                    .addOnSuccessListener { purchasesDoc ->
                        val purchasedItems = purchasesDoc.get("items") as? List<String> ?: emptyList()
                        loadShopItems(purchasedItems)
                    }
                    .addOnFailureListener { e ->
                        Log.e("ShopActivity", "Error loading purchases", e)
                        // Still load shop items even if purchases fail
                        loadShopItems(emptyList())
                    }
            }
            .addOnFailureListener { e ->
                Log.e("ShopActivity", "Error loading user coins", e)
                Toast.makeText(this, "Error loading data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadShopItems(purchasedItems: List<String>) {
        firestore.collection("ShopItems").get()
            .addOnSuccessListener { querySnapshot ->
                val availableItems = querySnapshot.documents.mapNotNull { doc ->
                    val itemId = doc.getString("id") ?: doc.id
                    val imageName = doc.getString("imageName") ?: ""
                    val price = doc.getLong("price")?.toInt() ?: 0
                    val category = doc.getString("category") ?: ""

                    Log.d("ShopActivity", "Loading item: $itemId, image: $imageName, price: $price")

                    ShopItem(
                        id = itemId,
                        imageName = imageName,
                        price = price,
                        category = category
                    )
                }.filter { item ->
                    val isNotPurchased = item.id !in purchasedItems
                    Log.d("ShopActivity", "Item ${item.id} - isPurchased: ${!isNotPurchased}")
                    isNotPurchased
                }

                Log.d("ShopActivity", "Total available items: ${availableItems.size}")

                if (availableItems.isEmpty()) {
                    Toast.makeText(this, "No items available in shop", Toast.LENGTH_SHORT).show()
                }

                adapter = ShopItemAdapter(availableItems) { item -> attemptBuy(item) }
                recyclerView.adapter = adapter
            }
            .addOnFailureListener { e ->
                Log.e("ShopActivity", "Error loading shop items", e)
                Toast.makeText(this, "Error loading shop items", Toast.LENGTH_SHORT).show()
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
                purchaseItem(item)
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
            Toast.makeText(this, "Purchase successful!", Toast.LENGTH_SHORT).show()
            setResult(RESULT_OK, Intent().putExtra("NEW_COINS", newBalance.toInt()))
        }.addOnFailureListener { e ->
            progressDialog.dismiss()
            Toast.makeText(this, "Purchase failed: ${e.message}", Toast.LENGTH_LONG).show()
            Log.e("ShopActivity", "Purchase failed", e)
        }
    }

    private fun updateCoinDisplay() {
        findViewById<TextView>(R.id.coinCountTextShop).text = "Coins: $userCoins"
    }
}