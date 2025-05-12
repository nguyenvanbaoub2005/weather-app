package com.example.textn.viewmodel

import PostRepository
import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.textn.data.model.Comment
import com.example.textn.data.model.Post
import com.example.textn.data.model.PostLocation
import com.example.textn.data.services.CloudinaryService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class CommunityViewModel(application: Application) : AndroidViewModel(application) {

    private val postRepository = PostRepository()
    private val cloudinaryService = CloudinaryService(application)

    private val _posts = MutableLiveData<List<Post>>()
    val posts: LiveData<List<Post>> = _posts

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _uploadStatus = MutableLiveData<Boolean>()
    val uploadStatus: LiveData<Boolean> = _uploadStatus

    fun loadPosts() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val postsList = postRepository.getPosts()
                _posts.value = postsList
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = e.message
                _isLoading.value = false
            }
        }
    }

    fun uploadPostWithImage(imageUri: Uri, description: String, location: PostLocation) {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                // Upload hình ảnh lên Cloudinary
                val imageUrlResult = cloudinaryService.uploadImage(imageUri)

                if (imageUrlResult.isSuccess) {
                    val imageUrl = imageUrlResult.getOrThrow()

                    // Lấy thông tin người dùng hiện tại
                    val currentUser = FirebaseAuth.getInstance().currentUser

                    if (currentUser != null) {
                        // Tạo post mới
                        val post = Post(
                            userId = currentUser.uid,
                            displayName = currentUser.displayName ?: "Người dùng ẩn danh",
                            imageUrl = imageUrl,
                            location = location,
                            description = description,
                            timestamp = System.currentTimeMillis()
                        )

                        // Lưu post vào Firestore
                        val isSuccess = postRepository.createPost(post)
                        _uploadStatus.value = isSuccess
                    } else {
                        _errorMessage.value = "Bạn cần đăng nhập để đăng bài"
                    }
                } else {
                    _errorMessage.value = "Không thể tải lên hình ảnh"
                }

            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadPostsByLocation(latitude: Double, longitude: Double, radiusKm: Double = 10.0) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val postsList = postRepository.getPostsByLocation(latitude, longitude, radiusKm)
                _posts.value = postsList
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = e.message
                _isLoading.value = false
            }
        }
    }

    fun likePost(postId: String) {
        viewModelScope.launch {
            try {
                postRepository.likePost(postId)
                // Tải lại danh sách để cập nhật UI
                loadPosts()
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun addComment(postId: String, commentText: String) {
        viewModelScope.launch {
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser

                if (currentUser != null) {
                    val comment = Comment(
                        userId = currentUser.uid,
                        displayName = currentUser.displayName ?: "Người dùng ẩn danh",
                        text = commentText,
                        timestamp = System.currentTimeMillis()
                    )

                    postRepository.addComment(postId, comment)
                    // Tải lại danh sách để cập nhật UI
                    loadPosts()
                } else {
                    _errorMessage.value = "Bạn cần đăng nhập để bình luận"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }
}