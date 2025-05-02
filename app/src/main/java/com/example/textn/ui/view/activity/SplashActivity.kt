package com.example.textn.ui.view.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            // In ra email người dùng
            Log.d("SplashActivity", "Đăng nhập với email: ${currentUser.email}")
            // hoặc
            println("Đăng nhập với email: ${currentUser.email}")

            // Chuyển sang com.example.textn.ui.view.activity.MainActivity
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            // Chuyển sang LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
        }

        finish()
    }
}
