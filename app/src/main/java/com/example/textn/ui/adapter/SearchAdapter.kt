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
import com.example.textn.model.ActivityType
import com.example.textn.model.LocationSearchItem
import com.example.textn.model.LocationType
import java.text.DecimalFormat

class SearchAdapter(private val onItemClick: (LocationSearchItem) -> Unit) :
    ListAdapter<LocationSearchItem, SearchAdapter.LocationViewHolder>(LocationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_location, parent, false)
        return LocationViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class LocationViewHolder(
        itemView: View,
        private val onItemClick: (LocationSearchItem) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val locationIcon: ImageView = itemView.findViewById(R.id.iv_location_type)
        private val locationName: TextView = itemView.findViewById(R.id.tv_location_name)
        private val rating: TextView = itemView.findViewById(R.id.tv_rating)
        private val distance: TextView = itemView.findViewById(R.id.tv_distance)
        private val activityIcon: ImageView = itemView.findViewById(R.id.iv_activity_icon)

        private val decimalFormat = DecimalFormat("0.#")

        fun bind(location: LocationSearchItem) {
            locationName.text = location.name

            // Set location type icon
            val iconResId = when (location.locationType) {
                LocationType.WEATHER_STATION -> R.drawable.ic_weather_station
                LocationType.COORDINATES -> R.drawable.ic_coordinates
                LocationType.BEACH -> R.drawable.ic_beach
                else -> R.drawable.ic_location
            }
            locationIcon.setImageResource(iconResId)

            // Set rating if available
            if (location.rating > 0) {
                rating.visibility = View.VISIBLE
                rating.text = location.rating.toString()
            } else {
                rating.visibility = View.GONE
            }

            // Set distance
            distance.text = "${decimalFormat.format(location.distance)} NM"

            // Set activity icon if available
            if (location.activityType != null && location.activityType != ActivityType.NONE) {
                activityIcon.visibility = View.VISIBLE
                val activityIconResId = when (location.activityType) {
                    ActivityType.SWIMMING -> R.drawable.ic_swimming
                    ActivityType.HIKING -> R.drawable.ic_hiking
                    ActivityType.GOLF -> R.drawable.ic_golf
                    else -> 0
                }
                if (activityIconResId != 0) {
                    activityIcon.setImageResource(activityIconResId)
                } else {
                    activityIcon.visibility = View.GONE
                }
            } else {
                activityIcon.visibility = View.GONE
            }

            // Set click listener
            itemView.setOnClickListener {
                onItemClick(location)
            }
        }
    }

    class LocationDiffCallback : DiffUtil.ItemCallback<LocationSearchItem>() {
        override fun areItemsTheSame(
            oldItem: LocationSearchItem,
            newItem: LocationSearchItem
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: LocationSearchItem,
            newItem: LocationSearchItem
        ): Boolean {
            return oldItem == newItem
        }
    }
}