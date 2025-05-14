package com.example.textn.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.textn.data.repository.AuthRepository
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository(FirebaseAuth.getInstance())

    // LiveData cho đăng nhập
    private val _loginSuccess = MutableLiveData<Boolean>()
    val loginSuccess: LiveData<Boolean> get() = _loginSuccess

    private val _loginError = MutableLiveData<String>()
    val loginError: LiveData<String> get() = _loginError

    // LiveData cho đăng ký
    private val _registerSuccess = MutableLiveData<Boolean>()
    val registerSuccess: LiveData<Boolean> get() = _registerSuccess

    private val _registerError = MutableLiveData<String>()
    val registerError: LiveData<String> get() = _registerError

    // LiveData cho thông tin người dùng hiện tại
    private val _currentUser = MutableLiveData<FirebaseUser?>()
    val currentUser: LiveData<FirebaseUser?> get() = _currentUser

    init {
        // Kiểm tra người dùng hiện tại mỗi khi ViewModel được khởi tạo
        _currentUser.value = FirebaseAuth.getInstance().currentUser
    }

    /**
     * Đăng nhập bằng email và mật khẩu
     */
    fun loginWithEmail(email: String, password: String) {
        viewModelScope.launch {
            try {
                val result = authRepository.loginWithEmail(email, password)
                result.onSuccess {
                    _currentUser.value = it
                    _loginSuccess.value = true
                }.onFailure {
                    _loginError.value = it.message ?: "Lỗi đăng nhập"
                }
            } catch (e: Exception) {
                _loginError.value = e.message ?: "Lỗi không xác định"
            }
        }
    }

    /**
     * Đăng ký tài khoản mới bằng email/mật khẩu
     */
    fun registerWithEmail(email: String, password: String) {
        viewModelScope.launch {
            try {
                val result = authRepository.registerWithEmail(email, password)
                result.onSuccess {
                    _currentUser.value = it
                    _registerSuccess.value = true
                }.onFailure {
                    _registerError.value = it.message ?: "Lỗi đăng ký"
                }
            } catch (e: Exception) {
                _registerError.value = e.message ?: "Lỗi không xác định"
            }
        }
    }

    /**
     * Đăng nhập bằng Google
     */
    fun signInWithGoogle(credential: AuthCredential) {
        viewModelScope.launch {
            try {
                val result = authRepository.signInWithCredential(credential)
                result.onSuccess {
                    _currentUser.value = it
                    _loginSuccess.value = true
                }.onFailure {
                    _loginError.value = it.message ?: "Lỗi đăng nhập Google"
                }
            } catch (e: Exception) {
                _loginError.value = e.message ?: "Lỗi không xác định"
            }
        }
    }

    /**
     * Đăng xuất
     */
    fun logout() {
        authRepository.logout()
        _currentUser.value = null
    }
}