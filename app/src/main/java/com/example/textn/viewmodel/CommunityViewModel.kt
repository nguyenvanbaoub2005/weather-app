package com.example.textn.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.textn.data.model.Comment
import com.example.textn.data.model.Post
import com.example.textn.data.model.PostLocation
import com.example.textn.data.repository.PostRepository
import com.example.textn.data.services.CloudinaryService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class CommunityViewModel(application: Application) : AndroidViewModel(application) {

    private val postRepository = PostRepository()
    private val cloudinaryService = CloudinaryService(application)

    private val _posts = MutableLiveData<List<Post>>()
    val posts: LiveData<List<Post>> = _posts

    private val _currentPost = MutableLiveData<Post>()
    val currentPost: LiveData<Post> = _currentPost

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _uploadStatus = MutableLiveData<Boolean>()
    val uploadStatus: LiveData<Boolean> = _uploadStatus

    val currentUserId: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    fun loadPosts() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val postsList = postRepository.getPosts()
                _posts.value = postsList
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Đã xảy ra lỗi khi tải bài viết"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadPostById(postId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val post = postRepository.getPostById(postId)
                post?.let {
                    _currentPost.value = it
                } ?: run {
                    _errorMessage.value = "Không tìm thấy bài viết"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Đã xảy ra lỗi khi tải bài viết"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun uploadPostWithImage(imageUri: Uri, description: String, location: PostLocation) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val imageUrlResult = cloudinaryService.uploadImage(imageUri)

                if (imageUrlResult.isSuccess) {
                    val imageUrl = imageUrlResult.getOrThrow()
                    val currentUser = FirebaseAuth.getInstance().currentUser

                    if (currentUser != null) {
                        val post = Post(
                            userId = currentUser.uid,
                            displayName = currentUser.displayName ?: "Người dùng ẩn danh",
                            imageUrl = imageUrl,
                            location = location,
                            description = description,
                            timestamp = System.currentTimeMillis()
                        )

                        val isSuccess = postRepository.createPost(post)
                        _uploadStatus.value = isSuccess
                    } else {
                        _errorMessage.value = "Bạn cần đăng nhập để đăng bài"
                    }
                } else {
                    _errorMessage.value = "Không thể tải lên hình ảnh"
                }

            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Lỗi khi đăng bài"
            } finally {
                _isLoading.value = false
            }
        }
    }

//    fun loadPostsByLocation(latitude: Double, longitude: Double, radiusKm: Double = 10.0) {
//        _isLoading.value = true
//        viewModelScope.launch {
//            try {
//                val postsList = postRepository.getPostsByLocation(latitude, longitude, radiusKm)
//                _posts.value = postsList
//            } catch (e: Exception) {
//                _errorMessage.value = e.message ?: "Lỗi khi tải bài theo vị trí"
//            } finally {
//                _isLoading.value = false
//            }
//        }
//    }

    fun loadPostsByUserId(userId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val postsList = postRepository.getPostsByUserId(userId)
                _posts.value = postsList
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Lỗi khi tải bài của người dùng"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleLike(postId: String, userId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val success = postRepository.toggleLike(postId, userId)

                if (success) {
                    val updatedPost = postRepository.getPostById(postId)
                    updatedPost?.let { post ->
                        _currentPost.value = post

                        val currentPosts = _posts.value
                        if (!currentPosts.isNullOrEmpty()) {
                            val updatedPosts = currentPosts.map {
                                if (it.id == postId) post else it
                            }
                            _posts.value = updatedPosts
                        }
                    } ?: run {
                        _errorMessage.value = "Không thể cập nhật bài viết sau khi like"
                    }
                } else {
                    _errorMessage.value = "Không thể thực hiện hành động like"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Lỗi khi like bài viết"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun isPostLikedByCurrentUser(postId: String): Boolean {
        val userId = currentUserId ?: return false
        val post = _currentPost.value ?: return false
        return post.likedUserIds.contains(userId)
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

                    val success = postRepository.addComment(postId, comment)
                    if (success) {
                        loadPostById(postId) // Tự động reload lại bài viết có comment
                    } else {
                        _errorMessage.value = "Không thể thêm bình luận"
                    }
                } else {
                    _errorMessage.value = "Bạn cần đăng nhập để bình luận"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Lỗi khi thêm bình luận"
            }
        }
    }
}
