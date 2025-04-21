package com.example.textn.utils

import android.util.Patterns
import android.widget.EditText
import android.widget.Spinner

class Validate {
    fun validateInput(email: String, password: String, emailField: EditText, passwordField: EditText): Boolean {
        if (email.isEmpty()) {
            emailField.error = "Email không được để trống"
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailField.error = "Email không đúng định dạng"
            return false
        }
        if (password.isEmpty()) {
            passwordField.error = "Mật khẩu không được để trống"
            return false
        }
        if (password.length < 6) {
            passwordField.error = "Mật khẩu phải có ít nhất 6 ký tự"
            return false
        }
        return true
    }

    fun validateRegister(
        email: String,
        password: String,
        confirmPassword: String,
        role: String,
        shopName: String,
        category: String,
        emailField: EditText,
        passwordField: EditText,
        confirmPasswordField: EditText,
        shopNameField: EditText,
        categoryField: Spinner
    ): Boolean {
        // Validate cơ bản
        if (!validateInput(email, password, emailField, passwordField)) return false

        if (confirmPassword.isEmpty()) {
            confirmPasswordField.error = "Vui lòng xác nhận mật khẩu"
            return false
        }

        if (password != confirmPassword) {
            confirmPasswordField.error = "Mật khẩu xác nhận không khớp"
            return false
        }

        // Nếu là seller thì phải nhập tên shop và ngành hàng
        if (role == "seller") {
            if (shopName.isNullOrEmpty()) {
                shopNameField?.error = "Tên cửa hàng không được để trống"
                return false
            }

            if (category.isNullOrEmpty()) {
                categoryField?.performClick()
                return false
            }
        }

        return true
    }



}