package com.example.textn.ui.view.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.textn.R

class LauncherActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)

        Handler(Looper.getMainLooper()).postDelayed({
//            checkLoginStatus()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 1000) // Delay 1s để show splash nhẹ, có thể bỏ nếu không cần
    }

    private fun checkLoginStatus() {
//        val currentUser = auth.currentUser
//        if (currentUser != null) {
//            // Giả sử bạn đã lưu role vào Room hoặc SharedPref sau đăng nhập
//            val role = getRoleLocally() // "user" hoặc "seller"
//            if (role == "seller") {
//                startActivity(Intent(this, SellerMainActivity::class.java))
//            } else {
//                startActivity(Intent(this, UserMainActivity::class.java))
//            }
//        } else {
//            startActivity(Intent(this, LoginActivity::class.java))
//        }
//        finish()
    }

    private fun getRoleLocally(): String {
        // Lấy từ Room, DataStore, hoặc SharedPreferences
        return "user" // Tạm cứng để test
    }
}