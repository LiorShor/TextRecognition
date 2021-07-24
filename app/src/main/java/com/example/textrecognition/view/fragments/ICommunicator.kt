package com.example.textrecognition.view.fragments

import java.io.Serializable

interface ICommunicator {
    fun changeFragmentWithData(
        isLoggedInWithFinger: Boolean,
        translatedTextList: Serializable,
        sourceTextList: Serializable
    )

    fun changeFragmentWithoutData()
}