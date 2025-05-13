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
import com.example.textn.model.LocationData

class SearchResultAdapter(
    private val onItemClick: (LocationData) -> Unit
) : ListAdapter<LocationData, SearchResultAdapter.LocationViewHolder>(LocationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_result, parent, false)
        return LocationViewHolder(view, onItemClick)
    }

    fun onSubmitList(list: List<LocationData>?) {
        super.submitList(list?.toList())  // Create a new list to ensure DiffUtil detects changes
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class LocationViewHolder(
        itemView: View,
        private val onItemClick: (LocationData) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val nameTextView: TextView = itemView.findViewById(R.id.location_name)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.location_description)
        private val ratingTextView: TextView = itemView.findViewById(R.id.location_rating)
        private val distanceTextView: TextView = itemView.findViewById(R.id.location_distance)
        private val locationIcon: ImageView = itemView.findViewById(R.id.location_icon)

        fun bind(location: LocationData) {
            nameTextView.text = location.name

            // Set description if available
            if (!location.description.isNullOrEmpty()) {
                descriptionTextView.text = location.description
                descriptionTextView.visibility = View.VISIBLE
            } else {
                descriptionTextView.visibility = View.GONE
            }

            // Set rating
            if (location.rating > 0) {
                ratingTextView.text = location.rating.toString()
                ratingTextView.visibility = View.VISIBLE
            } else {
                ratingTextView.visibility = View.GONE
            }

            // Set distance
            if (location.distance > 0) {
                val distanceText = "${location.distance} NM"
                distanceTextView.text = distanceText
                distanceTextView.visibility = View.VISIBLE
            } else {
                distanceTextView.visibility = View.GONE
            }

            // Set icon based on type (you can implement logic based on your needs)
            // For now, we'll use a generic location marker
            locationIcon.setImageResource(R.drawable.ic_location_marker)

            // Set click listener
            itemView.setOnClickListener { onItemClick(location) }
        }
    }
}

class LocationDiffCallback : DiffUtil.ItemCallback<LocationData>() {
    override fun areItemsTheSame(oldItem: LocationData, newItem: LocationData): Boolean {
        return oldItem.name == newItem.name &&
                oldItem.latitude == newItem.latitude &&
                oldItem.longitude == newItem.longitude
    }

    override fun areContentsTheSame(oldItem: LocationData, newItem: LocationData): Boolean {
        return oldItem == newItem
    }
}