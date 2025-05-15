package com.example.textn.data.repository

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class AuthRepository(private val auth: FirebaseAuth) {
    private val userRepository = UserRepository()

    /**
     * Đăng nhập bằng email và mật khẩu
     */
    suspend fun loginWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("Không thể đăng nhập")

            // Kiểm tra và lưu thông tin người dùng vào Firestore nếu chưa có
            saveUserToFirestore(user)

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Đăng ký tài khoản mới bằng email và mật khẩu
     */
    suspend fun registerWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("Không thể đăng ký")

            // Lưu thông tin người dùng vào Firestore
            saveUserToFirestore(user)

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Đăng nhập bằng Google
     */
    suspend fun signInWithCredential(credential: AuthCredential): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithCredential(credential).await()
            val user = result.user ?: throw Exception("Không thể đăng nhập")

            // Lưu thông tin người dùng vào Firestore
            saveUserToFirestore(user)

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Lưu thông tin người dùng vào Firestore
     */
    private suspend fun saveUserToFirestore(firebaseUser: FirebaseUser) {
        val userExists = userRepository.checkUserExists(firebaseUser.uid)

        if (!userExists) {
            val user = userRepository.mapFirebaseUser(firebaseUser)
            userRepository.saveUser(user)
        }
    }

    /**
     * Đăng xuất khỏi tài khoản
     */
    fun logout() {
        auth.signOut()
    }
    /**
     * Kiểm tra vai trò của người dùng
     */
    suspend fun checkRole(userId: String): String? {
        return try {
            val result = userRepository.getUser(userId)
            if (result.isSuccess) {
                val user = result.getOrNull()
                println("User: $user")
                user?.role ?: "user"
            } else {
                println("Get user failed: ${result.exceptionOrNull()}")
                null
            }
        } catch (e: Exception) {
            println("Error: $e")
            null
        }
    }
}