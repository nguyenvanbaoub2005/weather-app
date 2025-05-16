package com.example.textn.data.repository

import com.example.textn.data.model.Comment
import com.example.textn.data.model.Post
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PostRepository {
    private val db = FirebaseFirestore.getInstance()
    private val postsCollection = db.collection("posts")

    suspend fun getPosts(): List<Post> = withContext(Dispatchers.IO) {
        try {
            val snapshot = postsCollection
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            val posts = mutableListOf<Post>()

            // Xử lý từng document để lấy thông tin likes
            for (document in snapshot.documents) {
                val post = document.toObject(Post::class.java)?.copy(id = document.id)

                if (post != null) {
                    // Lấy danh sách người dùng đã like bài viết
                    val likesSnapshot = document.reference.collection("likes").get().await()
                    val likedUserIds = likesSnapshot.documents.map { it.id }

                    // Cập nhật post với danh sách người đã like
                    val updatedPost = post.copy(likedUserIds = likedUserIds)
                    posts.add(updatedPost)
                }
            }

            return@withContext posts
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext emptyList()
        }
    }

    suspend fun getPostById(postId: String): Post? = withContext(Dispatchers.IO) {
        try {
            val document = postsCollection.document(postId).get().await()
            val post = document.toObject(Post::class.java)?.copy(id = document.id)

            if (post != null) {
                // Lấy danh sách người dùng đã like bài viết
                val likesSnapshot = document.reference.collection("likes").get().await()
                val likedUserIds = likesSnapshot.documents.map { it.id }

                // Cập nhật post với danh sách người đã like
                return@withContext post.copy(likedUserIds = likedUserIds)
            }

            return@withContext null
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }

    suspend fun createPost(post: Post): Boolean = withContext(Dispatchers.IO) {
        try {
            val documentRef = postsCollection.document()
            val postWithId = post.copy(id = documentRef.id)
            documentRef.set(postWithId).await()
            return@withContext true
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }

    suspend fun getPostsByUserId(userId: String): Result<List<Post>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val querySnapshot = postsCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()

            // Sắp xếp thủ công bằng Kotlin sau khi lấy dữ liệu
            val posts = querySnapshot.documents.mapNotNull { document ->
                val post = document.toObject(Post::class.java)?.copy(id = document.id)
                post?.let {
                    val likedUserIds = document.reference
                        .collection("likes")
                        .get()
                        .await()
                        .documents
                        .map { it.id }
                    it.copy(likedUserIds = likedUserIds)
                }
            }.sortedByDescending { it.timestamp }

            Result.success(posts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


//    suspend fun getPostsByLocation(latitude: Double, longitude: Double, radiusKm: Double): List<Post> {
//        // Tính toán phạm vi tọa độ dựa trên bán kính
//        val latDelta = radiusKm / 411.0  // Khoảng 111km/1 độ latitude
//        val lonDelta = radiusKm / (411.0 * Math.cos(Math.toRadians(latitude)))
//
//        val minLat = latitude - latDelta
//        val maxLat = latitude + latDelta
//        val minLon = longitude - lonDelta
//        val maxLon = longitude + lonDelta
//
//        return withContext(Dispatchers.IO) {
//            try {
//                val snapshot = postsCollection
//                    .whereGreaterThanOrEqualTo("location.latitude", minLat)
//                    .whereLessThanOrEqualTo("location.latitude", maxLat)
//                    .get()
//                    .await()
//
//                val posts = mutableListOf<Post>()
//
//                for (document in snapshot.documents) {
//                    val post = document.toObject(Post::class.java)?.copy(id = document.id)
//
//                    if (post != null && post.location.longitude in minLon..maxLon) {
//                        // Lấy danh sách người dùng đã like bài viết
//                        val likesSnapshot = document.reference.collection("likes").get().await()
//                        val likedUserIds = likesSnapshot.documents.map { it.id }
//
//                        // Cập nhật post với danh sách người đã like
//                        val updatedPost = post.copy(likedUserIds = likedUserIds)
//                        posts.add(updatedPost)
//                    }
//                }
//
//                return@withContext posts
//            } catch (e: Exception) {
//                e.printStackTrace()
//                return@withContext emptyList()
//            }
//        }
//    }

    suspend fun toggleLike(postId: String, userId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val postRef = postsCollection.document(postId)
            val likeRef = postRef.collection("likes").document(userId)

            val likeDoc = likeRef.get().await()

            if (likeDoc.exists()) {
                // Nếu đã like rồi → unlike
                db.runTransaction { transaction ->
                    val snapshot = transaction.get(postRef)
                    val currentLikes = snapshot.getLong("likes") ?: 0
                    transaction.update(postRef, "likes", Math.max(0, currentLikes - 1))
                    transaction.delete(likeRef)
                }.await()
            } else {
                // Nếu chưa like → like
                db.runTransaction { transaction ->
                    val snapshot = transaction.get(postRef)
                    val currentLikes = snapshot.getLong("likes") ?: 0
                    transaction.update(postRef, "likes", currentLikes + 1)
                    transaction.set(likeRef, mapOf("timestamp" to FieldValue.serverTimestamp()))
                }.await()
            }

            return@withContext true
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }

    suspend fun isPostLikedByUser(postId: String, userId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val likeRef = postsCollection.document(postId).collection("likes").document(userId)
            val document = likeRef.get().await()
            return@withContext document.exists()
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }

    suspend fun addComment(postId: String, comment: Comment): Boolean = withContext(Dispatchers.IO) {
        try {
            val postRef = postsCollection.document(postId)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(postRef)
                val currentComments = snapshot.toObject(Post::class.java)?.comments ?: emptyList()
                transaction.update(postRef, "comments", currentComments + comment)
            }.await()
            return@withContext true
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }  /**
     * Lấy danh sách tất cả bài viết
     */
    suspend fun getAllPosts(): Result<List<Post>> {
        return try {
            val snapshot = postsCollection.get().await()
            val posts = snapshot.documents.mapNotNull { it.toObject(Post::class.java) }
            Result.success(posts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Xóa bài viết
     */
    suspend fun deletePost(postId: String): Result<Unit> {
        return try {
            postsCollection.document(postId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


}