package com.example.textn.ui.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.textn.R
import com.example.textn.data.model.Post
import com.example.textn.ui.adapter.CommunityAdapter
import com.example.textn.viewmodel.CommunityViewModel
import com.example.textn.viewmodel.CommunityViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton

class CommunityFragment : Fragment() {

    private lateinit var viewModel: CommunityViewModel
    private lateinit var communityAdapter: CommunityAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAddPost: FloatingActionButton
    private var currentPosts: List<Post> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_community, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Thiết lập nút quay lại
//        val btnBack: ImageButton = view.findViewById(R.id.btn_back)
//        btnBack.setOnClickListener {
//            findNavController().navigateUp()
//        }

        // Thiết lập RecyclerView
        recyclerView = view.findViewById(R.id.rv_community)

        // Sử dụng GridLayoutManager với khoảng cách 0dp để hiển thị giống như hình ảnh
        val gridLayoutManager = GridLayoutManager(requireContext(), 3)
        recyclerView.layoutManager = gridLayoutManager

        // Khởi tạo adapter và gán vào RecyclerView
        communityAdapter = CommunityAdapter { post ->
            navigateToPostDetail(post)
        }
        recyclerView.adapter = communityAdapter

        // Khởi tạo ViewModel
        val factory = CommunityViewModelFactory(requireActivity().application)
        viewModel = ViewModelProvider(this, factory)[CommunityViewModel::class.java]

        // Quan sát dữ liệu từ ViewModel
        viewModel.posts.observe(viewLifecycleOwner) { posts ->
            val sortedPosts = posts.sortedByDescending { it.timestamp } // Sắp xếp mới nhất lên đầu
            currentPosts = sortedPosts
            communityAdapter.submitList(sortedPosts)
        }

        // Xử lý thông báo lỗi
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            if (!errorMessage.isNullOrEmpty()) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        }

        // Thiết lập nút thêm bài đăng mới
        fabAddPost = view.findViewById(R.id.fab_add_post_rigth)
        fabAddPost.setOnClickListener {
            navigateToAddPost()
        }

        // Tải dữ liệu bài đăng
        viewModel.loadPosts()
    }

    private fun navigateToPostDetail(post: Post) {
        val action = CommunityFragmentDirections
            .actionCommunityFragmentToPostDetailFragment(post.id)
        findNavController().navigate(action)
    }

    private fun navigateToAddPost() {
        findNavController().navigate(R.id.nav_addPostFragment)
    }
}
