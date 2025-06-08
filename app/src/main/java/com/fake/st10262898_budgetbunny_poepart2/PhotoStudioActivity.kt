package com.fake.st10262898_budgetbunny_poepart2

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class PhotoStudioActivity : AppCompatActivity() {

    private lateinit var bunnyContainer: RelativeLayout
    private lateinit var backgroundImage: ImageView
    private lateinit var bunnyImage: ImageView
    private lateinit var backgroundSelector: LinearLayout
    private var currentBackgroundRes = R.drawable.bg_lightning

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }

    // Background resources
    private val backgrounds = listOf(
        R.drawable.bg_wood,
        R.drawable.bg_nodate,
        R.drawable.bg_lightning,
        R.drawable.bg_sunset,
        R.drawable.bg_study
    )

    private val imageResourceMap = mapOf(
        "jumpsuit_1" to R.drawable.jumpsuit_1,
        "jumpsuit_2" to R.drawable.jumpsuit_2
        // Add all your clothing items here
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_studio)

        // Initialize views
        bunnyContainer = findViewById(R.id.bunnyContainer)
        backgroundImage = findViewById(R.id.backgroundImage)
        bunnyImage = findViewById(R.id.bunnyImage)
        backgroundSelector = findViewById(R.id.backgroundSelector)

        // Set initial background
        backgroundImage.setImageResource(currentBackgroundRes)

        // Load the dressed bunny from intent
        /*val byteArray = intent.getByteArrayExtra("bunny_bitmap") ?: run {
            Toast.makeText(this, "Failed to load bunny", Toast.LENGTH_SHORT).show()
            finish()
            return

        }


        val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        bunnyImage.setImageBitmap(bitmap)*/

        // Load the dressed items from intent
        val dressedItemsArray = intent.getStringArrayExtra("dressed_items") ?: emptyArray()

        createDressedBunny(dressedItemsArray)

        // Set up background selection
        setupBackgroundSelector()

        // Set up capture button
        findViewById<Button>(R.id.btnCapture).setOnClickListener {
            if (checkPermissions()) {
                captureAndSaveBunny()
            }
        }

        // Set up back button
        findViewById<Button>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }


    private fun createDressedBunny(dressedItems: Array<String>) {
        // Add base bunny image
        bunnyImage.setImageResource(R.drawable.budgetbunny_wamsta_two) // Make sure you have this drawable


        dressedItems.forEach { itemData ->
            val parts = itemData.split(",")
            if (parts.size == 3) {
                val itemName = parts[0]
                val xPos = parts[1].toFloat()
                val yPos = parts[2].toFloat()

                imageResourceMap[itemName]?.let { resId ->
                    ImageView(this).apply {
                        setImageResource(resId)
                        layoutParams = RelativeLayout.LayoutParams(380, 380)
                        x = xPos
                        y = yPos
                        bunnyContainer.addView(this)
                    }
                }
            }
        }
    }

    private fun setupBackgroundSelector() {
        backgroundSelector.removeAllViews()
        listOf(
            R.drawable.bg_wood,
            R.drawable.bg_nodate,
            R.drawable.bg_lightning,
            R.drawable.bg_sunset,
            R.drawable.bg_study
        ).forEach { bgRes ->
            ImageView(this).apply {
                setImageResource(bgRes)
                layoutParams = LinearLayout.LayoutParams(200, 200).apply {
                    marginEnd = 16
                }
                setOnClickListener {
                    currentBackgroundRes = bgRes
                    backgroundImage.setImageResource(bgRes)
                }
                backgroundSelector.addView(this)
            }
        }
    }

    private fun captureAndSaveBunny() {
        try {
            // Create bitmap from the view
            val bitmap = Bitmap.createBitmap(
                bunnyContainer.width,
                bunnyContainer.height,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            bunnyContainer.draw(canvas)

            // Save to gallery
            saveImageToGallery(bitmap)
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to capture photo", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun saveImageToGallery(bitmap: Bitmap) {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "Bunny_$timeStamp.jpg"

        try {
            // Save to external storage
            val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            storageDir?.mkdirs()
            val imageFile = File(storageDir, fileName)

            FileOutputStream(imageFile).use { fos ->
                if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)) {
                    // Add to media store
                    MediaStore.Images.Media.insertImage(
                        contentResolver,
                        imageFile.absolutePath,
                        fileName,
                        "Budget Bunny Photo"
                    )

                    // Notify gallery
                    sendBroadcast(
                        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).apply {
                            data = Uri.fromFile(imageFile)
                        }
                    )

                    Toast.makeText(this, "Photo saved to gallery!", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: IOException) {
            Toast.makeText(this, "Failed to save photo", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun checkPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    true
                }
                shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
                    showPermissionRationale()
                    false
                }
                else -> {
                    requestPermissions(
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        PERMISSION_REQUEST_CODE
                    )
                    false
                }
            }
        } else {
            true
        }
    }

    private fun showPermissionRationale() {
        AlertDialog.Builder(this)
            .setTitle("Permission Needed")
            .setMessage("This permission is needed to save your bunny photos")
            .setPositiveButton("OK") { _, _ ->
                requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE
                )
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                captureAndSaveBunny()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}