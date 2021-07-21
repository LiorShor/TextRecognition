package com.example.textrecognition

import android.R
import android.app.Dialog
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.constraintlayout.widget.ConstraintLayout
import java.util.*


class Login : ConstraintLayout {
    private var m_LoginDialog: Dialog? = null
    private var m_LoginButton: Button? = null
    private var m_SignUpEditText: TextView? = null
    private var m_ForgotPasswordTextView: TextView? = null

    constructor(context: Context) : super(context) {
        m_LoginDialog = Dialog(context)
        setDialogSettings()
        m_LoginButton = m_LoginDialog.findViewById(R.id.signIn)
        m_SignUpEditText = m_LoginDialog.findViewById(R.id.editTextSignUp)
        m_ForgotPasswordTextView = m_LoginDialog.findViewById(R.id.forgotPasswordTextView)
        onClickLoginButton(context)
        onClicksSignUpEditText(context)
        onClickForgotPassword(context)
    }

    constructor(context: Context, @Nullable attrs: AttributeSet?) : super(context, attrs) {}
    constructor(
        context: Context,
        @Nullable attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
    }

    private fun setDialogSettings() {
        m_LoginDialog.getWindow()?.setBackgroundDrawableResource(R.color.transparent)
        m_LoginDialog.setContentView(R.layout.dialog_login)
        m_LoginDialog.show()
        m_LoginDialog.setCanceledOnTouchOutside(true)
        setDialogWidthAndHeight()
    }

    private fun setDialogWidthAndHeight() {
        val metrics = resources.displayMetrics
        val m_Width = metrics.widthPixels
        val m_Height = metrics.heightPixels
        m_LoginDialog.getWindow().setLayout(6 * m_Width / 7, 4 * m_Height / 5)
    }

    private fun onClicksSignUpEditText(context: Context) {
        m_SignUpEditText!!.setOnClickListener { view: View? ->
            m_LoginDialog?.dismiss()
            Register(context)
        }
    }

    private fun onClickForgotPassword(context: Context) {
        m_ForgotPasswordTextView!!.setOnClickListener { view: View? ->
            ForgotPassword(
                context
            )
        }
    }

    private fun onClickLoginButton(context: Context) {
        m_LoginButton?.setOnClickListener { view ->
            val emailAddressEditText: EditText =
                m_LoginDialog?.findViewById(R.id.editTextTextEmailAddress)
            val passwordEditText: EditText = m_LoginDialog?.findViewById(R.id.editTextPassword) ?:
            val emailAddress = emailAddressEditText.text.toString()
            val password = passwordEditText.text.toString()
            if (stringChecker(
                    emailAddress,
                    password,
                    emailAddressEditText,
                    passwordEditText
                )
            ) {
                SignIn(emailAddress, password, context)
            }
        }
    }

    companion object {
        private val m_Auth: FirebaseAuth = FirebaseAuth.getInstance()
        private fun stringChecker(
            emailAddress: String,
            password: String,
            emailAddressEditText: EditText,
            passwordEditText: EditText
        ): Boolean {
            if (emailAddress.isEmpty()) emailAddressEditText.setHintTextColor(Color.RED)
            if (password.isEmpty()) passwordEditText.setHintTextColor(Color.RED)
            return true
        }

        fun SignIn(emailAddress: String?, password: String?, context: Context) {
            m_Auth.signInWithEmailAndPassword(emailAddress, password)
                .addOnSuccessListener { authResult ->
                    val sharedPreferences: SharedPreferences = context.getSharedPreferences(
                        "USER_DETAILS",
                        MODE_PRIVATE
                    )
                    sharedPreferences.edit().putString("userEmail", emailAddress).apply()
                    sharedPreferences.edit().putString("userPassword", password).apply()
                    getTaskListFromDB(context)
                }
        }

        private fun getTaskListFromDB(context: Context) {
            val database: FirebaseDatabase = FirebaseDatabase.getInstance()
            val firebaseUser: FirebaseUser = m_Auth.getCurrentUser()!!
            val uid: String = firebaseUser.getUid()
            val myRef: DatabaseReference = database.getReference("tasks").child(uid)
            myRef.get().addOnSuccessListener { dataSnapshot ->
                for (childDataSnapshot in dataSnapshot.getChildren()) {
                  /*  .getInstance().getTaskMap().put(
                        Objects.requireNonNull(childDataSnapshot.child("m_ID").getValue())
                            .toString(), childDataSnapshot.getValue(
                            Task::class.java
                        )
                    )*/
                }
                //todo: move to next screen
            }
        }
    }
}