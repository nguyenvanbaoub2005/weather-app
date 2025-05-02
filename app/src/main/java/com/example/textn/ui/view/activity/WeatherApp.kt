package com.example.textn.ui.view.activity

import android.app.Application
import com.example.textn.utils.WeatherCheckWorker

class WeatherApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Thiết lập lịch kiểm tra thời tiết định kỳ khi ứng dụng được khởi tạo
        WeatherCheckWorker.scheduleRecurringChecks(this)
    }
}
