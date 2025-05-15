package com.example.textn.data.repository

import com.example.textn.data.model.User
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    /**
     * Lưu thông tin người dùng vào Firestore
     */
    suspend fun saveUser(user: User): Result<User> {
        return try {
            usersCollection.document(user.id).set(user).await()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    /**
     * Kiểm tra xem người dùng đã tồn tại trong database chưa
     */
    suspend fun checkUserExists(userId: String): Boolean {
        return try {
            val document = usersCollection.document(userId).get().await()
            document.exists()
        } catch (e: Exception) {
            false
        }
    }
    /**
     * Lấy thông tin người dùng từ Firestore
     */
    suspend fun getUser(userId: String): Result<User> {
        return try {
            val document = usersCollection.document(userId).get().await()
            if (document.exists()) {
                val user = document.toObject(User::class.java)
                Result.success(user!!)
            } else {
                Result.failure(Exception("Không tìm thấy người dùng"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    /**
     * Chuyển đổi từ FirebaseUser sang User model
     */
    fun mapFirebaseUser(firebaseUser: FirebaseUser): User {
        return User(
            id = firebaseUser.uid,
            email = firebaseUser.email ?: "",
            displayName = firebaseUser.displayName ?: "",
            photoUrl = firebaseUser.photoUrl?.toString() ?: "",
            createdAt = System.currentTimeMillis(),
            lastLogin = System.currentTimeMillis(),
            isActive = true,
            role = "user"
        )
    }

    /**
     * Cập nhật vai trò của người dùng
     */
    suspend fun updateUserRole(userId: String, newRole: String): Result<Unit> {
        return try {
            usersCollection.document(userId).update("role", newRole).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Cập nhật trạng thái hoạt động của người dùng
     */
    suspend fun updateUserActiveStatus(userId: String, isActive: Boolean): Result<Unit> {
        return try {
            usersCollection.document(userId).update("isActive", isActive).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Lấy danh sách tất cả người dùng
     */
    suspend fun getAllUsers(): Result<List<User>> {
        return try {
            val snapshot = usersCollection.get().await()
            val users = snapshot.documents.mapNotNull { it.toObject(User::class.java) }
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Cập nhật role cho người dùng hiện có nếu thiếu
     */
    suspend fun updateUsersWithRole() {
        try {
            val users = usersCollection.get().await()
            users.documents.forEach { doc ->
                if (!doc.contains("role")) {
                    doc.reference.update("role", "user").await()
                }
            }
        } catch (e: Exception) {
            // Xử lý lỗi nếu cần
        }
    }
}