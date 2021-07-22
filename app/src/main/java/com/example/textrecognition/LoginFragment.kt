package com.example.textrecognition

import android.app.KeyguardManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.UserNotAuthenticatedException
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.textrecognition.databinding.FragmentLoginBinding
import com.google.firebase.database.core.Constants
import java.io.UnsupportedEncodingException
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException
import java.util.concurrent.Executor
import javax.crypto.*


class LoginFragment : Fragment(R.layout.fragment_login) {
    private val KEY_NAME = "key"
    private lateinit var executor : Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var binding: FragmentLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    private val CHARSET_NAME = "UTF-8"
    private val ANDROID_KEY_STORE = "AndroidKeyStore"
    private val TRANSFORMATION =
        (KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/"
                + KeyProperties.ENCRYPTION_PADDING_PKCS7)

    private val AUTHENTICATION_DURATION_SECONDS = 30

    private val keyguardManager: KeyguardManager? = null
    private val SAVE_CREDENTIALS_REQUEST_CODE = 1


    @Throws(
        NoSuchPaddingException::class,
        NoSuchAlgorithmException::class,
        InvalidKeyException::class,
        UnsupportedEncodingException::class,
        BadPaddingException::class,
        IllegalBlockSizeException::class
    )
    fun saveUserPin(pin: String) {
        // encrypt the password
        try {
            val secretKey: SecretKey = createKey()
            val cipher: Cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val encryptionIv: ByteArray = cipher.getIV()
            val passwordBytes = pin.toByteArray(charset(CHARSET_NAME))
            val encryptedPasswordBytes: ByteArray = cipher.doFinal(passwordBytes)
            val encryptedPassword: String = Base64.encodeToString(encryptedPasswordBytes, Base64.DEFAULT)

            // store the login data in the shared preferences
            // only the password is encrypted, IV used for the encryption is stored
            val sharedPreferences: SharedPreferences? = context?.getSharedPreferences("sharedPrefFile",Context.MODE_PRIVATE)
            val editor: SharedPreferences.Editor? =  sharedPreferences?.edit()
            editor?.putString("password", encryptedPassword)
            editor?.putString("encryptionIv", Base64.encodeToString(encryptionIv, Base64.DEFAULT))
            editor?.apply()
        } catch (e: UserNotAuthenticatedException) {
            e.printStackTrace()
           /* showAuthenticationScreen(SAVE_CREDENTIALS_REQUEST_CODE)*/
        }
    }

    private fun createKey(): SecretKey {
        return try {
            val keyGenerator: KeyGenerator =
                KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
            keyGenerator.init(
                KeyGenParameterSpec.Builder(
                    KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setUserAuthenticationValidityDurationSeconds(AUTHENTICATION_DURATION_SECONDS)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build()
            )
            keyGenerator.generateKey()
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Failed to create a symmetric key", e)
        } catch (e: NoSuchProviderException) {
            throw RuntimeException("Failed to create a symmetric key", e)
        } catch (e: InvalidAlgorithmParameterException) {
            throw RuntimeException("Failed to create a symmetric key", e)
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        inflater.inflate(R.layout.fragment_login, container, false)
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        executor = ContextCompat.getMainExecutor(context)
        biometricPrompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(context, "auth error $errString", Toast.LENGTH_LONG).show()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    val transaction = getFragmentManager()?.beginTransaction()
                    transaction?.replace(R.id.container, ImageAnalysisFragment())
                    transaction?.commit()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(context, "auth Failed ", Toast.LENGTH_LONG).show()
                }
            })


        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric login")
            .setSubtitle("Log in using biometric fingerprint")
            .setNegativeButtonText("Close")
            .build()

        binding.button.setOnClickListener {
            biometricPrompt.authenticate(promptInfo)
        }
        biometricPrompt.authenticate(promptInfo)

    }
    companion object {
        @JvmStatic
        fun newInstance(): LoginFragment {
            return LoginFragment()
        }
    }
}