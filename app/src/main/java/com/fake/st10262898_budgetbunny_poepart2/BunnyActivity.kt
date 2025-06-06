package com.fake.st10262898_budgetbunny_poepart2

import android.os.Bundle
import android.view.DragEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class BunnyActivity : AppCompatActivity() {

    private lateinit var closetContainer: LinearLayout
    private lateinit var bunnyImage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bunny)

        bunnyImage = findViewById(R.id.bunnyImage)
        closetContainer = findViewById(R.id.closetContainer)

        addClothingItems()
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
                setImageResource(imageRes) // 👈 this was missing
                setOnTouchListener(DragTouchListener())
            }
            closetContainer.addView(closetItem)
        }
    }



    // 👇 Listener for dragging clothes
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

        // Enable dropping on the whole screen
        val rootLayout = findViewById<ViewGroup>(R.id.dressUpLayout)

        rootLayout.setOnDragListener { view, event ->
            when (event.action) {
                DragEvent.ACTION_DROP -> {
                    val draggedView = event.localState as View

                    val dropX = event.x
                    val dropY = event.y

                    if (draggedView.parent != rootLayout) {
                        // Remove from old parent and add to root layout
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



}
