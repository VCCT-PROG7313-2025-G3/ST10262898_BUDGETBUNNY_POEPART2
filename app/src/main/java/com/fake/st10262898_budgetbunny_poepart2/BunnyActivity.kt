package com.fake.st10262898_budgetbunny_poepart2

import android.app.ProgressDialog
import android.content.ContentValues.TAG
import com.google.firebase.firestore.FieldValue
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
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.fake.st10262898_budgetbunny_poepart2.viewmodel.BudgetViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class BunnyActivity : AppCompatActivity() {

    // UI Components
    private lateinit var closetContainer: LinearLayout
    private lateinit var bunnyImage: ImageView
    private lateinit var coinCountText: TextView
    private lateinit var bunnyNameText: TextView

    private val budgetViewModel: BudgetViewModel by viewModels()



    private val imageResourceMap = mapOf(
        "jumpsuit_1" to R.drawable.jumpsuit_1,
        "jumpsuit_2" to R.drawable.jumpsuit_2
        //add more shopping items here cails.
    )

    // Firebase
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bunny)

        val username = getSharedPreferences("UserPrefs", MODE_PRIVATE)
            .getString("username", "") ?: return


        // Initialize views
        coinCountText = findViewById(R.id.coinCountText)
        bunnyImage = findViewById(R.id.bunnyImage)
        closetContainer = findViewById(R.id.closetContainer)
        bunnyNameText = findViewById(R.id.bunnyNameText)

        val dressUpLayout = findViewById<View>(R.id.dressUpLayout)
        dressUpLayout.setOnDragListener(dragListener)


        // Observe coins
        budgetViewModel.userCoins.observe(this) { coins ->
            coinCountText.text = "Coins: $coins"
        }

        // Load and calculate coins
        budgetViewModel.loadUserCoins(username)
        budgetViewModel.calculateAndUpdateCoins(username)
        loadPurchasedItems(username)


        // Setup toggle bar
        setupToggleBar()


        // Setup buttons
        setupButtons()

        budgetViewModel.userCoins.observe(this) { coins ->
            coinCountText.text = "Coins: $coins"
            Log.d("CoinDebug", "Coins updated to: $coins")
        }

    }

    override fun onResume() {
        super.onResume()
        val username = getSharedPreferences("UserPrefs", MODE_PRIVATE)
            .getString("username", "") ?: return

        budgetViewModel.userCoins.observe(this) { coins ->
            coinCountText.text = "Coins: $coins"
        }

        budgetViewModel.calculateAndUpdateCoins(username)
        loadPurchasedItems(username)
    }

    private fun setupToggleBar() {
        val toggleBar = findViewById<LinearLayout>(R.id.toggleBar)
        val toggleHandle = findViewById<Button>(R.id.toggleHandleButton)
        val mainContent = findViewById<FrameLayout>(R.id.mainContentContainer)
        val btnCloseToggle = findViewById<Button>(R.id.btnCloseToggle)

        var isOpen = false

        toggleHandle.setOnClickListener {
            isOpen = if (isOpen) {
                toggleBar.animate().translationX(toggleBar.width.toFloat()).setDuration(300).start()
                mainContent.animate().translationX(0f).setDuration(300).start()
                false
            } else {
                toggleBar.animate().translationX(0f).setDuration(300).start()
                mainContent.animate().translationX(-toggleBar.width.toFloat()).setDuration(300).start()
                true
            }
        }

        btnCloseToggle.setOnClickListener {
            toggleBar.animate().translationX(toggleBar.width.toFloat()).setDuration(300).start()
            mainContent.animate().translationX(0f).setDuration(300).start()
            isOpen = false
        }
    }



    private fun setupButtons() {

        findViewById<Button>(R.id.btnShop).setOnClickListener {
            startActivityForResult(Intent(this, ShopActivity::class.java), SHOP_REQUEST_CODE)
        }

        findViewById<Button>(R.id.btnHowToPlay).setOnClickListener {
            startActivity(Intent(this, HowToPlayActivity::class.java))
        }


        findViewById<Button>(R.id.btnReset).setOnClickListener {
            resetClosetAndRefund()
        }



        findViewById<Button>(R.id.btnExit).setOnClickListener {
            finish()
        }
    }


    // Drag and drop functionality
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SHOP_REQUEST_CODE && resultCode == RESULT_OK) {
            val username = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                .getString("username", "") ?: return

            // Force refresh everything
            budgetViewModel.refreshCoins(username)
            loadPurchasedItems(username)

            // Update coins immediately if provided
            data?.getIntExtra("NEW_COINS", -1)?.takeIf { it >= 0 }?.let { coins ->
                coinCountText.text = "Coins: $coins"
            }
        }
    }

    companion object {
        const val SHOP_REQUEST_CODE = 1001

    }

    private val dragListener = View.OnDragListener { v, event ->
        when (event.action) {
            DragEvent.ACTION_DROP -> {
                val draggedView = event.localState as ImageView
                val dropX = event.x - draggedView.width / 2
                val dropY = event.y - draggedView.height / 2


                val dressUpLayout = findViewById<RelativeLayout>(R.id.dressUpLayout)

                // Remove from parent if needed
                if (draggedView.parent != null) {
                    (draggedView.parent as ViewGroup).removeView(draggedView)
                }

                // Set new position parameters
                draggedView.x = dropX
                draggedView.y = dropY

                // Add to dress up area
                dressUpLayout.addView(draggedView)
                true
            }
            else -> true
        }
    }


    //Functions for shop and clothes and closet start now:
    private fun loadPurchasedItems(username: String) {
        // Clear existing items first
        closetContainer.removeAllViews()

        firestore.collection("UserPurchases").document(username).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Use distinct() to ensure no duplicates
                    val purchasedItems = (document.get("items") as? List<String>)?.distinct() ?: emptyList()

                    if (purchasedItems.isNotEmpty()) {
                        // Load item details in batch
                        firestore.collection("ShopItems")
                            .whereIn("id", purchasedItems)
                            .get()
                            .addOnSuccessListener { querySnapshot ->
                                // Create a map to track added items
                                val addedItems = mutableSetOf<String>()

                                querySnapshot.documents.forEach { doc ->
                                    val itemId = doc.getString("id") ?: return@forEach
                                    if (!addedItems.contains(itemId)) {
                                        addItemToCloset(itemId)
                                        addedItems.add(itemId)
                                    }
                                }
                            }
                    }
                }
            }
    }


    private fun displayPurchasedItems(itemIds: List<String>) {
        if (itemIds.isEmpty()) return

        firestore.collection("ShopItems")
            .whereIn("id", itemIds)
            .get()
            .addOnSuccessListener { querySnapshot ->
                // Create a map to ensure unique items
                val uniqueItems = mutableMapOf<String, String>()
                querySnapshot.documents.forEach { doc ->
                    val itemName = doc.getString("id") ?: return@forEach
                    uniqueItems[itemName] = itemName
                }

                // Add only unique items to closet
                uniqueItems.values.forEach { itemName ->
                    addItemToCloset(itemName)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error loading shop items", e)
            }
    }

    private fun addItemToCloset(itemName: String) {
        val resourceId = imageResourceMap[itemName] ?: return

        // Check if item already exists in closet
        for (i in 0 until closetContainer.childCount) {
            val view = closetContainer.getChildAt(i)
            if (view is ImageView && view.tag == itemName) {
                return // Item already exists, don't add again
            }
        }

        val imageView = ImageView(this).apply {
            setImageResource(resourceId)
            layoutParams = LinearLayout.LayoutParams(380, 380).apply {
                marginEnd = 16
            }
            tag = itemName // Use tag to track items
            setOnTouchListener(DragTouchListener())
        }

        closetContainer.addView(imageView)
    }


    private fun resetClosetAndRefund() {
        val username = getSharedPreferences("UserPrefs", MODE_PRIVATE)
            .getString("username", "") ?: return

        val progressDialog = ProgressDialog(this).apply {
            setMessage("Processing reset...")
            setCancelable(false)
        }

        AlertDialog.Builder(this)
            .setTitle("Reset Closet")
            .setMessage("Return all items to shop and refund coins?")
            .setPositiveButton("Reset") { _, _ ->
                progressDialog.show()

                lifecycleScope.launch {
                    try {
                        // 1. Check if purchases document exists
                        val purchasesRef = firestore.collection("UserPurchases").document(username)
                        val purchasesDoc = purchasesRef.get().await()

                        if (!purchasesDoc.exists()) {
                            progressDialog.dismiss()
                            Toast.makeText(
                                this@BunnyActivity,
                                "No items to reset",
                                Toast.LENGTH_LONG
                            ).show()
                            return@launch
                        }

                        // 2. Get items (empty list if field doesn't exist)
                        val items = purchasesDoc.get("items") as? List<String> ?: emptyList()

                        if (items.isEmpty()) {
                            progressDialog.dismiss()
                            Toast.makeText(
                                this@BunnyActivity,
                                "No items to reset",
                                Toast.LENGTH_LONG
                            ).show()
                            return@launch
                        }

                        // 3. Calculate refund
                        val shopItems = firestore.collection("ShopItems")
                            .whereIn("id", items).get().await()

                        val totalRefund = shopItems.sumOf { it.getLong("price")?.toInt() ?: 0 }

                        // 4. Execute transaction
                        firestore.runTransaction { transaction ->
                            // Clear purchases
                            transaction.update(purchasesRef, "items", emptyList<String>())

                            // Update coins (create if doesn't exist)
                            val coinsRef = firestore.collection("UserCoins").document(username)
                            if (!transaction.get(coinsRef).exists()) {
                                transaction.set(coinsRef, mapOf(
                                    "userId" to username,
                                    "coins" to totalRefund
                                ))
                            } else {
                                transaction.update(coinsRef, "coins", FieldValue.increment(totalRefund.toLong()))
                            }
                        }.addOnCompleteListener {
                            progressDialog.dismiss()

                            if (it.isSuccessful) {
                                closetContainer.removeAllViews()
                                budgetViewModel.refreshCoins(username)
                                Toast.makeText(
                                    this@BunnyActivity,
                                    "Reset complete! Refunded $totalRefund coins",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                Toast.makeText(
                                    this@BunnyActivity,
                                    "Reset failed: ${it.exception?.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    } catch (e: Exception) {
                        progressDialog.dismiss()
                        Toast.makeText(
                            this@BunnyActivity,
                            "Error: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                        Log.e("BunnyActivity", "Reset error", e)
                    }
                }
            }
            .setNegativeButton("Cancel") { _, _ -> progressDialog.dismiss() }
            .setOnDismissListener { progressDialog.dismiss() }
            .show()
    }



}