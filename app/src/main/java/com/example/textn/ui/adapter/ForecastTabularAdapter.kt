package com.example.textn.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.textn.R
import com.example.textn.data.model.DayForecast
import com.example.textn.data.model.HourlyForecast
import com.example.textn.ui.view.WindDirectionView
import java.text.SimpleDateFormat
import java.util.Locale

class ForecastTabularAdapter : RecyclerView.Adapter<ForecastTabularAdapter.ForecastViewHolder>() {

    private val forecastDays = mutableListOf<DayForecast>()
    private val timeSlots = listOf("7 AM", "10 AM", "1 PM", "4 PM", "7 PM", "10 PM", "1 AM", "4 AM")

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

        private fun createWindSpeedRow(dayForecast: DayForecast) {
            val row = LayoutInflater.from(itemView.context)
                .inflate(R.layout.item_forecast_row, forecastContainer, false)

            val label: TextView = row.findViewById(R.id.textRowLabel)
            val cells: ViewGroup = row.findViewById(R.id.container_cells)

            label.text = "m/s"

            // Create cells for each time slot
            dayForecast.hourlyForecasts.forEachIndexed { index, hourlyForecast ->
                val cell = createTextCell(cells, hourlyForecast.windSpeed.toString())
                cell.setBackgroundResource(getWindSpeedBackgroundColor(hourlyForecast.windSpeed))
            }

            forecastContainer.addView(row)
        }

        private fun createWindDirectionRow(dayForecast: DayForecast) {
            val row = LayoutInflater.from(itemView.context)
                .inflate(R.layout.item_forecast_row, forecastContainer, false)

            val label: TextView = row.findViewById(R.id.textRowLabel)
            val cells: ViewGroup = row.findViewById(R.id.container_cells)

            label.text = ""  // Arrow icons show direction

            // Create cells with wind direction arrows
            dayForecast.hourlyForecasts.forEach { hourlyForecast ->
                val cell = LayoutInflater.from(itemView.context)
                    .inflate(R.layout.item_wind_direction_cell, cells, false)

                val directionView: WindDirectionView = cell.findViewById(R.id.windDirectionView)
                directionView.setWindDirection(hourlyForecast.windDirection)

                cells.addView(cell)
            }

            forecastContainer.addView(row)
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

        private fun createPrecipitationRow(dayForecast: DayForecast) {
            val row = LayoutInflater.from(itemView.context)
                .inflate(R.layout.item_forecast_row, forecastContainer, false)

            val label: TextView = row.findViewById(R.id.textRowLabel)
            val cells: ViewGroup = row.findViewById(R.id.container_cells)

            label.text = "mm"

            // Create cells for precipitation values
            dayForecast.hourlyForecasts.forEach { hourlyForecast ->
                val precipText = hourlyForecast.precipitation?.toString() ?: "-"
                val cell = createTextCell(cells, precipText)
            }

            forecastContainer.addView(row)
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