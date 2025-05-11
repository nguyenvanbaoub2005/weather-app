package com.example.textn.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.textn.R
import com.example.textn.data.model.Post

class CommunityAdapter(private val onItemClick: (Post) -> Unit) :
    ListAdapter<Post, CommunityAdapter.CommunityViewHolder>(PostDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommunityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_community_post, parent, false)
        return CommunityViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommunityViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
    }

    inner class CommunityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.iv_post_image)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }

            // Thiết lập chiều cao của ảnh bằng chiều rộng để tạo hình vuông
            imageView.post {
                val params = imageView.layoutParams
                params.height = itemView.width
                imageView.layoutParams = params
            }
        }

        fun bind(post: Post) {
            // Tải ảnh bằng Glide
            Glide.with(itemView.context)
                .load(post.imageUrl)
//                .placeholder(R.drawable.placeholder_image)
//                .error(R.drawable.error_image)
                .centerCrop()
                .into(imageView)
        }
    }
}

class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }
}