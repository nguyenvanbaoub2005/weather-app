package com.example.textn.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.textn.R
import com.example.textn.data.model.DayForecastItem

class DayForecastAdapter : ListAdapter<DayForecastItem, DayForecastAdapter.ForecastViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_day_forecast, parent, false)
        return ForecastViewHolder(view)
    }

    override fun onBindViewHolder(holder: ForecastViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    class ForecastViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dayText: TextView = itemView.findViewById(R.id.tv_day)
        private val dateText: TextView = itemView.findViewById(R.id.tv_date)
        private val tempText: TextView = itemView.findViewById(R.id.tv_temp)
        private val weatherIcon: ImageView = itemView.findViewById(R.id.iv_weather_icon)
        private val descriptionText: TextView = itemView.findViewById(R.id.tv_description)

        fun bind(item: DayForecastItem) {
            dayText.text = item.day
            dateText.text = item.date
            tempText.text = item.temperature
            weatherIcon.setImageResource(item.iconId)
            descriptionText.text = item.description
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<DayForecastItem>() {
            override fun areItemsTheSame(oldItem: DayForecastItem, newItem: DayForecastItem): Boolean {
                return oldItem.id == newItem.id
            }
            override fun areContentsTheSame(oldItem: DayForecastItem, newItem: DayForecastItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}