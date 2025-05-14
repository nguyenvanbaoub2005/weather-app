package com.example.textn.ui.view.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.textn.R
import com.example.textn.data.local.UserPreferences
import com.example.textn.databinding.ActivityRegisterBinding
import com.example.textn.ui.view.component.LoadingHandler
import com.example.textn.utils.PasswordVisibility
import com.example.textn.utils.Validate
import com.example.textn.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var loadingHandler: LoadingHandler
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001
    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var userPrefs: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        userPrefs = UserPreferences(this)
        loadingHandler = LoadingHandler(supportFragmentManager)

        setupObservers()
        setupUI()
        setupGoogleLogin()
    }

    private fun setupObservers() {
        // Quan sát kết quả đăng ký thành công
        authViewModel.registerSuccess.observe(this, Observer { success ->
            loadingHandler.hideLoading()
            if (success) {
                Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        })

        // Quan sát lỗi đăng ký
        authViewModel.registerError.observe(this, Observer { error ->
            loadingHandler.hideLoading()
            Toast.makeText(this, "Lỗi đăng ký: $error", Toast.LENGTH_SHORT).show()
        })

        // Quan sát kết quả đăng nhập Google thành công
        authViewModel.loginSuccess.observe(this, Observer { success ->
            loadingHandler.hideLoading()
            if (success) {
                val user = auth.currentUser
                saveUserInfo(user?.email, user?.displayName)
                Toast.makeText(this, "Đăng nhập Google thành công", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        })

        // Quan sát lỗi đăng nhập Google
        authViewModel.loginError.observe(this, Observer { error ->
            loadingHandler.hideLoading()
            Toast.makeText(this, "Lỗi: $error", Toast.LENGTH_SHORT).show()
        })
    }

    private fun setupUI() {
        // Setup password visibility toggle
        val passwordVisibility = PasswordVisibility(binding.etPassword, binding.ivTogglePassword)
        binding.ivTogglePassword.setOnClickListener {
            passwordVisibility.toggle()
        }

        val confirmPasswordVisibility = PasswordVisibility(binding.etConfirmPassword, binding.ivToggleCofirmPassword)
        binding.ivToggleCofirmPassword.setOnClickListener {
            confirmPasswordVisibility.toggle()
        }

        // Đăng ký bằng email và mật khẩu
        binding.btnSignUp.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            val validate = Validate()
            if (validate.validateRegister(email, password, confirmPassword, binding.etEmail, binding.etPassword, binding.etConfirmPassword)) {
                loadingHandler.showLoading()
                authViewModel.registerWithEmail(email, password)
            }
        }

        // Trở về trang đăng nhập
        binding.tvSignIn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun setupGoogleLogin() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.btnGoogleRegister.setOnClickListener {
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
                // Sử dụng ViewModel thay vì gọi trực tiếp
                val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)
                authViewModel.signInWithGoogle(credential)
            } else {
                loadingHandler.hideLoading()
                Toast.makeText(this, "Đăng nhập Google thất bại", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveUserInfo(email: String?, displayName: String?) {
        userPrefs.saveUserEmail(email ?: "")
        userPrefs.saveUserName(displayName ?: "")
    }
}