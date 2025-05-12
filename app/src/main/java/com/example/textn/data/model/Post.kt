package com.example.textn.data.model

data class Post(
    val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val imageUrl: String = "",
    val location: PostLocation = PostLocation(),
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val likes: Int = 0,
    val comments: List<Comment> = emptyList()
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
    val timestamp: Long = System.currentTimeMillis()
)