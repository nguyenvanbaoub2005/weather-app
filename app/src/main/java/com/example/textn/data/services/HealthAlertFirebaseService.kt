package com.example.textn.data.services


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.textn.R
import com.example.textn.data.model.HealthAlert
import com.example.textn.data.services.AlertSeverity
import com.example.textn.ui.view.activity.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class HealthAlertFirebaseService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "HealthAlertFirebaseService"
        private const val CHANNEL_ID = "health_alerts_channel"
        private const val NOTIFICATION_ID = 100
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, "Thông báo từ: ${remoteMessage.from}")

        // Xử lý dữ liệu thông báo (data payload)
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Dữ liệu thông báo: ${remoteMessage.data}")

            try {
                val alertType = remoteMessage.data["alertType"]
                val title = remoteMessage.data["title"] ?: "Cảnh báo sức khỏe"
                val content = remoteMessage.data["content"] ?: "Có cảnh báo sức khỏe mới"
                val severityStr = remoteMessage.data["severity"] ?: "MEDIUM"
                val alertsJson = remoteMessage.data["alerts"]

                val severity = try {
                    AlertSeverity.valueOf(severityStr)
                } catch (e: Exception) {
                    AlertSeverity.MEDIUM
                }

                // Xử lý danh sách cảnh báo nếu có
                val alerts = if (!alertsJson.isNullOrEmpty()) {
                    try {
                        val type = object : TypeToken<List<HealthAlert>>() {}.type
                        Gson().fromJson<List<HealthAlert>>(alertsJson, type)
                    } catch (e: Exception) {
                        Log.e(TAG, "Lỗi khi phân tích JSON cảnh báo", e)
                        emptyList()
                    }
                } else {
                    // Tạo một cảnh báo đơn lẻ từ thông tin thông báo
                    listOf(
                        HealthAlert(
                            id = System.currentTimeMillis().toString(),
                            severity = severity,
                            title = title,
                            description = content,
                            iconResId = getIconForAlertType(alertType)
                        )
                    )
                }

                showNotification(title, content, alerts)
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi khi xử lý thông báo", e)
            }
        }

        // Xử lý thông báo trực tiếp (notification payload)
        remoteMessage.notification?.let {
            val title = it.title ?: "Cảnh báo sức khỏe"
            val body = it.body ?: "Có cảnh báo sức khỏe mới"

            val alert = HealthAlert(
                id = System.currentTimeMillis().toString(),
                severity = AlertSeverity.MEDIUM,
                title = title,
                description = body,
                iconResId = R.drawable.ic_notification_health
            )

            showNotification(title, body, listOf(alert))
        }
    }

    private fun getIconForAlertType(alertType: String?): Int {
        return when (alertType) {
            "temperature" -> R.drawable.ic_high_temp_warning
            "uv" -> R.drawable.ic_uv_warning
            "air_quality" -> R.drawable.ic_air_quality_warning
            "humidity" -> R.drawable.ic_humidity_warning
            else -> R.drawable.ic_notification_health
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Token FCM mới: $token")

        // Gửi token mới lên server
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String) {
        // Thực hiện gửi token lên server của bạn
        // Ví dụ: apiService.registerDevice(token)

        // Lưu token vào SharedPreferences
        getSharedPreferences("health_alert_prefs", Context.MODE_PRIVATE).edit().apply {
            putString("fcm_token", token)
            apply()
        }
    }

    private fun showNotification(title: String, content: String, alerts: List<HealthAlert>) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Tạo notification channel nếu cần
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Cảnh báo sức khỏe"
            val descriptionText = "Thông báo về các cảnh báo sức khỏe liên quan đến thời tiết"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("SHOW_HEALTH_ALERTS", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(alerts.firstOrNull()?.iconResId ?: R.drawable.ic_notification_health)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

}