package com.example.textn.viewmodel.auth



import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.textn.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val authRepository = AuthRepository(FirebaseAuth.getInstance())

    // LiveData để quan sát kết quả đăng nhập
    private val _loginSuccess = MutableLiveData<Boolean>()
    val loginSuccess: LiveData<Boolean> get() = _loginSuccess

    private val _loginError = MutableLiveData<String>()
    val loginError: LiveData<String> get() = _loginError


    // Hàm đăng nhập
    fun loginWithEmail(email: String, password: String) {
        viewModelScope.launch {
            val result = authRepository.loginWithEmail(email, password)
            result.onSuccess {
                _loginSuccess.value = true // Đăng nhập thành công
            }.onFailure {
                _loginError.value = it.message // Đăng nhập thất bại, trả về thông báo lỗi
            }
        }
    }
}