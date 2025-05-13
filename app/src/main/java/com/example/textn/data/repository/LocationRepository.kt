package com.example.textn.repository

import com.example.textn.model.LocationData

class LocationRepository {
    // This is a simple in-memory repository with Vietnamese cities
    // In a real app, you might want to fetch this data from an API or database
    private val vietnamCities = listOf(
        LocationData("Ha noi", 21.0278, 105.8342, 5.0f, "Thủ đô Việt Nam"),
        LocationData("Ho Chi Minh City", 10.8231, 106.6297, 5.0f, "Thành phố Hồ Chí Minh"),
        LocationData("Da Nang", 16.0544, 108.0717, 5.0f, "Đà Nẵng"),
        LocationData("Hue", 16.4678, 107.5959, 5.0f, "Huế"),
        LocationData("Nha Trang", 12.2388, 109.1967, 5.0f, "Nha Trang"),
        LocationData("Can Tho", 10.0452, 105.7469, 4.8f, "Cần Thơ"),
        LocationData("Hai Phong", 20.8449, 106.6881, 4.7f, "Hải Phòng"),
        LocationData("Vung Tau", 10.3546, 107.0843, 4.8f, "Vũng Tàu"),
        LocationData("Quy Nhon", 13.7829, 109.2196, 4.6f, "Quy Nhơn"),
        LocationData("Buon Ma Thuot", 12.6661, 108.0504, 4.5f, "Buôn Ma Thuột"),
        LocationData("Da lat", 11.9404, 108.4580, 4.9f, "Đà Lạt"),
        LocationData("Bac Ninh", 21.1861, 106.0763, 4.4f, "Bắc Ninh"),
        LocationData("Bien Hoa", 10.9450, 106.8249, 4.3f, "Biên Hòa"),
        LocationData("Ha Long", 20.9511, 107.0848, 4.8f, "Hạ Long"),
        LocationData("My Tho", 10.3600, 106.3600, 4.4f, "Mỹ Tho"),
        LocationData("Nam Dinh", 20.4389, 106.1621, 4.3f, "Nam Định"),
        LocationData("Phan Thiet", 10.9288, 108.1020, 4.7f, "Phan Thiết"),
        LocationData("Pleiku", 13.9833, 108.0000, 4.2f, "Pleiku"),
        LocationData("Rach Gia", 10.0139, 105.0839, 4.1f, "Rạch Giá"),
        LocationData("Soc Trang", 9.6003, 105.9681, 4.1f, "Sóc Trăng"),
        LocationData("Thai Binh", 20.4500, 106.3333, 4.2f, "Thái Bình"),
        LocationData("Thai Nguyen", 21.5889, 105.8056, 4.3f, "Thái Nguyên"),
        LocationData("Thanh Hoa", 19.8067, 105.7667, 4.3f, "Thanh Hóa"),
        LocationData("Vinh", 18.6733, 105.6922, 4.4f, "Vinh"),
        LocationData("Sa Pa", 22.3367, 103.8378, 4.9f, "Sa Pa"),
        LocationData("Mui Ne", 10.9331, 108.2421, 4.8f, "Mũi Né"),
        LocationData("Hoi An", 15.8794, 108.3358, 4.9f, "Hội An"),
        LocationData("Phu Quoc", 10.2202, 103.9577, 4.9f, "Phú Quốc"),
        LocationData("Non Nuoc Beach", 16.0018, 108.2640, 4.7f, "Non Nước"),
        LocationData("China Beach", 16.0951, 108.2486, 4.8f, "China Beach"),
        LocationData("My Khe Beach", 16.0736, 108.2466, 4.9f, "Mỹ Khê"),
        LocationData("Cao Bang", 22.6667, 106.2500, 4.3f, "Cao Bằng"),
        LocationData("Dien Bien Phu", 21.3858, 103.0179, 4.4f, "Điện Biên Phủ"),
        LocationData("Ha Giang", 22.8333, 104.9833, 4.6f, "Hà Giang"),
        LocationData("Lang Son", 21.8530, 106.7610, 4.2f, "Lạng Sơn"),
        LocationData("Moc Chau", 20.8404, 104.6531, 4.5f, "Mộc Châu"),
        LocationData("Ninh Binh", 20.2581, 105.9752, 4.6f, "Ninh Bình"),
        LocationData("Ha Tinh", 18.3333, 105.9000, 4.2f, "Hà Tĩnh"),
        LocationData("Dong Hoi", 17.4833, 106.6000, 4.3f, "Đồng Hới"),
        LocationData("Quang Ngai", 15.1167, 108.8000, 4.1f, "Quảng Ngãi"),
        LocationData("Kon Tum", 14.3500, 108.0000, 4.0f, "Kon Tum")
    )

    fun searchCities(query: String): List<LocationData> {
        if (query.isBlank()) return emptyList()

        val normalizedQuery = query.lowercase().trim()

        return if (normalizedQuery == "vietnam") {
            vietnamCities
        } else {
            vietnamCities.filter { city ->
                city.name.lowercase().contains(normalizedQuery) ||
                        city.description?.lowercase()?.contains(normalizedQuery) == true
            }
        }
    }
}