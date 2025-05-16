package com.example.textn.ui.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.textn.R
import com.example.textn.data.model.User
import com.example.textn.databinding.FragmentUserManagementBinding
import com.example.textn.databinding.ItemUserBinding
import com.example.textn.ui.adapter.PostAdapter
import com.example.textn.ui.adapter.UserAdapter
import com.example.textn.viewmodel.AdminViewModel

class UserManagementFragment : Fragment() {
    private var _binding: FragmentUserManagementBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by activityViewModels()
    private lateinit var adapter: UserAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
        viewModel.fetchUsers()
    }

    private fun setupRecyclerView() {
        adapter = UserAdapter(
            onRoleToggle = { user ->
                val newRole = if (user.role == "admin") "user" else "admin"
                viewModel.updateUserRole(user.id, newRole)
            },
            onActiveToggle = { user ->
                viewModel.updateUserActiveStatus(user.id, !user.isActive)
            },
            onEdit = { user ->
                showEditDialog(user)
            },
            onNameClick = { user ->
                // Xử lý khi người dùng nhấn vào tên
                showUserPostsDialog(user)
            }
        )
        binding.rvUsers.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = this@UserManagementFragment.adapter
        }
    }

    private fun setupObservers() {
        viewModel.users.observe(viewLifecycleOwner) { users ->
            adapter.submitList(users)
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
    private fun showUserPostsDialog(user: User) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_user_posts, null)
        val rvPosts = dialogView.findViewById<RecyclerView>(R.id.rvUserPosts)
        val adapter = PostAdapter(
            onDelete = { post -> viewModel.deletePost(post.id) },
            onPostClick = { post ->
                Toast.makeText(context, "Bài viết: ${post.description}", Toast.LENGTH_SHORT).show()
            }
        )
        rvPosts.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }
        viewModel.userPosts.observe(viewLifecycleOwner) { posts ->
            adapter.submitList(posts)
        }
        viewModel.fetchPostsByUserId(user.id)
        AlertDialog.Builder(requireContext())
            .setTitle("Bài viết của ${user.displayName}")
            .setView(dialogView)
            .setNegativeButton("Đóng", null)
            .show()
    }
    private fun showEditDialog(user: User) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_user, null)
        val etDisplayName = dialogView.findViewById<EditText>(R.id.etDisplayName)
        val etPhotoUrl = dialogView.findViewById<EditText>(R.id.etPhotoUrl)

        etDisplayName.setText(user.displayName)
        etPhotoUrl.setText(user.photoUrl)

        AlertDialog.Builder(requireContext())
            .setTitle("Chỉnh sửa thông tin người dùng")
            .setView(dialogView)
            .setPositiveButton("Lưu") { _, _ ->
                val newDisplayName = etDisplayName.text.toString().trim()
                val newPhotoUrl = etPhotoUrl.text.toString().trim()

                if (newDisplayName.isNotEmpty()) {
                    viewModel.updateUserProfile(user.id, newDisplayName, newPhotoUrl)
                } else {
                    Toast.makeText(context, "Tên không được để trống", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}