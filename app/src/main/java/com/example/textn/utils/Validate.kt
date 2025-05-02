package com.example.textn.utils

import android.util.Patterns
import android.widget.EditText

class Validate {

    fun validateInput(email: String, password: String, emailField: EditText, passwordField: EditText): Boolean {
        if (email.isEmpty()) {
            emailField.error = "Email không được để trống"
            emailField.requestFocus()
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailField.error = "Email không đúng định dạng"
            emailField.requestFocus()
            return false
        }

        if (password.isEmpty()) {
            passwordField.error = "Mật khẩu không được để trống"
            passwordField.requestFocus()
            return false
        }

        if (password.length < 6) {
            passwordField.error = "Mật khẩu phải có ít nhất 6 ký tự"
            passwordField.requestFocus()
            return false
        }

        return true
    }

    fun validateRegister(
        email: String,
        password: String,
        confirmPassword: String,
        emailField: EditText,
        passwordField: EditText,
        confirmPasswordField: EditText
    ): Boolean {
        // Validate cơ bản
        if (!validateInput(email, password, emailField, passwordField)) {
            return false
        }

        // Validate confirm password
        if (confirmPassword.isEmpty()) {
            confirmPasswordField.error = "Vui lòng xác nhận mật khẩu"
            confirmPasswordField.requestFocus()
            return false
        }

        if (password != confirmPassword) {
            confirmPasswordField.error = "Mật khẩu xác nhận không khớp"
            confirmPasswordField.requestFocus()
            return false
        }

        return true
    }
}