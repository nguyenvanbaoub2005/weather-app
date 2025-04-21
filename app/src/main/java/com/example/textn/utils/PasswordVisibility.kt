package com.example.textn.utils

import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.EditText
import android.widget.ImageView
import com.example.textn.R

class PasswordVisibility(private val passwordField: EditText, private val toggleButton: ImageView) {

    private var isPasswordVisible = false

    fun toggle() {
        if (isPasswordVisible) {
            passwordField.transformationMethod = PasswordTransformationMethod.getInstance()
            toggleButton.setImageResource(R.drawable.ic_visibility_off)
        } else {
            passwordField.transformationMethod = HideReturnsTransformationMethod.getInstance()
            toggleButton.setImageResource(R.drawable.ic_visibility)
        }
        isPasswordVisible = !isPasswordVisible
        passwordField.setSelection(passwordField.text.length)
    }
}