package com.example.textn.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.textn.R
import com.example.textn.data.model.User
import com.example.textn.databinding.ItemUserBinding

class UserAdapter(
    private val onRoleToggle: (User) -> Unit,
    private val onActiveToggle: (User) -> Unit,
    private val onEdit: (User) -> Unit,
    private val onNameClick: (User) -> Unit
) : androidx.recyclerview.widget.ListAdapter<User, UserAdapter.UserViewHolder>(
    object : androidx.recyclerview.widget.DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean = oldItem == newItem
    }
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class UserViewHolder(
        private val binding: ItemUserBinding
    ) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            binding.tvUserId.text = "#${user.id.take(5)}"
            binding.tvDisplayName.text = user.displayName
            binding.tvEmail.text = user.email
            binding.chipRole.text = user.role
            binding.chipRole.chipBackgroundColor = android.content.res.ColorStateList.valueOf(
                when (user.role) {
                    "admin" -> android.graphics.Color.parseColor("#3F51B5")
                    else -> android.graphics.Color.parseColor("#4CAF50")
                }
            )
            binding.btnToggleActive.text = if (user.isActive) "Lock" else "Unlock"
            binding.userStatusIndicator.setBackgroundColor(
                if (user.isActive)
                    android.graphics.Color.parseColor("#4CAF50")
                else
                    android.graphics.Color.parseColor("#F44336")
            )

            // Tải ảnh đại diện
            Glide.with(binding.root.context)
                .load(user.photoUrl)
                .placeholder(R.drawable.image_user)
                .error(R.drawable.image_user)
                .into(binding.ivUserAvatar)

            binding.btnToggleRole.setOnClickListener { onRoleToggle(user) }
            binding.btnToggleActive.setOnClickListener { onActiveToggle(user) }
            binding.btnEdit.setOnClickListener { onEdit(user) }
            binding.tvDisplayName.setOnClickListener { onNameClick(user) }
        }
    }
}