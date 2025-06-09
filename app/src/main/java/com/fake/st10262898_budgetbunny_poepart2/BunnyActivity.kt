package com.fake.st10262898_budgetbunny_poepart2

import android.app.ProgressDialog
import android.content.ContentValues.TAG
import com.google.firebase.firestore.FieldValue
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.view.DragEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.fake.st10262898_budgetbunny_poepart2.viewmodel.BudgetViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import kotlin.math.abs

class BunnyActivity : AppCompatActivity() {

    // UI Components
    private lateinit var closetContainer: LinearLayout
    private lateinit var bunnyImage: ImageView
    private lateinit var coinCountText: TextView
    private lateinit var bunnyNameText: TextView

    //allows the methods in the budgetViewModel to be used
    private val budgetViewModel: BudgetViewModel by viewModels()



    //This links whats in the drawables to the database and to the page
    private val imageResourceMap = mapOf(
        "jumpsuit_1" to R.drawable.jumpsuit_1,
        "jumpsuit_2" to R.drawable.jumpsuit_2,
        "blue_shirt" to R.drawable.clothes_blue_shirt,
        "brat_shirt" to R.drawable.clothes_brat,
        "cap" to R.drawable.clothes_cap,
        "KCP_shirt" to R.drawable.clothes_kcp,
        "coffee_shirt" to R.drawable.clothes_shirt_coffee,
        "sideman_hoodie" to R.drawable.clothes_sideman,
        "skirt" to R.drawable.clothes_skirt
    )



