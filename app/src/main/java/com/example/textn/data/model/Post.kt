package com.example.textn.data.model

data class Post(
    val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val imageUrl: String = "",
    val location: PostLocation = PostLocation(),
    val description: String = "",
    val timestamp: Long = 0,
    val likes: Int = 0,
    val comments: List<Comment> = listOf(),
    val likedUserIds: List<String> = listOf() // Thêm trường này để lưu danh sách userIds đã like bài viết
)

data class PostLocation(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val locationName: String = ""
)

data class Comment(
    val userId: String = "",
    val displayName: String = "",
    val text: String = "",
    val timestamp: Long = 0
)