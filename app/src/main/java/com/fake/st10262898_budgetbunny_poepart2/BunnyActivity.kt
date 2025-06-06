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
            R.drawable.c_jumpsuit1,
            R.drawable.c_jumpsuit2,
        )

        clothingImages.forEach { imageRes ->
            val item = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(300, 300).apply {
                    marginEnd = 16
                }
                setImageResource(imageRes)
                setOnTouchListener(DragTouchListener())
            }
            closetContainer.addView(item)
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
        // Allow dropping on the bunny
        bunnyImage.setOnDragListener { view, event ->
            when (event.action) {
                DragEvent.ACTION_DROP -> {
                    val draggedView = event.localState as View
                    val imageView = ImageView(this).apply {
                        layoutParams = ViewGroup.LayoutParams(300, 300)
                        (draggedView as ImageView).drawable?.let { setImageDrawable(it) }
                        x = event.x - 75
                        y = event.y - 75
                    }
                    (view.parent as ViewGroup).addView(imageView)
                    true
                }
                else -> true
            }
        }
    }
}
