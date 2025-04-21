package com.example.textn.ui.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.textn.R
import com.example.textn.databinding.ActivityLoginBinding
import com.example.textn.ui.view.component.LoadingHandler
import com.example.textn.utils.PasswordVisibility
import com.example.textn.utils.Validate
import com.example.textn.viewmodel.auth.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var passwordVisibility: PasswordVisibility
    private lateinit var auth: FirebaseAuth
    private val authViewModel: AuthViewModel by viewModels() // Sử dụng ViewModel
    private var loadingHandler = LoadingHandler(supportFragmentManager) //Loading
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        // Quan sát kết quả đăng nhập
        authViewModel.loginSuccess.observe(this, Observer {
            if (it) {
                Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        })
        authViewModel.loginError.observe(this, Observer {
            Toast.makeText(this, "Lỗi: $it", Toast.LENGTH_SHORT).show()
            loadingHandler.hideLoading()
        })
        // Đăng nhập
        binding.btnSignIn.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val validate = Validate()
            if (validate.validateInput(email, password, binding.etEmail, binding.etPassword)) {
                loadingHandler.showLoading()
                authViewModel.loginWithEmail(email, password)
            }
        }

        // Ẩn/hiện mật khẩu
        passwordVisibility = PasswordVisibility(binding.etPassword, binding.ivTogglePassword)
        binding.ivTogglePassword.setOnClickListener {
            passwordVisibility.toggle()
        }

        // Quên mật khẩu
        binding.tvForgotPassword.setOnClickListener {
            Toast.makeText(this, "Forgot Password feature coming soon", Toast.LENGTH_SHORT).show()
        }

        // Đăng ký
        binding.tvSignUp.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }

        setupGoogleLogin()
    }





    //Login bằng google
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
            loadingHandler.hideLoading()
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            if (task.isSuccessful) {
                val account = task.result
                firebaseAuthWithGoogle(account.idToken!!)
            } else {
                Toast.makeText(this, "Đăng nhập Google thất bại", Toast.LENGTH_SHORT).show()
                loadingHandler.hideLoading()
            }
        }
    }
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Toast.makeText(this, "Đăng nhập Google thành công", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Firebase Auth thất bại", Toast.LENGTH_SHORT).show()
                }
            }
    }




}