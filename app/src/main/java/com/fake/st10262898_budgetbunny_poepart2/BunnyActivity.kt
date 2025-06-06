package com.fake.st10262898_budgetbunny_poepart2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.DragEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.fake.st10262898_budgetbunny_poepart2.data.ShopItem

class BunnyActivity : AppCompatActivity() {

    private lateinit var closetContainer: LinearLayout
    private lateinit var bunnyImage: ImageView
    private lateinit var coinCountText: TextView
    private var coinCount: Int = 0



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bunny)

        coinCountText = findViewById(R.id.coinCountText)
        bunnyImage = findViewById(R.id.bunnyImage)
        closetContainer = findViewById(R.id.closetContainer)
        val toggleBar = findViewById<LinearLayout>(R.id.toggleBar)
        val toggleHandle = findViewById<Button>(R.id.toggleHandleButton)
        val mainContent = findViewById<FrameLayout>(R.id.mainContentContainer)

        loadBoughtClothingItems()

        val btnShop = findViewById<Button>(R.id.btnShop)
        val btnHowToPlay = findViewById<Button>(R.id.btnHowToPlay)

        btnShop.setOnClickListener {

            Toast.makeText(this, "Shop clicked", Toast.LENGTH_SHORT).show()
        }

        btnHowToPlay.setOnClickListener {

            Toast.makeText(this, "How to play clicked", Toast.LENGTH_SHORT).show()
        }

        var isOpen = false

        toggleHandle.setOnClickListener {
            if (isOpen) {
                // Close toggle bar - slide right out
                toggleBar.animate().translationX(toggleBar.width.toFloat()).setDuration(300).start()
                // Expand main content to full width
                mainContent.animate().translationX(0f).setDuration(300).start()
            } else {
                // Open toggle bar - slide in from right
                toggleBar.animate().translationX(0f).setDuration(300).start()
                // Push main content left by toggleBar width
                mainContent.animate().translationX(-toggleBar.width.toFloat()).setDuration(300).start()
            }
            isOpen = !isOpen
        }

        // Load saved coins first
        coinCount = loadCoinsFromPrefs()
        updateCoinDisplay()




        btnShop.setOnClickListener {
            val intent = Intent(this, ShopActivity::class.java)
            startActivity(intent)
        }

        val btnReset = findViewById<Button>(R.id.btnReset)
        btnReset.setOnClickListener {
            resetClosetAndRefund()
        }

        /*
        val sharedPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        coinCount = sharedPrefs.getInt("userCoins", 0)

        coinCountText = findViewById(R.id.coinCountText)
        coinCountText.text = "Coins: $coinCount"


         */

    }

    fun spendCoins(amount: Int) {
        if (coinCount >= amount) {
            coinCount -= amount
            coinCountText.text = "Coins: $coinCount"

            val sharedPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
            sharedPrefs.edit().putInt("userCoins", coinCount).apply()
        } else {
            Toast.makeText(this, "Not enough coins!", Toast.LENGTH_SHORT).show()
        }
    }


    private fun addClothingItems() {
        val clothingImages = listOf(
            R.drawable.jumpsuit_1,
            R.drawable.jumpsuit_2
        )

        clothingImages.forEach { imageRes ->
            val closetItem = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(380, 380).apply {
                marginEnd = 16
                }
                setImageResource(imageRes)
                setOnTouchListener(DragTouchListener())
            }
            closetContainer.addView(closetItem)
        }
    }




    inner class DragTouchListener : View.OnTouchListener {
        override fun onTouch(view: View, event: MotionEvent): Boolean {
            if (event.action == MotionEvent.ACTION_DOWN) {
                val shadow = View.DragShadowBuilder(view)
                view.startDragAndDrop(null, shadow, view, 0)
                return true
            }
            return false
        }
    }


    override fun onResume() {
        super.onResume()


        val rootLayout = findViewById<ViewGroup>(R.id.dressUpLayout)

        rootLayout.setOnDragListener { view, event ->
            when (event.action) {
                DragEvent.ACTION_DROP -> {
                    val draggedView = event.localState as View

                    val dropX = event.x
                    val dropY = event.y

                    if (draggedView.parent != rootLayout) {

                        (draggedView.parent as? ViewGroup)?.removeView(draggedView)
                        rootLayout.addView(draggedView)

                        // Delay positioning to avoid "sticking" on first drop
                        draggedView.post {
                            draggedView.x = dropX - draggedView.width / 2
                            draggedView.y = dropY - draggedView.height / 2
                        }

                        // Allow it to be dragged again
                        draggedView.setOnTouchListener(DragTouchListener())
                    } else {
                        // Already in layout, just move it
                        draggedView.x = dropX - draggedView.width / 2
                        draggedView.y = dropY - draggedView.height / 2
                    }

                    true
                }

                else -> true
            }
        }
    }

    private fun updateCoinDisplay() {
        coinCountText.text = "Coins: $coinCount"
        saveCoinsToPrefs(coinCount)
    }

    private fun saveCoinsToPrefs(coins: Int) {
        val sharedPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val username = sharedPrefs.getString("username", null)
        if (username != null) {
            sharedPrefs.edit().putInt("${username}_userCoins", coins).apply()
        }
    }

    private fun loadCoinsFromPrefs(): Int {
        val sharedPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val username = sharedPrefs.getString("username", null)
        return if (username != null) {
            sharedPrefs.getInt("${username}_userCoins", 0)
        } else {
            0
        }
    }

    private fun resetClosetAndRefund() {
        val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val boughtSetKey = "bought_items_set"

        // Load bought items (the clothes currently owned)
        val boughtSet = prefs.getStringSet(boughtSetKey, emptySet()) ?: emptySet()

        if (boughtSet.isEmpty()) {
            Toast.makeText(this, "No items to reset.", Toast.LENGTH_SHORT).show()
            return
        }

        // Calculate total refund coins by summing prices of bought items
        val allShopItems = listOf(
            ShopItem("item1", R.drawable.jumpsuit_1, 20),
            ShopItem("item2", R.drawable.jumpsuit_2, 50)
        )
        val refundAmount = allShopItems.filter { it.id in boughtSet }.sumOf { it.price }

        // Refund coins
        coinCount += refundAmount
        updateCoinDisplay()

        // Clear bought items set (remove all items from closet/shop bought list)
        prefs.edit().putStringSet(boughtSetKey, emptySet()).apply()

        // Remove all clothing views from closetContainer or dressing area
        closetContainer.removeAllViews()

        // Optionally add back default closet items if needed (e.g. addClothingItems())

        Toast.makeText(this, "Closet reset! Refunded $refundAmount coins.", Toast.LENGTH_SHORT).show()
    }

    private fun loadBoughtClothingItems() {
        val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val boughtSet = prefs.getStringSet("bought_items_set", emptySet()) ?: emptySet()


        val allShopItems = listOf(
            ShopItem("item1", R.drawable.jumpsuit_1, 20),
            ShopItem("item2", R.drawable.jumpsuit_2, 50)
        )

        // Filter only bought items and show them
        allShopItems.filter { it.id in boughtSet }.forEach { item ->
            val closetItem = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(380, 380).apply {
                    marginEnd = 16
                }
                setImageResource(item.imageRes)
                setOnTouchListener(DragTouchListener())
            }
            closetContainer.addView(closetItem)
        }
    }





}
