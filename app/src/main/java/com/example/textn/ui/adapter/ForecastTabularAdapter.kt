package com.example.textn.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.textn.R
import com.example.textn.data.model.DayForecast
import com.example.textn.data.model.HourlyForecast
import com.example.textn.ui.view.WindDirectionView
import java.text.SimpleDateFormat
import java.util.Locale

class ForecastTabularAdapter : RecyclerView.Adapter<ForecastTabularAdapter.ForecastViewHolder>() {

    private val forecastDays = mutableListOf<DayForecast>()
    private val timeSlots = listOf("1AM", "4AM", "7 AM", "10 AM", "1 PM", "4 PM", "7 PM", "10 PM")


    fun submitData(days: List<DayForecast>) {
        forecastDays.clear()
        forecastDays.addAll(days)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_forecast_day, parent, false)
        return ForecastViewHolder(view)
    }

    override fun onBindViewHolder(holder: ForecastViewHolder, position: Int) {
        val dayForecast = forecastDays[position]
        holder.bind(dayForecast)
    }

    override fun getItemCount(): Int = forecastDays.size

    inner class ForecastViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateTitle: TextView = itemView.findViewById(R.id.textDate)
        private val forecastContainer: ViewGroup = itemView.findViewById(R.id.container_hourly_forecasts)

        fun bind(dayForecast: DayForecast) {
            // Format and set date
            val dateFormat = SimpleDateFormat("EEE, dd MMM", Locale.getDefault())
            dateTitle.text = dateFormat.format(dayForecast.date)

            // Clear previous views
            forecastContainer.removeAllViews()

            // Create rows for each data type
            createWindSpeedRow(dayForecast)
            createWindDirectionRow(dayForecast)
            createBeaufortRow(dayForecast, "bft", R.drawable.ic_beaufort)
            createBeaufortRow(dayForecast, "bft*", R.drawable.ic_beaufort_gust)
            createTemperatureRow(dayForecast)
            createPrecipitationRow(dayForecast)
            createWaveHeightRow(dayForecast)
        }

        // Hàm để tạo một hàng hiển thị tốc độ gió trong dự báo thời tiết theo ngày
        private fun createWindSpeedRow(dayForecast: DayForecast) {
            // Tạo một hàng mới từ layout item_forecast_row
            val row = LayoutInflater.from(itemView.context)
                .inflate(R.layout.item_forecast_row, forecastContainer, false)

            val label: TextView = row.findViewById(R.id.textRowLabel) // Lấy TextView để hiển thị nhãn
            val cells: ViewGroup = row.findViewById(R.id.container_cells) // Lấy ViewGroup để chứa các ô dữ liệu

            label.text = "m/s" // Đặt nhãn cho hàng là "m/s"

            // Tạo các ô cho mỗi khoảng thời gian trong dự báo
            dayForecast.hourlyForecasts.forEach { hourlyForecast ->
                val formattedSpeed = String.format("%.1f", hourlyForecast.windSpeed) // Định dạng tốc độ gió thành chuỗi với 1 chữ số thập phân
                val cell = createTextCell(cells, formattedSpeed) // Tạo ô văn bản cho tốc độ gió
                cell.setBackgroundResource(getWindSpeedBackgroundColor(hourlyForecast.windSpeed)) // Đặt màu nền cho ô dựa trên tốc độ gió
            }

            forecastContainer.addView(row) // Thêm hàng vào container dự báo
        }

        // Hàm để tạo một hàng hiển thị hướng gió trong dự báo thời tiết theo ngày
        private fun createWindDirectionRow(dayForecast: DayForecast) {
            // Tạo một hàng mới từ layout item_forecast_row
            val row = LayoutInflater.from(itemView.context)
                .inflate(R.layout.item_forecast_row, forecastContainer, false)

            val label: TextView = row.findViewById(R.id.textRowLabel) // Lấy TextView để hiển thị nhãn
            val cells: ViewGroup = row.findViewById(R.id.container_cells) // Lấy ViewGroup để chứa các ô dữ liệu

            // Đặt biểu tượng hướng gió cho nhãn
            val windDirectionIcon = ContextCompat.getDrawable(itemView.context, R.drawable.ic_wind_direction)
            label.setCompoundDrawablesWithIntrinsicBounds(windDirectionIcon, null, null, null) // Thêm biểu tượng vào nhãn
            label.text = ""  // Đặt văn bản rỗng chỉ để hiển thị biểu tượng

            // Tạo các ô với mũi tên hướng gió
            dayForecast.hourlyForecasts.forEach { hourlyForecast ->
                val cell = LayoutInflater.from(itemView.context)
                    .inflate(R.layout.item_wind_direction_cell, cells, false) // Tạo ô mới cho hướng gió

                val directionView: WindDirectionView = cell.findViewById(R.id.windDirectionView) // Lấy View để hiển thị hướng gió
                directionView.setWindDirection(hourlyForecast.windDirection) // Đặt hướng gió cho View

                cells.addView(cell) // Thêm ô vào container
            }

            forecastContainer.addView(row) // Thêm hàng vào container dự báo
        }

        private fun createBeaufortRow(dayForecast: DayForecast, label: String, iconResId: Int) {
            val row = LayoutInflater.from(itemView.context)
                .inflate(R.layout.item_forecast_row, forecastContainer, false)

            val labelView: TextView = row.findViewById(R.id.textRowLabel)
            val labelIcon: ImageView = row.findViewById(R.id.iconRowLabel)
            val cells: ViewGroup = row.findViewById(R.id.container_cells)

            labelView.text = label
            labelIcon.setImageResource(iconResId)
            labelIcon.visibility = View.VISIBLE

            // Create cells for Beaufort values
            dayForecast.hourlyForecasts.forEach { hourlyForecast ->
                val beaufortValue = if (label == "bft")
                    hourlyForecast.beaufortValue else hourlyForecast.beaufortGusts
                val cell = createTextCell(cells, beaufortValue.toString())
                cell.setBackgroundResource(getBeaufortBackgroundColor(beaufortValue))
            }

            forecastContainer.addView(row)
        }

        private fun createTemperatureRow(dayForecast: DayForecast) {
            val row = LayoutInflater.from(itemView.context)
                .inflate(R.layout.item_forecast_row, forecastContainer, false)

            val label: TextView = row.findViewById(R.id.textRowLabel)
            val cells: ViewGroup = row.findViewById(R.id.container_cells)

            label.text = "°C"

            // Create cells for temperature values
            dayForecast.hourlyForecasts.forEach { hourlyForecast ->
                val cell = createTextCell(cells, hourlyForecast.temperature.toInt().toString())
                cell.setBackgroundResource(getTemperatureBackgroundColor(hourlyForecast.temperature))
            }

            forecastContainer.addView(row)
        }

        // Hàm để tạo một hàng hiển thị giá trị lượng mưa trong dự báo thời tiết theo ngày
        private fun createPrecipitationRow(dayForecast: DayForecast) {
            // Tạo một hàng mới từ layout item_forecast_row
            val row = LayoutInflater.from(itemView.context)
                .inflate(R.layout.item_forecast_row, forecastContainer, false)

            val label: TextView = row.findViewById(R.id.textRowLabel) // Lấy TextView để hiển thị nhãn
            val cells: ViewGroup = row.findViewById(R.id.container_cells) // Lấy ViewGroup để chứa các ô dữ liệu

            label.text = "mm" // Đặt nhãn cho hàng là "mm" (milimét)

            // Tạo các ô cho giá trị lượng mưa với định dạng phù hợp
            dayForecast.hourlyForecasts.forEach { hourlyForecast ->
                val precipValue = hourlyForecast.precipitation ?: 0f // Lấy giá trị lượng mưa, mặc định là 0 nếu không có
                // Định dạng giá trị lượng mưa
                val precipText = if (precipValue < 0.01f) "0.0" else String.format("%.2f", precipValue)
                val cell = createTextCell(cells, precipText) // Tạo ô văn bản cho lượng mưa

                // Tùy chọn: Thêm màu nền dựa trên mức độ lượng mưa
                if (precipValue > 0) {
                    cell.setBackgroundResource(getPrecipitationBackgroundColor(precipValue)) // Đặt màu nền cho ô
                }
            }

            forecastContainer.addView(row) // Thêm hàng vào container dự báo
        }

        // Hàm trợ giúp để lấy màu nền cho lượng mưa
        private fun getPrecipitationBackgroundColor(precipitation: Float): Int {
            return when {
                precipitation < 0.5f -> R.color.precipitation_very_light // Rất nhẹ
                precipitation < 2.5f -> R.color.precipitation_light // Nhẹ
                precipitation < 7.5f -> R.color.precipitation_moderate // Vừa
                precipitation < 15f -> R.color.precipitation_heavy // Nặng
                else -> R.color.precipitation_very_heavy // Rất nặng
            }
        }


        private fun createWaveHeightRow(dayForecast: DayForecast) {
            // Only show wave height if data is available
            if (dayForecast.hourlyForecasts.any { it.waveHeight != null }) {
                val row = LayoutInflater.from(itemView.context)
                    .inflate(R.layout.item_forecast_row, forecastContainer, false)

                val label: TextView = row.findViewById(R.id.textRowLabel)
                val cells: ViewGroup = row.findViewById(R.id.container_cells)

                label.text = "m"

                // Create cells for wave height values
                dayForecast.hourlyForecasts.forEach { hourlyForecast ->
                    val waveText = hourlyForecast.waveHeight?.toString() ?: "-"
                    val cell = createTextCell(cells, waveText)
                    if (hourlyForecast.waveHeight != null) {
                        cell.setBackgroundResource(getWaveHeightBackgroundColor(hourlyForecast.waveHeight))
                    }
                }

                forecastContainer.addView(row)
            }
        }

        private fun createTextCell(container: ViewGroup, text: String): TextView {
            val cell = LayoutInflater.from(itemView.context)
                .inflate(R.layout.item_forecast_cell, container, false) as TextView
            cell.text = text
            container.addView(cell)
            return cell
        }

        private fun getWindSpeedBackgroundColor(windSpeed: Float): Int {
            return when {
                windSpeed < 2.0f -> R.color.wind_speed_low
                windSpeed < 4.0f -> R.color.wind_speed_medium
                else -> R.color.wind_speed_high
            }
        }

        private fun getBeaufortBackgroundColor(beaufort: Int): Int {
            return when {
                beaufort <= 2 -> R.color.beaufort_low
                beaufort <= 4 -> R.color.beaufort_medium
                else -> R.color.beaufort_high
            }
        }

        private fun getTemperatureBackgroundColor(temp: Float): Int {
            return when {
                temp < 25 -> R.color.temp_low
                temp < 30 -> R.color.temp_medium
                else -> R.color.temp_high
            }
        }

        private fun getWaveHeightBackgroundColor(height: Float): Int {
            return when {
                height < 0.3f -> R.color.wave_low
                height < 0.7f -> R.color.wave_medium
                else -> R.color.wave_high
            }
        }
    }
}