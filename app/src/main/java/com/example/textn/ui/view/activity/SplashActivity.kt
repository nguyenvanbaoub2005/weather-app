package com.example.textn.ui.view.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.textn.utils.FirebaseNotificationHelper
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // GỌI HÀM LẤY TOKEN từ helper
        val fcmHelper = FirebaseNotificationHelper(applicationContext)
        fcmHelper.getFirebaseToken { token ->
            if (token != null) {
                Log.d("SplashActivity", "Lấy FCM Token thành công: $token")
                // Nếu bạn muốn đăng ký chủ đề:
                fcmHelper.subscribeToTopic("health_alerts")  // Ví dụ chủ đề
            } else {
                Log.e("SplashActivity", "Lấy FCM Token thất bại")
            }
        }

        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            Log.d("SplashActivity", "Đăng nhập với email: ${currentUser.email}")
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        finish()
    }
}
