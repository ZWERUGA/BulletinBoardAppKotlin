package com.example.bulletinboardappkotlin

import android.app.Activity
import android.widget.Toast

object HelperFunctions {
    fun toastMessage(activity: Activity, message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }
}