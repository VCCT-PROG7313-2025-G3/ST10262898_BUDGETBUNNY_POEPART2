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
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PhotoStudioActivity : AppCompatActivity() {

    private lateinit var bunnyContainer: RelativeLayout
    private lateinit var backgroundImage: ImageView
    private lateinit var backgroundSelector: LinearLayout
    private var currentBackgroundRes: Int = R.drawable.bg_lightning

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
        private const val FILENAME_PREFIX = "BudgetBunny_"
    }

    // Background resources
    private val backgrounds = listOf(
        R.drawable.bg_wood,
        R.drawable.bg_nodate,
        R.drawable.bg_lightning,
        R.drawable.bg_sunset,
        R.drawable.bg_study
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_studio)

        // Initialize views
        bunnyContainer = findViewById(R.id.bunnyContainer)
        backgroundImage = findViewById(R.id.backgroundImage)
        backgroundSelector = findViewById(R.id.backgroundSelector)

        // Set initial background
        backgroundImage.setImageResource(currentBackgroundRes)

        // Load the dressed bunny from intent
        val byteArray = intent.getByteArrayExtra("bunny_bitmap") ?: run {
            Toast.makeText(this, "Failed to load bunny", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        findViewById<ImageView>(R.id.bunnyImage).apply {
            setImageBitmap(bitmap)
            adjustViewBounds = true
        }

        // Set up background selection
        setupBackgroundSelector()

        // Set up capture button
        findViewById<Button>(R.id.btnCapture).setOnClickListener {
            checkPermissionsAndCapture()
        }

        // Set up back button
        findViewById<Button>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }

    private fun setupBackgroundSelector() {
        backgroundSelector.removeAllViews()
        backgrounds.forEach { bgRes ->
            val thumb = ImageView(this).apply {
                setImageResource(bgRes)
                layoutParams = LinearLayout.LayoutParams(200, 200).apply {
                    marginEnd = 16
                }
                setOnClickListener {
                    currentBackgroundRes = bgRes
                    backgroundImage.setImageResource(bgRes)
                }
            }
            backgroundSelector.addView(thumb)
        }
    }

    private fun checkPermissionsAndCapture() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            // Permission already granted or not needed (Android 10+ uses scoped storage)
            captureAndSaveBunny()
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                showPermissionRationale()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE
                )
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
            Toast.makeText(this, "Failed to capture photo: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun saveImageToGallery(bitmap: Bitmap) {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "${FILENAME_PREFIX}$timeStamp.jpg"

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
                } else {
                    Toast.makeText(this, "Failed to compress image", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: IOException) {
            Toast.makeText(this, "Failed to save photo: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        } catch (e: Exception) {
            Toast.makeText(this, "Error saving photo: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
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
                Toast.makeText(this, "Permission denied - cannot save photos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showPermissionRationale() {
        AlertDialog.Builder(this)
            .setTitle("Storage Permission Needed")
            .setMessage("This app needs storage permission to save your bunny photos to your gallery.")
            .setPositiveButton("Grant") { _, _ ->
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE
                )
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}