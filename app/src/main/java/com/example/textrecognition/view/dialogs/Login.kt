package com.example.textrecognition.view.dialogs

import android.app.Dialog
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import com.example.textrecognition.view.fragments.ImageAnalysisFragment
import com.example.textrecognition.R
import com.example.textrecognition.databinding.DialogLoginBinding
import com.google.firebase.auth.FirebaseAuth

class Login(context: Context) : ConstraintLayout(context) {
    private val mLoginDialog = Dialog(context)
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var mBinding: DialogLoginBinding

    init {
        setDialogSettings()
        onClickLoginButton(context)
        onClicksSignUpEditText(context)
        onClickForgotPassword(context)
    }

    private fun setDialogSettings() {
        mBinding = DialogLoginBinding.inflate(LayoutInflater.from(context))
        mLoginDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        mLoginDialog.setContentView(mBinding.root)
        mLoginDialog.show()
        mLoginDialog.setCanceledOnTouchOutside(true)
        setDialogWidthAndHeight()
    }

    private fun setDialogWidthAndHeight() {
        val metrics = resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        mLoginDialog.window?.setLayout(6 * width / 7, 4 * height / 5)
    }

    private fun onClicksSignUpEditText(context: Context) {
        mBinding.editTextSignUp.setOnClickListener {
            mLoginDialog.dismiss()
            Register(context)
        }
    }

    private fun onClickForgotPassword(context: Context) {
        mBinding.forgotPasswordTextView.setOnClickListener {
            ForgotPassword(
                context
            )
        }
    }

    private fun onClickLoginButton(context: Context) {
        mBinding.signIn.setOnClickListener {

            val emailAddress = mBinding.editTextTextEmailAddress.text.toString()
            val password = mBinding.editTextPassword.text.toString()
            if (stringChecker(
                    emailAddress,
                    password
                )
            ) {
                signIn(emailAddress, password, context)
            }
        }
    }

    private fun stringChecker(emailAddress: String, password: String): Boolean {
        var isValid = true
        if (emailAddress.isEmpty()) {
            mBinding.editTextTextEmailAddress.setHintTextColor(Color.RED)
            isValid = false
        }
        if (password.isEmpty()) {
            mBinding.editTextPassword.setHintTextColor(Color.RED)
            isValid = false
        }
        return isValid
    }

    private fun signIn(emailAddress: String, password: String, context: Context) {
        val bundle = Bundle()
        val fragment = ImageAnalysisFragment()
        mAuth.signInWithEmailAndPassword(emailAddress, password)
            .addOnSuccessListener {
                val sharedPreferences: SharedPreferences = context.getSharedPreferences(
                    "USER_DETAILS",
                    MODE_PRIVATE
                )
                bundle.putBoolean("WithFingerprint",false)
                sharedPreferences.edit().putString("userEmail", emailAddress).apply()
                sharedPreferences.edit().putString("userPassword", password).apply()
                mLoginDialog.dismiss()
                (context as FragmentActivity).supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.container,
                        fragment
                    )
                    .commitNow()
            }
    }
}

