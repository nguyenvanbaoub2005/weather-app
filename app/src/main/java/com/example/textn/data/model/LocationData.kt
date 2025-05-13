package com.example.textn.model

data class LocationData(
    val name: String,           // Tên thành phố hoặc địa điểm (ví dụ: "Da Nang")
    val latitude: Double,       // Vĩ độ (latitude) của địa điểm, dùng để định vị trên bản đồ
    val longitude: Double,      // Kinh độ (longitude) của địa điểm
    val rating: Float = 0.0f,   // Điểm đánh giá địa điểm (ví dụ: từ 0 đến 5 sao), mặc định là 0.0f
    val description: String? = null, // Mô tả ngắn về địa điểm (có thể là tiếng Việt hoặc thông tin chi tiết), có thể null
    val distance: Float = 0.0f  // Khoảng cách từ vị trí hiện tại đến địa điểm (tính bằng km hoặc m), mặc định là 0.0f
)