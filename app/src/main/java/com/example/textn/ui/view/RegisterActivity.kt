package com.example.textn.ui.view
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.textn.R
import com.example.textn.databinding.ActivityRegisterBinding
import com.example.textn.ui.view.component.LoadingHandler
import com.example.textn.utils.PasswordVisibility
import com.example.textn.utils.Validate
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var loadingHandler = LoadingHandler(supportFragmentManager)
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001

    private val categoryList = listOf("Thời trang", "Điện tử", "Gia dụng", "Sách", "Thực phẩm", "Mỹ phẩm", "Khác...")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        setupCategorySpinner()

        binding.roleGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == binding.rbSeller.id) {
                binding.sellerInfoLayout.visibility = View.VISIBLE
                binding.llSocialRegister.visibility = View.GONE
                binding.tvOr.visibility = View.GONE
            } else {
                binding.sellerInfoLayout.visibility = View.GONE
                binding.llSocialRegister.visibility = View.VISIBLE
                binding.tvOr.visibility = View.VISIBLE
            }
        }
        var passwordVisibility = PasswordVisibility(binding.etPassword, binding.ivTogglePassword)
        binding.ivTogglePassword.setOnClickListener {
            passwordVisibility.toggle()
        }
        var confirmPasswordVisibility = PasswordVisibility(binding.etConfirmPassword, binding.ivToggleCofirmPassword)
        binding.ivToggleCofirmPassword.setOnClickListener {
            confirmPasswordVisibility.toggle()
        }

        binding.btnSignUp.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()
            val role = if (binding.rbSeller.isChecked) "seller" else "user"
            val shopName = binding.etShopName.text.toString().trim()
            val category = binding.spCategory.selectedItem.toString()

            var validate = Validate()
            if (validate.validateRegister(email, password, confirmPassword, role, shopName, category, binding.etEmail, binding.etPassword, binding.etConfirmPassword, binding.etShopName, binding.spCategory)) {
                loadingHandler.showLoading()
                registerUser(email, password, role, shopName, category)
                Log.d("RegisterActivity", role + shopName + category)
                loadingHandler.hideLoading()
            }
        }

        //Trở về trang đăng nhập
        binding.tvSignIn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        setupGoogleLogin()
    }



    private fun setupCategorySpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spCategory.adapter = adapter
    }




    private fun registerUser(
        email: String,
        password: String,
        role: String,
        shopName: String,
        category: String
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                Toast.makeText(this,"Đăng ký thành công",Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
//                val userId = auth.currentUser?.uid ?: return@addOnSuccessListener
//                val userMap = hashMapOf(
//                    "email" to email,
//                    "role" to role,
//                    "createdAt" to System.currentTimeMillis()
//                )
//                if (role == "seller") {
//                    userMap["shopName"] = shopName
//                    userMap["category"] = category
//                }
//
//                firestore.collection("users")
//                    .document(userId)
//                    .set(userMap)
//                    .addOnSuccessListener {
//                        showToast("Đăng ký thành công với vai trò $role")
//                        finish() // hoặc chuyển sang màn hình đăng nhập
//                    }
//                    .addOnFailureListener {
//                        showToast("Lưu thông tin thất bại: ${it.localizedMessage}")
//                    }
            }
            .addOnFailureListener {
                showToast("Lỗi đăng ký: ${it.localizedMessage}")
            }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
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