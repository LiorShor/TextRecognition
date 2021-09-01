package com.example.textrecognition.view.activities

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.example.textrecognition.view.fragments.ImageAnalysisFragment
import com.example.textrecognition.R
import com.example.textrecognition.view.dialogs.Login
import com.example.textrecognition.view.fragments.HistoryFragment
import com.example.textrecognition.view.fragments.ICommunicator
import com.example.textrecognition.databinding.ActivityMainBinding
import java.io.Serializable
import java.util.concurrent.Executor

class MainActivity : AppCompatActivity(), ICommunicator {
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var binding: ActivityMainBinding
    private var prefs: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.fingerPrintBT.setOnClickListener {
            executeLoginWithFingerprint()
        }
        binding.loginBT.setOnClickListener {
            executeLoginWithEmail()
        }
    }

    fun executeLoginWithEmail() {
        Login(this)
        prefs = this.getPreferences(Context.MODE_PRIVATE)
        val editor = prefs?.edit()
        editor?.putBoolean("Fingerprint", false)
        editor?.apply()
    }

    fun executeLoginWithFingerprint() {
        prefs = this.getPreferences(Context.MODE_PRIVATE)
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    binding.loginBT.visibility = View.INVISIBLE
                    binding.fingerPrintBT.visibility = View.INVISIBLE
                    val editor = prefs?.edit()
                    editor?.putBoolean("Fingerprint", true)
                    editor?.apply()
                    supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.container,
                            ImageAnalysisFragment()

                        )
                        .commitNow()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric login")
            .setSubtitle("Log in using biometric fingerprint")
            .setNegativeButtonText("Close")
            .build()
        biometricPrompt.authenticate(promptInfo)
    }

    override fun changeFragmentWithData(
        isLoggedInWithFinger: Boolean,
        translatedTextList: Serializable,
        sourceTextList: Serializable
    ) {

        val bundle = Bundle()
        val fragment = HistoryFragment()
        bundle.putBoolean("WithFingerprint", isLoggedInWithFinger)
        bundle.putSerializable("translatedArray", translatedTextList)
        bundle.putSerializable("sourceArray", sourceTextList)
        fragment.arguments = bundle
        supportFragmentManager.beginTransaction().replace(R.id.container,fragment).commit()
    }

    override fun changeFragmentWithoutData() {
        supportFragmentManager.beginTransaction().replace(R.id.container, ImageAnalysisFragment()).commit()
        binding.loginBT.visibility = View.VISIBLE
        binding.fingerPrintBT.visibility = View.VISIBLE
    }
}