    // Firebase
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bunny)

        //Get which user is locked in
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

        closetContainer.post {
            Log.d("ClosetDebug", "Closet container size: ${closetContainer.width}x${closetContainer.height}")
        }

        closetContainer.postDelayed({
            Log.d("ClosetDebug", "Closet children: ${closetContainer.childCount}")
            for (i in 0 until closetContainer.childCount) {
                val view = closetContainer.getChildAt(i)
                Log.d("ClosetDebug", "Child $i: ${view.width}x${view.height} visible=${view.isShown}")
                if (view is ImageView) {
                    Log.d("ClosetDebug", "Image tag: ${view.tag ?: "No tag"}")
                }
            }
        }, 1000)

    }



    //What happens when the application is resumed
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

    //This is the feature for the toggle bar on the right
    private fun setupToggleBar() {
        val toggleBar = findViewById<LinearLayout>(R.id.toggleBar)
        val toggleHandle = findViewById<Button>(R.id.toggleHandleButton)
        val btnCloseToggle = findViewById<ImageButton>(R.id.btnCloseToggle)

        var isOpen = false

        toggleHandle.setOnClickListener {
            isOpen = if (isOpen) {

                toggleBar.animate()
                    .translationX(toggleBar.width.toFloat())
                    .setDuration(300)
                    .start()
                false
            } else {

                toggleBar.animate()
                    .translationX(0f)
                    .setDuration(300)
                    .start()
                true
            }
        }

        btnCloseToggle.setOnClickListener {

            toggleBar.animate()
                .translationX(toggleBar.width.toFloat())
                .setDuration(300)
                .start()
            isOpen = false
        }
    }



    //This sets up the elements in the xml
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

        findViewById<Button>(R.id.btnPhotoStudio).setOnClickListener {
            val dressUpLayout = findViewById<RelativeLayout>(R.id.dressUpLayout)
            val toggleBar = findViewById<LinearLayout>(R.id.toggleBar)

            dressUpLayout.post {
                try {

                    toggleBar.visibility = View.GONE


                    val bitmap = Bitmap.createBitmap(
                        dressUpLayout.width,
                        dressUpLayout.height,
                        Bitmap.Config.RGB_565
                    )
                    val canvas = Canvas(bitmap)
                    dressUpLayout.draw(canvas)


                    toggleBar.visibility = View.VISIBLE


                    val stream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream)

                    val intent = Intent(this, PhotoStudioActivity::class.java).apply {
                        putExtra("bunny_bitmap", stream.toByteArray())
                    }
                    startActivity(intent)

                } catch (e: Exception) {
                    toggleBar.visibility = View.VISIBLE
                    Toast.makeText(this, "Failed to prepare photo", Toast.LENGTH_SHORT).show()
                }
            }
        }



    }


    // Drag and drop functionality
    inner class ScalingDragTouchListener : View.OnTouchListener {
        override fun onTouch(view: View, event: MotionEvent): Boolean {
            return try {
                if (event.action == MotionEvent.ACTION_DOWN && view is ImageView) {

                    // Ensure view has proper tag
                    val itemTag = view.tag?.toString()
                    if (itemTag.isNullOrEmpty()) {
                        Log.e("DragDrop", "View has no tag, cannot drag")
                        return false
                    }


                    val shadow = View.DragShadowBuilder(view)


                    view.startDragAndDrop(null, shadow, view, 0)

                    Log.d("DragDrop", "Started drag for item: $itemTag")
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                Log.e("DragDrop", "Error in touch listener", e)
                false
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SHOP_REQUEST_CODE && resultCode == RESULT_OK) {
            val username = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                .getString("username", "") ?: return


            budgetViewModel.refreshCoins(username)
            loadPurchasedItems(username)


            data?.getIntExtra("NEW_COINS", -1)?.takeIf { it >= 0 }?.let { coins ->
                coinCountText.text = "Coins: $coins"
            }
        }
    }

    //This is so the app can be given permission to use the camera
    companion object {
        const val SHOP_REQUEST_CODE = 1001

    }

    //This is functionality to be able to drag and drop the clothes
    private val dragListener = View.OnDragListener { v, event ->
        when (event.action) {
            DragEvent.ACTION_DRAG_STARTED -> {

                true
            }

            DragEvent.ACTION_DRAG_ENTERED -> {

                true
            }

            DragEvent.ACTION_DRAG_EXITED -> {

                true
            }

            DragEvent.ACTION_DROP -> {
                try {
                    val draggedView = event.localState as? ImageView ?: return@OnDragListener false
                    val dressUpLayout = findViewById<RelativeLayout>(R.id.dressUpLayout)


                    val dropX = event.x
                    val dropY = event.y


                    val droppedView = ImageView(this@BunnyActivity).apply {
                        setImageDrawable(draggedView.drawable)
                        tag = draggedView.tag

                        // Size when dragging clothes (This is different to the ones in closet so it can fit the bunny)
                        val dragSize = when (draggedView.tag.toString()) {
                            "jumpsuit_1", "jumpsuit_2" -> 110.dpToPx() to 140.dpToPx()
                            "blue_shirt" -> 450.dpToPx() to 520.dpToPx()
                            "brat_shirt" -> 450.dpToPx() to 520.dpToPx()
                            "KCP_shirt" -> 450.dpToPx() to 520.dpToPx()
                            "coffee_shirt" -> 460.dpToPx() to 520.dpToPx()
                            "sideman_hoodie" -> 500.dpToPx() to 500.dpToPx()
                            "cap" -> 400.dpToPx() to 500.dpToPx()
                            "skirt" -> 390.dpToPx() to 480.dpToPx()
                            else -> 500.dpToPx() to 500.dpToPx()
                        }

                        layoutParams = RelativeLayout.LayoutParams(dragSize.first, dragSize.second).apply {

                            leftMargin = (dropX - dragSize.first/2).toInt().coerceAtLeast(0)
                            topMargin = (dropY - dragSize.second/2).toInt().coerceAtLeast(0)
                        }

                        scaleType = ImageView.ScaleType.FIT_CENTER
                        adjustViewBounds = true
                        setOnTouchListener(MovingTouchListener())
                    }


                    dressUpLayout.addView(droppedView)
                    dressUpLayout.post {

                        droppedView.x = (dropX - droppedView.width/2).coerceIn(0f, (dressUpLayout.width - droppedView.width).toFloat())
                        droppedView.y = (dropY - droppedView.height/2).coerceIn(0f, (dressUpLayout.height - droppedView.height).toFloat())
                    }

                    Log.d("DragDrop", "Dropped item at (${dropX}, ${dropY})")
                    Toast.makeText(this@BunnyActivity, "Item dropped! Touch and drag to move, or tap to remove", Toast.LENGTH_SHORT).show()
                    true
                } catch (e: Exception) {
                    Log.e("DragDrop", "Error in drop action", e)
                    false
                }
            }

            DragEvent.ACTION_DRAG_ENDED -> true
            else -> false
        }
    }


    //This loads all the items that have been purchased from shop
    private fun loadPurchasedItems(username: String) {
        closetContainer.removeAllViews()
        Log.d("ClosetDebug", "Loading purchased items for: $username")

        firestore.collection("UserPurchases").document(username).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val purchasedItems = (document.get("items") as? List<String>)?.distinct() ?: emptyList()
                    Log.d("ClosetDebug", "Purchased items: $purchasedItems")

                    if (purchasedItems.isNotEmpty()) {
                        firestore.collection("ShopItems")
                            .whereIn("id", purchasedItems)
                            .get()
                            .addOnSuccessListener { querySnapshot ->
                                Log.d("ClosetDebug", "Found ${querySnapshot.size()} shop items")

                                querySnapshot.documents.forEach { doc ->
                                    val itemId = doc.getString("id") ?: ""
                                    val imageName = doc.getString("imageName") ?: ""
                                    Log.d("ClosetDebug", "Processing item - ID: $itemId, ImageName: $imageName")

                                    // Check for both ID and imageName could nnot find which one exactly works
                                    when {
                                        // 1. Try exact ID match
                                        imageResourceMap.containsKey(itemId) -> addItemToClosetWithDynamicSize(itemId)
                                        // 2. Try ID with suffix removed
                                        imageResourceMap.containsKey(itemId.substringBeforeLast('_')) ->
                                            addItemToClosetWithDynamicSize(itemId.substringBeforeLast('_'))
                                        // 3. Try exact imageName match
                                        imageResourceMap.containsKey(imageName) -> addItemToClosetWithDynamicSize(imageName)
                                        // 4. Try imageName with suffix removed
                                        imageResourceMap.containsKey(imageName.substringBeforeLast('_')) ->
                                            addItemToClosetWithDynamicSize(imageName.substringBeforeLast('_'))
                                        else -> Log.e("ClosetError", "No matching resource for $itemId or $imageName")
                                    }
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("ClosetDebug", "Error loading shop items", e)
                            }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("ClosetDebug", "Error loading purchases", e)
            }
    }


    //This displays the items that have been purchased
    private fun displayPurchasedItems(itemIds: List<String>) {
        if (itemIds.isEmpty()) return

        firestore.collection("ShopItems")
            .whereIn("id", itemIds)
            .get()
            .addOnSuccessListener { querySnapshot ->

                val uniqueItems = mutableMapOf<String, String>()
                querySnapshot.documents.forEach { doc ->
                    val itemName = doc.getString("id") ?: return@forEach
                    uniqueItems[itemName] = itemName
                }


                uniqueItems.values.forEach { itemName ->
                    addItemToClosetWithDynamicSize(itemName)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error loading shop items", e)
            }
    }

    //This is a method to ensure clothes are at the closet and are big enough to be seen there.
    private fun addItemToClosetWithDynamicSize(itemName: String) {
        try {
            val resourceId = imageResourceMap[itemName] ?: run {
                Log.e("ClosetError", "No drawable found for: $itemName")
                return
            }


            closetContainer.findViewWithTag<View>(itemName)?.let {
                Log.d("ClosetDebug", "Item $itemName already exists in closet")
                return
            }

            val imageView = ImageView(this).apply {

                setLayerType(View.LAYER_TYPE_HARDWARE, null)


                setImageResource(resourceId)

                // Set size for closet display (this is different to size it will be on bunny)
                val closetSize = if (itemName.startsWith("jumpsuit")) {
                    120.dpToPx() to 160.dpToPx()
                } else {
                    200.dpToPx() to 200.dpToPx()
                }

                layoutParams = LinearLayout.LayoutParams(
                    closetSize.first,
                    closetSize.second
                ).apply {
                    marginEnd = 16.dpToPx()
                    marginStart = 8.dpToPx()
                    topMargin = 8.dpToPx()
                    bottomMargin = 8.dpToPx()
                }

                scaleType = ImageView.ScaleType.FIT_CENTER
                adjustViewBounds = true
                tag = itemName


                setOnTouchListener(ScalingDragTouchListener())


                background = ContextCompat.getDrawable(context, R.drawable.debug_border)
            }


            closetContainer.addView(imageView)
            Log.d("ClosetDebug", "Successfully added item: $itemName")

        } catch (e: Exception) {
            Log.e("ClosetError", "Failed to add item $itemName to closet", e)
        }
    }




    // This convert dp to pixels so I can control it manually (size of the images)
    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }


    //This allows for moving of the clothes
    inner class MovingTouchListener : View.OnTouchListener {
        private var dX = 0f
        private var dY = 0f
        private var isDragging = false
        private val clickThreshold = ViewConfiguration.get(this@BunnyActivity).scaledTouchSlop
        private var initialX = 0f
        private var initialY = 0f

        override fun onTouch(view: View, event: MotionEvent): Boolean {
            val dressUpLayout = findViewById<RelativeLayout>(R.id.dressUpLayout)

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isDragging = false
                    initialX = event.rawX
                    initialY = event.rawY
                    dX = view.x - event.rawX
                    dY = view.y - event.rawY
                    return true
                }

                MotionEvent.ACTION_MOVE -> {
                    val deltaX = abs(event.rawX - initialX)
                    val deltaY = abs(event.rawY - initialY)

                    if (!isDragging && (deltaX > clickThreshold || deltaY > clickThreshold)) {
                        isDragging = true
                    }

                    if (isDragging) {
                        val newX = event.rawX + dX
                        val newY = event.rawY + dY


                        val layoutLocation = IntArray(2)
                        dressUpLayout.getLocationOnScreen(layoutLocation)


                        val layoutX = newX - layoutLocation[0]
                        val layoutY = newY - layoutLocation[1]


                        view.x = layoutX.coerceIn(0f, (dressUpLayout.width - view.width).toFloat())
                        view.y = layoutY.coerceIn(0f, (dressUpLayout.height - view.height).toFloat())
                    }
                    return true
                }

                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        dressUpLayout.removeView(view)
                        Toast.makeText(this@BunnyActivity, "Item removed", Toast.LENGTH_SHORT).show()
                    }
                    return true
                }
            }
            return false
        }
    }

    //This is the method to essentially start the game all over, get all their coins back and all clothes go back to store
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

                        val purchasesRef = firestore.collection("UserPurchases").document(username)
                        val coinsRef = firestore.collection("UserCoins").document(username)


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


                        val shopItems = firestore.collection("ShopItems")
                            .whereIn("id", items).get().await()

                        val totalRefund = shopItems.sumOf { it.getLong("price")?.toInt() ?: 0 }


                        firestore.runTransaction { transaction ->

                            val coinDoc = transaction.get(coinsRef)


                            transaction.update(purchasesRef, "items", emptyList<String>())


                            val currentTotalEarned = coinDoc.getLong("totalEarned") ?: 0
                            val currentBalance = coinDoc.getLong("currentBalance") ?:
                            coinDoc.getLong("coins") ?: 0

                            val updates = hashMapOf<String, Any>(
                                "currentBalance" to (currentBalance + totalRefund),
                                "totalEarned" to (currentTotalEarned + totalRefund),
                                "lastUpdated" to FieldValue.serverTimestamp()
                            )

                            transaction.update(coinsRef, updates)
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
                                Log.e("BunnyActivity", "Transaction failed", it.exception)
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