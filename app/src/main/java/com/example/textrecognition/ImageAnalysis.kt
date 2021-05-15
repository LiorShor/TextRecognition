package com.example.textrecognition

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import java.lang.StringBuilder

class ImageAnalysis : AppCompatActivity(), ImageAnalysis.Analyzer {
    private val TAG = "ImageAnalysis"
    private val RequestImageCapture = 1
    private lateinit var imageView: ImageView
    private lateinit var image : InputImage
    private val stringbuilder = StringBuilder()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_analysis)
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            //grant the permission
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 101)
        }
        imageView = findViewById<ImageView>(R.id.imageView)
        val captureButton = findViewById<Button>(R.id.captureBT)
        val detectButton = findViewById<Button>(R.id.detectBT)

        captureButton.setOnClickListener {
            dispatchTakePictureIntent()
        }
        detectButton.setOnClickListener {
            detectTextFromImage(image)

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RequestImageCapture && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            image = InputImage.fromBitmap(imageBitmap, 0)
            imageView.setImageBitmap(imageBitmap)

        }
    }

    private fun detectTextFromImage(image: InputImage) {
        val textFromImage = findViewById<TextView>(R.id.textFromImage)
        val recognizer = TextRecognition.getClient() // instance of TextRecognizer
        val result = recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    // Task completed successfully
                    // ...
                    val resultText = visionText.text
                    for (block in visionText.textBlocks) {
                        val blockText = block.text
                        val blockCornerPoints = block.cornerPoints
                        val blockFrame = block.boundingBox
                        for (line in block.lines) {
                            val lineText = line.text
                            val lineCornerPoints = line.cornerPoints
                            val lineFrame = line.boundingBox
                            for (element in line.elements) {
                                val elementText = element.text
                                val elementCornerPoints = element.cornerPoints
                                val elementFrame = element.boundingBox
                                stringbuilder.append(elementText).append(" ")
                            }
                            stringbuilder.appendLine()
                        }
                    }
                    textFromImage.setText(stringbuilder.toString())
                }
                .addOnFailureListener { e ->
                    Log.d(TAG, "detectTextFromImage: "+e)
                }

    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, RequestImageCapture)
        } catch (e: ActivityNotFoundException) {
            // display error state to the user
        }
    }

        @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image =
                InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            detectTextFromImage(image)
        }
    }

}
