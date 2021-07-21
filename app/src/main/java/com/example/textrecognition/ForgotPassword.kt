package com.example.textrecognition


import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth


class ForgotPassword : ConstraintLayout {
    private var m_ForgotPasswordDialog: Dialog? = null

    constructor(context: Context) : super(context) {
        m_ForgotPasswordDialog = Dialog(context)
        setDialogSettings()
        setOnCLickRecover()
    }

    private fun setOnCLickRecover() {
        val recoverLostPassword =
            m_ForgotPasswordDialog!!.findViewById<Button>(R.id.recoverPasswordBT)
        recoverLostPassword.setOnClickListener { view: View? -> recoverLostPassword() }
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
    }

    private fun recoverLostPassword() {
        val m_EmailTextView =
            m_ForgotPasswordDialog!!.findViewById<TextView>(R.id.editTextForgotEmail)
        val email = m_EmailTextView.text.toString()
        if (!email.isEmpty()) {
            val auth = FirebaseAuth.getInstance()
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task: Task<Void?> ->
                    if (task.isSuccessful) {
                        Toast.makeText(context, R.string.emailsent, Toast.LENGTH_LONG).show()
                        m_ForgotPasswordDialog!!.dismiss()
                    } else m_EmailTextView.setHintTextColor(Color.RED)
                }
        } else m_EmailTextView.setHintTextColor(Color.RED)
    }

    private fun setDialogSettings() {
        m_ForgotPasswordDialog!!.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        m_ForgotPasswordDialog.setContentView(R.layout.dialog_forgotpassword)
        m_ForgotPasswordDialog!!.show()
        m_ForgotPasswordDialog!!.setCanceledOnTouchOutside(true)
        setDialogWidthAndHeight()
    }

    private fun setDialogWidthAndHeight() {
        val metrics = resources.displayMetrics
        val m_Width = metrics.widthPixels
        val m_Height = metrics.heightPixels
        m_ForgotPasswordDialog!!.window!!.setLayout(6 * m_Width / 7, 4 * m_Height / 5)
    }
}