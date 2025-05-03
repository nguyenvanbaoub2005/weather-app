package com.example.textn.utils

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging

class FirebaseNotificationHelper(private val context: Context) {

    companion object {
        private const val TAG = "FirebaseNotifHelper"
    }

    /**
     * Đăng ký và lấy token FCM
     */
    fun getFirebaseToken(callback: (String?) -> Unit) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Lấy token FCM thất bại", task.exception)
                callback(null)
                return@OnCompleteListener
            }
            // Lấy token mới
            val token = task.result

            // Log và lưu token
            Log.d(TAG, "FCM Token: $token")
            saveTokenToPrefs(token)

            callback(token)
        })
    }

    /**
     * Lưu token vào SharedPreferences
     */
    private fun saveTokenToPrefs(token: String) {
        val sharedPreferences = context.getSharedPreferences("health_alert_prefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("fcm_token", token)
            apply()
        }
    }
    /**
     * Đăng ký nhận thông báo theo chủ đề
     */
    fun subscribeToTopic(topic: String) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Đã đăng ký nhận thông báo chủ đề: $topic")
                } else {
                    Log.e(TAG, "Đăng ký chủ đề thất bại: $topic", task.exception)
                }
            }
    }
    /**
     * Hủy đăng ký nhận thông báo theo chủ đề
     */
    fun unsubscribeFromTopic(topic: String) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Đã hủy đăng ký chủ đề: $topic")
                } else {
                    Log.e(TAG, "Hủy đăng ký chủ đề thất bại: $topic", task.exception)
                }
            }
    }
}
