package com.fake.st10262898_budgetbunny_poepart2

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class HowToPlayActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_how_to_play)

        // Set click listener on the overlay
        findViewById<View>(R.id.overlay).setOnClickListener {
            returnToBunnyActivity()
        }

        // Keep your existing insets listener but update the ID
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainScrollView)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                systemBars.left,
                systemBars.top + 50,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }

        setupHowToPlayText()
    }

    private fun returnToBunnyActivity() {
        val intent = Intent(this, BunnyActivity::class.java)
        startActivity(intent)
        finish()
        overridePendingTransition(0, 0)
    }


    private fun setupHowToPlayText() {
        val howToPlayText = findViewById<TextView>(R.id.howToPlayText)

        val htmlText = """
            <h1 style="color:#FF6B6B;text-align:center;">🌟 How to Play Budget Bunny 🌟</h1>
            <br/>
            <h2 style="color:#4ECDC4;">1. Earn Coins 💰</h2>
            <p>• For every <b>R10</b> you save in real life, you get <b>1 coin</b>!</p>
            <p>• Example: Save <b>R100</b> = <b>10 coins</b> to spend!</p>
            <br/>
            <h2 style="color:#4ECDC4;">2. Shop & Style 🛍️</h2>
            <p>• Use your coins to buy cute clothes for your bunny!</p>
            <p>• Mix and match for a <b>unique look</b>.</p>
            <br/>
            <h2 style="color:#4ECDC4;">3. Show Off & Reset 🔄</h2>
            <p>• Love your bunny's fit? <b>Take a screenshot</b>!</p>
            <p>• Changed your mind? <b>RESET</b> returns items and coins.</p>
            <br/>
            <p style="text-align:center;font-size:1.2em;">💰 <b>Save money → Earn coins → Dress up → Repeat!</b> 💰</p>
        """.trimIndent()

        howToPlayText.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(htmlText, Html.FROM_HTML_MODE_COMPACT)
        } else {
            @Suppress("DEPRECATION")
            Html.fromHtml(htmlText)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            returnToBunnyActivity()
            return true
        }
        return super.onTouchEvent(event)
    }
}