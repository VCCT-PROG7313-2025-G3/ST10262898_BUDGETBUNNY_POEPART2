package com.fake.st10262898_budgetbunny_poepart2

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
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
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class PhotoStudioActivity : AppCompatActivity() {

    private lateinit var bunnyContainer: RelativeLayout
    private lateinit var backgroundImage: ImageView
    private lateinit var bunnyImage: ImageView
    private lateinit var backgroundSelector: LinearLayout
    private var currentBackgroundRes = R.drawable.bg_lightning
    //These are the clothes that the bunny was supposed to be able to wear:
    private val imageResourceMap = mapOf(
        "jumpsuit_1" to R.drawable.jumpsuit_1,
        "jumpsuit_2" to R.drawable.jumpsuit_2
    )



    //This allows a requests for permission to be made
    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
        private const val FILENAME_FORMAT = "yyyyMMdd_HHmmss"
    }



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

        // Load dressed items
        val dressedItemsArray = intent.getStringArrayExtra("dressed_items") ?: emptyArray()
        createDressedBunny(dressedItemsArray)
        setupBackgroundSelector()

        // Set up buttons
        findViewById<Button>(R.id.btnCapture).setOnClickListener {
            if (checkPermissions()) {
                captureAndSaveBunny()
            }
        }
        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }
    }


    private fun captureAndSaveBunny() {
        try {
            val bitmap = Bitmap.createBitmap(
                bunnyContainer.width,
                bunnyContainer.height,
                Bitmap.Config.ARGB_8888
            ).apply {
                val canvas = Canvas(this)
                bunnyContainer.draw(canvas)
            }
            saveImageToGallery(bitmap)
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to capture photo", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun saveImageToGallery(bitmap: Bitmap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            saveImageUsingMediaStore(bitmap)
        } else {

            saveImageLegacy(bitmap)
        }
    }

    private fun saveImageUsingMediaStore(bitmap: Bitmap) {
        val dateFormatter = SimpleDateFormat(FILENAME_FORMAT, Locale.getDefault())
        val fileName = "Bunny_${dateFormatter.format(Date())}.jpg"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/BudgetBunny")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val contentResolver = contentResolver
        var outputStream: OutputStream? = null
        var uri: Uri? = null

        try {
            uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let {
                outputStream = contentResolver.openOutputStream(it)
                outputStream?.use { stream ->
                    if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)) {
                        Toast.makeText(this, "Photo saved to gallery!", Toast.LENGTH_SHORT).show()
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    contentResolver.update(uri, contentValues, null, null)
                }
            }
        } catch (e: IOException) {
            uri?.let { contentResolver.delete(it, null, null) }
            Toast.makeText(this, "Failed to save photo", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun saveImageLegacy(bitmap: Bitmap) {
        val dateFormatter = SimpleDateFormat(FILENAME_FORMAT, Locale.getDefault())
        val fileName = "Bunny_${dateFormatter.format(Date())}.jpg"
        val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val imageFile = File(storageDir, "BudgetBunny/$fileName")

        try {
            imageFile.parentFile?.mkdirs()
            FileOutputStream(imageFile).use { fos ->
                if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)) {

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
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            true
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED || requestStoragePermission()
        }
    }

    private fun requestStoragePermission(): Boolean {
        return if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            showPermissionRationale()
            false
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_CODE
            )
            false
        }
    }




    private fun createDressedBunny(dressedItems: Array<String>) {

        bunnyImage.setImageResource(R.drawable.budgetbunny_wamsta_two)


        for (i in bunnyContainer.childCount - 1 downTo 0) {
            val view = bunnyContainer.getChildAt(i)
            if (view != bunnyImage) {
                bunnyContainer.removeView(view)
            }
        }


        dressedItems.forEach { itemData ->
            val parts = itemData.split(",")
            if (parts.size == 3) {
                val itemName = parts[0]
                val xPos = parts[1].toFloat()
                val yPos = parts[2].toFloat()

                imageResourceMap[itemName]?.let { resId ->
                    ImageView(this).apply {
                        setImageResource(resId)
                        layoutParams = RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.WRAP_CONTENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            width = 380
                            height = 380
                        }
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
