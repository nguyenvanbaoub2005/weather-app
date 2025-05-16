package com.example.textn.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.textn.data.model.Post
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PostAdapter(
    private val onDelete: (Post) -> Unit,
    private val onPostClick: (Post) -> Unit
) : androidx.recyclerview.widget.ListAdapter<Post, PostAdapter.PostViewHolder>(
    object : androidx.recyclerview.widget.DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean = oldItem == newItem
    }
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = com.example.textn.databinding.ItemPostBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PostViewHolder(
        private val binding: com.example.textn.databinding.ItemPostBinding
    ) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        fun bind(post: Post) {
            binding.tvDisplayName.text = post.displayName
            binding.tvDescription.text = post.description
            binding.tvLocation.text = post.location.locationName.ifEmpty { "Không có vị trí" }
            binding.tvTimestamp.text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(Date(post.timestamp))
            binding.tvLikes.text = "Lượt thích: ${post.likes}"
            binding.tvComments.text = "Bình luận: ${post.comments.size}"
            binding.btnDelete.setOnClickListener { onDelete(post) }
            binding.root.setOnClickListener { onPostClick(post) } // Thêm sự kiện nhấn vào item
        }
    }
}
