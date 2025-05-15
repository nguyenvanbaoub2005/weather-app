package com.example.textn.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.textn.data.model.Post
import com.example.textn.data.model.User
import com.example.textn.data.repository.PostRepository
import com.example.textn.data.repository.UserRepository
import kotlinx.coroutines.launch

class AdminViewModel : ViewModel() {
    private val userRepository = UserRepository()
    private val postRepository = PostRepository()

    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> get() = _users

    private val _posts = MutableLiveData<List<Post>>()
    val posts: LiveData<List<Post>> get() = _posts

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    fun fetchUsers() {
        viewModelScope.launch {
            _loading.value = true
            val result = userRepository.getAllUsers()
            _loading.value = false
            if (result.isSuccess) {
                _users.value = result.getOrNull()!!
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Lỗi khi lấy danh sách người dùng"
            }
        }
    }

    fun updateUserRole(userId: String, newRole: String) {
        viewModelScope.launch {
            _loading.value = true
            val result = userRepository.updateUserRole(userId, newRole)
            _loading.value = false
            if (result.isSuccess) {
                fetchUsers()
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Lỗi khi cập nhật vai trò"
            }
        }
    }

    fun updateUserActiveStatus(userId: String, isActive: Boolean) {
        viewModelScope.launch {
            _loading.value = true
            val result = userRepository.updateUserActiveStatus(userId, isActive)
            _loading.value = false
            if (result.isSuccess) {
                fetchUsers()
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Lỗi khi cập nhật trạng thái"
            }
        }
    }
    fun fetchPosts() {
        viewModelScope.launch {
            _loading.value = true
            val result = postRepository.getAllPosts()
            _loading.value = false
            if (result.isSuccess) {
                _posts.value = result.getOrNull()!!
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Lỗi khi lấy danh sách bài viết"
            }
        }
    }
    fun deletePost(postId: String) {
        viewModelScope.launch {
            _loading.value = true
            val result = postRepository.deletePost(postId)
            _loading.value = false
            if (result.isSuccess) {
                fetchPosts()
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Lỗi khi xóa bài viết"
            }
        }
    }
}