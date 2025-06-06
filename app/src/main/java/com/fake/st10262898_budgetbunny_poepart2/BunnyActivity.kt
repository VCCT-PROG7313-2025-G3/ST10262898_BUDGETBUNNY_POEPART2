package com.fake.st10262898_budgetbunny_poepart2

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

        addClothingItems()

        val btnShop = findViewById<Button>(R.id.btnShop)
        val btnHowToPlay = findViewById<Button>(R.id.btnHowToPlay)

        btnShop.setOnClickListener {
            // TODO: open Shop UI or fragment/dialog
            Toast.makeText(this, "Shop clicked", Toast.LENGTH_SHORT).show()
        }

        btnHowToPlay.setOnClickListener {
            // TODO: open How To Play instructions UI or dialog
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


        //Some coin logic:
        /*val income = intent.getIntExtra("user_income", 0)
        if (coinCount == 0 && income > 0) { // Only give coins once
            coinCount = income / 10 // e.g. R5000 = 500 coins
            updateCoinDisplay()
        }*/



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
        val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        prefs.edit().putInt("userCoins", coins).apply()
    }

    private fun loadCoinsFromPrefs(): Int {
        val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val loadedCoins = prefs.getInt("userCoins", 0)
        return loadedCoins
    }



}
