package com.example.textrecognition

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.container,
                    LoginFragment.newInstance()
                )
                .commitNow()
        }
    }
/*    fun ExecuteLogin(view: View?) {
        Login(this)
    }*/
}
