package com.example.textrecognition

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.textrecognition.databinding.ActivityImageAnalysisBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition

class ImageAnalysis : AppCompatActivity(), ImageAnalysis.Analyzer {
    private val tag = "ImageAnalysis"
    private val requestImageCapture = 1
    private lateinit var image : InputImage
    private val recognizedStringBuilder = StringBuilder()
    private lateinit var binding : ActivityImageAnalysisBinding
    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val data: Intent? = result.data
            val imageBitmap = data?.extras?.get("data") as Bitmap
            image = InputImage.fromBitmap(imageBitmap, 0)
            binding.imageView.setImageBitmap(imageBitmap)
            binding.detectBT.isEnabled = true
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageAnalysisBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            //grant the permission
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 101)
        }
        binding.captureBT.setOnClickListener {
            dispatchTakePictureIntent()
        }
        binding.detectBT.setOnClickListener {
            detectTextFromImage(image)
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
                                recognizedStringBuilder.append(elementText).append(" ")
                            }
                            recognizedStringBuilder.appendLine()
                        }
                    }
                    binding.textFromImage.setText(recognizedStringBuilder.toString())
                }
                .addOnFailureListener { e ->
                    Log.d(tag, "detectTextFromImage: "+e)
                }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            resultLauncher.launch(takePictureIntent)
            //startActivityForResult(takePictureIntent, requestImageCapture)

        } catch (e: ActivityNotFoundException) {
            // display error state to the user
        }
    }

        @SuppressLint("UnsafeExperimentalUsageError", "UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image =
                InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            detectTextFromImage(image)
        }
    }

}
