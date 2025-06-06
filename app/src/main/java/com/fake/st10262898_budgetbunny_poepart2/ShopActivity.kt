package com.fake.st10262898_budgetbunny_poepart2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fake.st10262898_budgetbunny_poepart2.data.ShopItem

class ShopActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ShopItemAdapter
    private var coinCount = 0

    private val sharedPrefs by lazy { getSharedPreferences("UserPrefs", MODE_PRIVATE) }
    private val boughtItemsSetKey = "bought_items_set"

    private val shopItems = mutableListOf(
        ShopItem("item1", R.drawable.jumpsuit_1, 20),
        ShopItem("item2", R.drawable.jumpsuit_2, 50)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shop)

        coinCount = sharedPrefs.getInt("userCoins", 0)

        recyclerView = findViewById(R.id.recyclerViewShop)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        // Load bought items from SharedPreferences and filter them out
        val boughtSet = sharedPrefs.getStringSet(boughtItemsSetKey, emptySet()) ?: emptySet()
        val availableItems = shopItems.filter { it.id !in boughtSet }.toMutableList()

        adapter = ShopItemAdapter(availableItems) { item -> attemptBuy(item) }
        recyclerView.adapter = adapter

        // Optionally show coin count in toolbar or here
        findViewById<TextView>(R.id.coinCountTextShop).text = "Coins: $coinCount"

        Log.d("ShopActivity", "Loaded coin count: $coinCount")
        Log.d("ShopActivity", "Bought items: $boughtSet")
        Log.d("ShopActivity", "Available items count: ${availableItems.size}")


        val btnReturnToBunny = findViewById<Button>(R.id.btnReturnToBunny)
        btnReturnToBunny.setOnClickListener {
            val intent = Intent(this, BunnyActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    private fun attemptBuy(item: ShopItem) {
        if (coinCount < item.price) {
            Toast.makeText(this, "Not enough coins", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Confirm Purchase")
            .setMessage("Buy this item for ${item.price} coins?")
            .setPositiveButton("Buy") { _, _ ->
                buyItem(item)
            }
            .setNegativeButton("Cancel", null)
            .show()

        Log.d("ShopActivity", "Attempting to buy: ${item.id} for ${item.price} coins")
    }

    private fun buyItem(item: ShopItem) {
        coinCount -= item.price
        sharedPrefs.edit().putInt("userCoins", coinCount).apply()

        // Add to bought set
        val boughtSet = sharedPrefs.getStringSet(boughtItemsSetKey, mutableSetOf())?.toMutableSet()
            ?: mutableSetOf()
        boughtSet.add(item.id)
        sharedPrefs.edit().putStringSet(boughtItemsSetKey, boughtSet).apply()

        adapter.removeItem(item)
        findViewById<TextView>(R.id.coinCountTextShop).text = "Coins: $coinCount"

        Toast.makeText(this, "Item purchased!", Toast.LENGTH_SHORT).show()

        Log.d("ShopActivity", "Item purchased: ${item.id}")
        Log.d("ShopActivity", "Remaining coins: $coinCount")

    }
}