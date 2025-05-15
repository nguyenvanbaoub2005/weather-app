package com.example.textn.ui.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.textn.data.model.Post
import com.example.textn.databinding.FragmentPostManagementBinding
import com.example.textn.viewmodel.AdminViewModel
import java.text.SimpleDateFormat
import java.util.*

class PostManagementFragment : Fragment() {
    private var _binding: FragmentPostManagementBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by activityViewModels()
    private lateinit var adapter: PostAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
        viewModel.fetchPosts()
    }

    private fun setupRecyclerView() {
        adapter = PostAdapter { post ->
            viewModel.deletePost(post.id)
        }
        binding.rvPosts.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = this@PostManagementFragment.adapter
        }
    }

    private fun setupObservers() {
        viewModel.posts.observe(viewLifecycleOwner) { posts ->
            adapter.submitList(posts)
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class PostAdapter(
    private val onDelete: (Post) -> Unit
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
        }
    }
}