package com.example.textn.data.model

import com.example.textn.data.services.AlertSeverity

data class HealthAlert(
    val id: String,  // ID để nhận dạng cảnh báo
    val severity: AlertSeverity,
    val title: String,
    val description: String,
    val iconResId: Int
)