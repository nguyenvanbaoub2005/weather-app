package com.example.textn.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.textn.R
import com.example.textn.data.model.HealthAlert
import com.example.textn.ui.view.MainActivity

class NotificationHelper(private val context: Context) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        private const val CHANNEL_ID = "health_alerts_channel"
        private const val NOTIFICATION_ID = 100
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Cảnh báo sức khỏe"
            val descriptionText = "Thông báo về các cảnh báo sức khỏe liên quan đến thời tiết"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(title: String, content: String, alerts: List<HealthAlert>) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("SHOW_HEALTH_ALERTS", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_health)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Nếu có nhiều cảnh báo, hiển thị dạng expanded
        if (alerts.size > 1) {
            val inboxStyle = NotificationCompat.InboxStyle()
            alerts.forEach { alert ->
                inboxStyle.addLine(alert.title)
            }
            builder.setStyle(inboxStyle)
        }

        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }
}