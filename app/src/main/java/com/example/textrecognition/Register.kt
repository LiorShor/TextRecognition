package com.example.textrecognition

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class Register @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet?,
    defStyle: Int = 0
) :
    ConstraintLayout(context!!, attrs, defStyle) {
    private var m_EditTextPersonName: EditText? = null
    private var m_EditTextEmail: EditText? = null
    private var m_EditTextRegisterPassword: EditText? = null
    private var m_EditTextRePassword: EditText? = null
    private var m_RegisterDialog: Dialog? = null
    private var m_Auth: FirebaseAuth? = null

    constructor(context: Context?) : this(context, null, 0) {
        m_RegisterDialog = Dialog(context!!)
        setDialogSettings()
        m_EditTextPersonName = m_RegisterDialog!!.findViewById(R.id.editTextPersonName)
        m_EditTextEmail = m_RegisterDialog!!.findViewById(R.id.editTextEmail)
        m_EditTextRegisterPassword = m_RegisterDialog!!.findViewById(R.id.editTextRegisterPassword)
        m_EditTextRePassword = m_RegisterDialog!!.findViewById(R.id.editTextRePassword)
        m_Auth = FirebaseAuth.getInstance()
        setOnClickRegisterButton()
    }

    private fun setDialogSettings() {
        m_RegisterDialog!!.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        m_RegisterDialog!!.setContentView(R.layout.dialog_register)
        m_RegisterDialog!!.show()
        m_RegisterDialog!!.setCanceledOnTouchOutside(true)
        setDialogWidthAndHeight()
    }

    private fun setDialogWidthAndHeight() {
        val metrics = resources.displayMetrics
        val m_Width = metrics.widthPixels
        val m_Height = metrics.heightPixels
        m_RegisterDialog!!.window!!.setLayout(6 * m_Width / 7, 4 * m_Height / 5)
    }

    private fun setOnClickRegisterButton() {
        val RegisterButton = m_RegisterDialog!!.findViewById<Button>(R.id.registerBT)
        RegisterButton.setOnClickListener { view: View? ->
            val name = m_EditTextPersonName!!.text.toString()
            val email = m_EditTextEmail!!.text.toString()
            val password = m_EditTextRegisterPassword!!.text.toString()
            val rePassword = m_EditTextRePassword!!.text.toString()
            if (validation(name, email, password, rePassword)) writeNewUser(password, email, name)
        }
    }

    fun writeNewUser(password: String?, email: String?, name: String?) {
        val user = User(name, email)
        m_Auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(context as Activity) { task ->
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    val firebaseUser: FirebaseUser = m_Auth.getCurrentUser()!!
                    val uid: String = firebaseUser.getUid()
                    // Write a user to the database
                    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
                    val databaseReference: DatabaseReference =
                        database.getReference("users").child(uid)
                    databaseReference.setValue(user)
                    m_RegisterDialog!!.dismiss()
                    //todo: replace this fragment with TranslatedViewModel
                } else {
                    Toast.makeText(context, "Error signing in", Toast.LENGTH_SHORT).show()
                    // If sign in fails, display a message to the user.
                }
            }
    }

    private fun validation(
        name: String,
        email: String,
        password: String,
        rePassword: String
    ): Boolean {
        var validationSuccess = true
        if (name.isEmpty()) {
            m_EditTextPersonName!!.setHintTextColor(Color.RED)
            validationSuccess = false
        }
        if (email.isEmpty()) {
            m_EditTextEmail!!.setHintTextColor(Color.RED)
            validationSuccess = false
        }
        if (rePassword == "") {
            m_EditTextRePassword!!.setHintTextColor(Color.RED)
            validationSuccess = false
        }
        if (password == "") {
            m_EditTextRegisterPassword!!.setHintTextColor(Color.RED)
            validationSuccess = false
        }
        if (password != rePassword) {
            m_EditTextRegisterPassword!!.setTextColor(Color.RED)
            m_EditTextRePassword!!.setTextColor(Color.RED)
            validationSuccess = false
        }
        return validationSuccess
    }
}