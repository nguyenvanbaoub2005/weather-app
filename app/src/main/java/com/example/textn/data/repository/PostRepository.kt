import com.example.textn.data.model.Comment
import com.example.textn.data.model.Post
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

            return@withContext snapshot.toObjects(Post::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext emptyList()
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

    suspend fun getPostsByUserId(userId: String): List<Post> = withContext(Dispatchers.IO) {
        try {
            val snapshot = postsCollection
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            return@withContext snapshot.toObjects(Post::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext emptyList()
        }
    }

    suspend fun getPostsByLocation(latitude: Double, longitude: Double, radiusKm: Double): List<Post> {
        // Tính toán phạm vi tọa độ dựa trên bán kính
        val latDelta = radiusKm / 111.0  // Khoảng 111km/1 độ latitude
        val lonDelta = radiusKm / (111.0 * Math.cos(Math.toRadians(latitude)))

        val minLat = latitude - latDelta
        val maxLat = latitude + latDelta
        val minLon = longitude - lonDelta
        val maxLon = longitude + lonDelta

        return withContext(Dispatchers.IO) {
            try {
                val snapshot = postsCollection
                    .whereGreaterThanOrEqualTo("location.latitude", minLat)
                    .whereLessThanOrEqualTo("location.latitude", maxLat)
                    .get()
                    .await()

                val posts = snapshot.toObjects(Post::class.java)

                // Lọc thêm theo longitude (vì Firestore chỉ hỗ trợ 1 phạm vi inequality)
                return@withContext posts.filter { post ->
                    post.location.longitude in minLon..maxLon
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext emptyList()
            }
        }
    }

    suspend fun likePost(postId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val postRef = postsCollection.document(postId)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(postRef)
                val currentLikes = snapshot.getLong("likes") ?: 0
                transaction.update(postRef, "likes", currentLikes + 1)
            }.await()
            return@withContext true
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
    }
}