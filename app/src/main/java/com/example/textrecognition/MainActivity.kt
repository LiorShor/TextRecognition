package com.example.textrecognition

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor

class MainActivity : AppCompatActivity() {
    private lateinit var executor : Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
    fun executeLoginWithEmail(view: View) {
        Login(this)
    }

    fun executeLoginWithFingerprint(view: View) {
        val bundle = Bundle()
        val fragment = ImageAnalysisFragment()
        fragment.arguments = bundle
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    bundle.putBoolean("WithFingerprint",true)
                        supportFragmentManager.beginTransaction()
                            .replace(
                                R.id.container,
                                fragment
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

}
