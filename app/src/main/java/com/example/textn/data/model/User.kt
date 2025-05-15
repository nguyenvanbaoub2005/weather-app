package com.example.textn.data.model

data class User(
    val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    val createdAt: Long = 0,
    val lastLogin: Long = 0,
    val isActive: Boolean = true,
    val role: String = "" // Thêm trường role
) {
    // Firebase Realtime Database yêu cầu constructor rỗng
    constructor() : this("", "", "", "", 0, 0, true, "")
}