package com.example.textrecognition

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import java.util.concurrent.Executor

class MainActivity : AppCompatActivity() {
    private lateinit var executor : Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private val bundle = Bundle()


    //    private var mContext:Context = this@MainActivity
    private var prefs : SharedPreferences? = null
//    private val prefs = PreferenceManager.getDefaultSharedPreferences(mContext)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
    fun executeLoginWithEmail(view: View) {
        Login(this)
        prefs = this?.getPreferences(Context.MODE_PRIVATE)
        bundle.putBoolean("WithFingerprint",false)
        val editor = prefs?.edit()
        editor?.putBoolean("Fingerprint", false)
        editor?.apply()
    }

    fun executeLoginWithFingerprint(view: View) {
        val fragment = ImageAnalysisFragment()
        fragment.arguments = bundle
        prefs = this?.getPreferences(Context.MODE_PRIVATE)
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    bundle.putBoolean("WithFingerprint",true)
                    val editor = prefs?.edit()
                    editor?.putBoolean("Fingerprint", true)
                    editor?.apply()
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
