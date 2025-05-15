package com.example.textn.ui.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.textn.data.model.User
import com.example.textn.databinding.FragmentUserManagementBinding
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class UserAdapter(
    private val onRoleToggle: (User) -> Unit,
    private val onActiveToggle: (User) -> Unit
) : androidx.recyclerview.widget.ListAdapter<User, UserAdapter.UserViewHolder>(
    object : androidx.recyclerview.widget.DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean = oldItem == newItem
    }
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = com.example.textn.databinding.ItemUserBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class UserViewHolder(
        private val binding: com.example.textn.databinding.ItemUserBinding
    ) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            binding.tvUserId.text = user.id
            binding.tvRole.text = user.role
            binding.tvEmail.text = user.email
            binding.tvDisplayName.text = user.displayName
            binding.btnToggleRole.setOnClickListener { onRoleToggle(user) }
            binding.btnToggleActive.text = if (user.isActive) "Lock" else "Unlock"
            binding.btnToggleActive.setOnClickListener { onActiveToggle(user) }
        }
    }
}