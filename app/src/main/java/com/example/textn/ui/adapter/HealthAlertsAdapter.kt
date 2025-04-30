package com.example.textn.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.textn.R
import com.example.textn.data.model.HealthAlert
import com.example.textn.data.services.AlertSeverity
import com.google.android.material.card.MaterialCardView

class HealthAlertsAdapter(
    private var alerts: List<HealthAlert>
) : RecyclerView.Adapter<HealthAlertsAdapter.AlertViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_health_alert, parent, false)
        return AlertViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        holder.bind(alerts[position])
    }

    override fun getItemCount() = alerts.size

    inner class AlertViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconView: ImageView = itemView.findViewById(R.id.alert_icon)
        private val titleView: TextView = itemView.findViewById(R.id.alert_title)
        private val descriptionView: TextView = itemView.findViewById(R.id.alert_description)
        private val cardView: MaterialCardView = itemView.findViewById(R.id.alert_card)

        fun bind(alert: HealthAlert) {
            iconView.setImageResource(alert.iconResId)
            titleView.text = alert.title
            descriptionView.text = alert.description

            // Đặt màu nền dựa theo mức độ nghiêm trọng
            val bgColor = when (alert.severity) {
                AlertSeverity.LOW -> R.color.alert_low
                AlertSeverity.MEDIUM -> R.color.alert_medium
                AlertSeverity.HIGH -> R.color.alert_high
            }
            cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.context, bgColor))
        }
    }
    fun updateData(newAlerts: List<HealthAlert>) {
        alerts = newAlerts
        notifyDataSetChanged()
    }
}