package com.example.textn.ui.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.textn.databinding.FragmentPostManagementBinding
import com.example.textn.ui.adapter.PostAdapter
import com.example.textn.viewmodel.AdminViewModel

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
        adapter = PostAdapter(
            onDelete = { post ->
                viewModel.deletePost(post.id)
            },
            onPostClick = { post ->
                // Hiển thị Toast khi nhấn vào bài viết (có thể để trống nếu không cần)
                Toast.makeText(context, "Đã nhấn bài viết: ${post.description}", Toast.LENGTH_SHORT).show()
            }
        )
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

