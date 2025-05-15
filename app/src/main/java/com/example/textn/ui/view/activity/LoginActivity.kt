package com.example.textn.ui.view.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.textn.R
import com.example.textn.data.local.UserPreferences
import com.example.textn.databinding.ActivityLoginBinding
import com.example.textn.ui.view.component.LoadingHandler
import com.example.textn.utils.PasswordVisibility
import com.example.textn.utils.Validate
import com.example.textn.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.textn.data.repository.AuthRepository

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var passwordVisibility: PasswordVisibility
    private lateinit var auth: FirebaseAuth
    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var loadingHandler: LoadingHandler
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001
    private lateinit var userPrefs: UserPreferences
    private lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        userPrefs = UserPreferences(this)
        loadingHandler = LoadingHandler(supportFragmentManager)
        authRepository = AuthRepository(auth)

        setupObservers()
        setupUI()
        setupGoogleLogin()
    }

    private fun setupObservers() {
        // Quan sát kết quả đăng nhập thành công
        authViewModel.loginSuccess.observe(this, Observer { success ->
            loadingHandler.hideLoading()
            if (success) {
                val user = auth.currentUser
                saveUserInfo(user?.email, user?.displayName)
                Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show()

                // Kiểm tra vai trò của người dùng
                CoroutineScope(Dispatchers.Main).launch {
                    val role = user?.uid?.let { authRepository.checkRole(it) }
                    if (role == "admin") {
                        startActivity(Intent(this@LoginActivity, AdminActivity::class.java))
                    } else {
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    }
                    finish()
                }
            }
        })

        // Quan sát lỗi đăng nhập
        authViewModel.loginError.observe(this, Observer { error ->
            loadingHandler.hideLoading()
            Toast.makeText(this, "Lỗi: $error", Toast.LENGTH_SHORT).show()
        })

        // Quan sát thay đổi thông tin người dùng hiện tại
        authViewModel.currentUser.observe(this, Observer { user ->
            user?.let {
                // Lưu thông tin người dùng vào SharedPreferences
                saveUserInfo(it.email, it.displayName)
            }
        })
    }

    private fun setupUI() {
        passwordVisibility = PasswordVisibility(binding.etPassword, binding.ivTogglePassword)

        binding.btnSignIn.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val validate = Validate()

            if (validate.validateInput(email, password, binding.etEmail, binding.etPassword)) {
                loadingHandler.showLoading()
                authViewModel.loginWithEmail(email, password)
            }
        }

        binding.ivTogglePassword.setOnClickListener {
            passwordVisibility.toggle()
        }

        binding.tvForgotPassword.setOnClickListener {
            Toast.makeText(this, "Chức năng quên mật khẩu sẽ sớm có mặt", Toast.LENGTH_SHORT).show()
        }

        binding.tvSignUp.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }

    private fun setupGoogleLogin() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.btnGoogleLogin.setOnClickListener {
            googleSignInClient.signOut().addOnCompleteListener {
                loadingHandler.showLoading()
                val signInIntent = googleSignInClient.signInIntent
                startActivityForResult(signInIntent, RC_SIGN_IN)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            if (task.isSuccessful) {
                val account = task.result
                // Thay vì gọi firebaseAuthWithGoogle, chúng ta sử dụng ViewModel
                val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)
                authViewModel.signInWithGoogle(credential)
            } else {
                Toast.makeText(this, "Đăng nhập Google thất bại", Toast.LENGTH_SHORT).show()
                loadingHandler.hideLoading()
            }
        }
    }

    private fun saveUserInfo(email: String?, displayName: String?) {
        userPrefs.saveUserEmail(email ?: "")
        userPrefs.saveUserName(displayName ?: "")
    }
}