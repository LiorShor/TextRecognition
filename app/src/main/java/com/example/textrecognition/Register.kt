package com.example.textrecognition

import android.R.attr
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import com.example.textrecognition.databinding.DialogRegisterBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class Register(context: Context) : ConstraintLayout(context) {

    private var mRegisterDialog = Dialog(context)
    private lateinit var mAuth: FirebaseAuth;
    private lateinit var mBinding: DialogRegisterBinding
    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mFirebaseDatabase: FirebaseDatabase
    private val TAG = "Register"

    init {
        setDialogSettings()
        mAuth = Firebase.auth
        setOnClickRegisterButton()
        mFirebaseDatabase = FirebaseDatabase.getInstance()
        mDatabaseReference = mFirebaseDatabase.getReference("Users")

    }

    private fun setDialogSettings() {
        mBinding = DialogRegisterBinding.inflate(LayoutInflater.from(context))
        mRegisterDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        mRegisterDialog.setContentView(mBinding.root)
        mRegisterDialog.show()
        mRegisterDialog.setCanceledOnTouchOutside(true)
        setDialogWidthAndHeight()
    }

    private fun setDialogWidthAndHeight() {
        val metrics = resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        mRegisterDialog!!.window!!.setLayout(6 * width / 7, 4 * height / 5)
    }

    private fun setOnClickRegisterButton() {
        mBinding.registerBT.setOnClickListener {
            val name = mBinding.editTextPersonName.text.toString()
            val email = mBinding.editTextEmail.text.toString()
            val password = mBinding.editTextRegisterPassword.text.toString()
            val rePassword = mBinding.editTextRePassword.text.toString()
            if (validation(name, email, password, rePassword)) {
                writeNewUser(password, email)
            }
        }
    }

    private fun writeNewUser(password: String, email: String) {
        val id = mDatabaseReference.push().key
        mDatabaseReference.child(id!!).setValue(User(password,email))
    }

/*        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val firebaseUser: FirebaseUser = mAuth.currentUser!!
                    val uid: String = firebaseUser.uid
                    // Write a user to the database
                    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
                    val databaseReference: DatabaseReference =
                        database.getReference("users").child(uid)
                    databaseReference.setValue(user)
                    mRegisterDialog.dismiss()
                    (context as FragmentActivity).supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.container,
                            LoginFragment.newInstance()
                        )
                        .commitNow()
                } else {
                    Toast.makeText(context, "Error signing in", Toast.LENGTH_SHORT).show()
                    // If sign in fails, display a message to the user.
                }
            }*/
/*    }*/

    private fun validation(
        name: String,
        email: String,
        password: String,
        rePassword: String
    ): Boolean {
        var validationSuccess = true
        if (name.isEmpty()) {
            mBinding.editTextPersonName.setHintTextColor(Color.RED)
            validationSuccess = false
        }
        if (email.isEmpty()) {
            mBinding.editTextEmail.setHintTextColor(Color.RED)
            validationSuccess = false
        }
        if (rePassword == "") {
            mBinding.editTextRegisterPassword.setHintTextColor(Color.RED)
            validationSuccess = false
        }
        if (password == "") {
            mBinding.editTextRePassword.setHintTextColor(Color.RED)
            validationSuccess = false
        }
        if (password != rePassword) {
            mBinding.editTextRegisterPassword.setTextColor(Color.RED)
            mBinding.editTextRePassword.setTextColor(Color.RED)
            validationSuccess = false
        }
        return validationSuccess
    }
}
