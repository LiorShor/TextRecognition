package com.example.textrecognition

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.textrecognition.databinding.FragmentImageAnalysisBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizerOptions
import java.lang.Exception
import java.lang.reflect.Type


class ImageAnalysisFragment : Fragment(R.layout.fragment_image_analysis), ImageAnalysis.Analyzer {

    private val TAG = "ImageAnalysisFragment"
    private val translatedTextList = ArrayList<String>()
    private val sourceTextList = ArrayList<String>()
    private var clicked = false
    private lateinit var image: InputImage
    private val recognizedStringBuilder = StringBuilder()
    private lateinit var binding: FragmentImageAnalysisBinding
    private lateinit var bundle: Bundle

    private val rotateOpen: Animation by lazy {
        AnimationUtils.loadAnimation(
            context,
            R.anim.rotate_open_anim
        )
    }
    private val rotateClose: Animation by lazy {
        AnimationUtils.loadAnimation(
            context,
            R.anim.rotate_close_anim
        )
    }
    private val fromBottom: Animation by lazy {
        AnimationUtils.loadAnimation(
            context,
            R.anim.from_bottom_anim
        )
    }
    private val toBottom: Animation by lazy {
        AnimationUtils.loadAnimation(
            context,
            R.anim.to_bottom_anim
        )
    }
    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Intent? = result.data
                val imageBitmap = data?.extras?.get("data") as Bitmap
                image = InputImage.fromBitmap(imageBitmap, 0)
                binding.imageView.setImageBitmap(imageBitmap)
                binding.detectBT.isEnabled = true
            }
        }
    private val mPermissionResult = registerForActivityResult(
        RequestPermission()
    ) { result ->
        if (result) {
            Log.e(tag, "onActivityResult: PERMISSION GRANTED")
        } else {
            Log.e(tag, "onActivityResult: PERMISSION DENIED")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (context?.let {
                checkSelfPermission(
                    it,
                    Manifest.permission.CAMERA
                )
            } != PackageManager.PERMISSION_GRANTED) {
            mPermissionResult.launch(Manifest.permission.CAMERA)
        }
        try {
            sourceTextList.addAll(getArrayList("source"))
            translatedTextList.addAll(getArrayList("translated"))
        } catch (e: Exception) {
            Log.d(TAG, "Database is empty")
        }
        bundle = Bundle()
        bundle.putSerializable("translatedArray", translatedTextList)
        bundle.putSerializable("sourceArray", sourceTextList)
        //setHasOptionsMenu(false) check if needed
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        inflater.inflate(R.layout.fragment_image_analysis, container, false)
        binding = FragmentImageAnalysisBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewModel = ViewModelProvider(this).get(
            TranslateViewModel::class.java
        )
        val adapter = context?.let {
            ArrayAdapter(
                it,
                android.R.layout.simple_spinner_dropdown_item, viewModel.availableLanguages
            )
        }
        binding.captureBT.setOnClickListener {
            dispatchTakePictureIntent()
        }
        binding.detectBT.setOnClickListener {
            detectTextFromImage(image)
        }

        binding.addFloatingActionButton.setOnClickListener {
            onAddButtonClicked()
        }
        binding.takePhotofloatingActionButton.setOnClickListener {

        }
        binding.historyFloatingActionButton.setOnClickListener {
            val transaction = getFragmentManager()?.beginTransaction()
            val fragment = HistoryFragment()
            if (binding.textFromImage.text.toString() != "") {
                sourceTextList.add(binding.textFromImage.text.toString())
                translatedTextList.add(binding.translatedText.text.toString())
                saveArrayList(sourceTextList, "source")
                saveArrayList(translatedTextList, "translated")
            }
            if (sourceTextList.size != 0) {
                fragment.arguments = bundle
                transaction?.replace(R.id.container, fragment)?.addToBackStack(null)
                transaction?.commit()
            } else {
                Toast.makeText(
                    context,
                    "It appears that you don't have any history",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        binding.clearText.setOnClickListener {
            binding.textFromImage.setText("")
            binding.clearText.visibility = View.INVISIBLE
        }
        binding.buttonSwitchLang.setOnClickListener {
            val sourceLangPosition = binding.sourceLangSelector.selectedItemPosition
            binding.sourceLangSelector.setSelection(binding.targetLangSelector.selectedItemPosition)
            binding.targetLangSelector.setSelection(sourceLangPosition)
        }

        binding.sourceLangSelector.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    setProgressText()
                    if (adapter != null) {
                        viewModel.sourceLang.value = adapter.getItem(position)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    binding.translatedText.text = ""
                }
            }
        binding.targetLangSelector.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    setProgressText()
                    if (adapter != null) {
                        viewModel.targetLang.value = adapter.getItem(position)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    binding.translatedText.text = ""
                }
            }
        binding.textFromImage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                setProgressText()
                viewModel.sourceText.postValue(s.toString())
            }
        })
        viewModel.translatedText.observe(
            viewLifecycleOwner,
            { resultOrError ->
                if (resultOrError.error != null) {
                    binding.textFromImage.error = resultOrError.error!!.localizedMessage
                } else {
                    binding.translatedText.text = resultOrError.result
                }
            }
        )
        binding.sourceLangSelector.adapter = adapter
        binding.targetLangSelector.adapter = adapter
    }

    private fun setProgressText() {
        binding.translatedText.setText((R.string.translating))
    }

    private fun onAddButtonClicked() {
        setVisibility(clicked)
        setAnimation(clicked)
        clicked = !clicked
    }

    private fun setVisibility(clicked: Boolean) {
        if (!clicked) {
            binding.takePhotofloatingActionButton.visibility = View.VISIBLE
            binding.historyFloatingActionButton.visibility = View.VISIBLE
        } else {
            binding.takePhotofloatingActionButton.visibility = View.INVISIBLE
            binding.historyFloatingActionButton.visibility = View.INVISIBLE
        }
    }

    private fun setAnimation(clicked: Boolean) {
        if (!clicked) {
            binding.historyFloatingActionButton.startAnimation(fromBottom)
            binding.takePhotofloatingActionButton.startAnimation(fromBottom)
            binding.addFloatingActionButton.startAnimation(rotateOpen)
        } else {
            binding.historyFloatingActionButton.startAnimation(toBottom)
            binding.takePhotofloatingActionButton.startAnimation(toBottom)
            binding.addFloatingActionButton.startAnimation(rotateClose)
        }
    }

    private fun detectTextFromImage(image: InputImage) {
        val recognizer =
            TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) // instance of TextRecognizer
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
//                // Task completed successfully
//                // ...
//                val resultText = visionText.text
                for (block in visionText.textBlocks) {
//                    val blockText = block.text
//                    val blockCornerPoints = block.cornerPoints
//                    val blockFrame = block.boundingBox
                    for (line in block.lines) {
//                        val lineText = line.text
//                        val lineCornerPoints = line.cornerPoints
//                        val lineFrame = line.boundingBox
                        for (element in line.elements) {
                            val elementText = element.text
//                            val elementCornerPoints = element.cornerPoints
//                            val elementFrame = element.boundingBox
                            recognizedStringBuilder.append(elementText).append(" ")
                        }
                        recognizedStringBuilder.appendLine()
                    }
                }
                binding.textFromImage.setText(recognizedStringBuilder.toString())
                recognizedStringBuilder.clear()
            }
            .addOnFailureListener { e ->
                Log.d("ImageAnalysisFragment", "detectTextFromImage: $e")
            }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            resultLauncher.launch(takePictureIntent)
        } catch (e: ActivityNotFoundException) {
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image =
                InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            detectTextFromImage(image)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): ImageAnalysisFragment {
            return ImageAnalysisFragment()
        }
    }

    private fun saveArrayList(list: ArrayList<String>, key: String?) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        val editor = prefs.edit()
        val gson = Gson()
        val json: String = gson.toJson(list)
        editor.putString(key, json)
        editor.apply()
    }

    private fun getArrayList(key: String): ArrayList<String> {
        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        val gson = Gson()
        val json = prefs.getString(key, null)
        val type: Type = object : TypeToken<ArrayList<String?>?>() {}.type
        return gson.fromJson(json, type)
    }
}