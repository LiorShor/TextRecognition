package com.example.textrecognition.view.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.textrecognition.R
import com.example.textrecognition.databinding.DialogForgotPasswordBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth

class ForgotPassword(context: Context) : ConstraintLayout(context) {
    private var mForgotPasswordDialog = Dialog(context)
    private lateinit var mBinding: DialogForgotPasswordBinding

    init {
        setDialogSettings()
        setOnCLickRecover()
    }

    private fun setOnCLickRecover() {
        mBinding.recoverPasswordBT.setOnClickListener { recoverLostPassword() }
    }

    private fun recoverLostPassword() {
        val email = mBinding.editTextForgotEmail.text.toString()
        if (email.isNotEmpty()) {
            val auth = FirebaseAuth.getInstance()
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task: Task<Void?> ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.email_sent),
                            Toast.LENGTH_LONG
                        ).show()
                        mForgotPasswordDialog.dismiss()
                    } else {
                        mBinding.editTextForgotEmail.setHintTextColor(Color.RED)
                    }
                }
        } else { 
            mBinding.editTextForgotEmail.setHintTextColor(Color.RED)
        }
    }

    private fun setDialogSettings() {
        mBinding = DialogForgotPasswordBinding.inflate(LayoutInflater.from(context))
        mForgotPasswordDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        mForgotPasswordDialog.setContentView(mBinding.root)
        mForgotPasswordDialog.show()
        mForgotPasswordDialog.setCanceledOnTouchOutside(true)
        setDialogWidthAndHeight()
    }

    private fun setDialogWidthAndHeight() {
        val metrics = resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        mForgotPasswordDialog.window?.setLayout(6 * width / 7, 4 * height / 5)
    }
}
