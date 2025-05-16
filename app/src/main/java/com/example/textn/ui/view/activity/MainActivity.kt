package com.example.textn.ui.view.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.bumptech.glide.Glide
import com.example.textn.R
import com.example.textn.data.local.UserPreferences
import com.example.textn.ui.view.component.DraggableFloatingActionButton
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var userPrefs: UserPreferences
    private lateinit var fabAskAi: DraggableFloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Khởi tạo UserPreferences
        userPrefs = UserPreferences(this)

        // Ánh xạ các thành phần
        drawerLayout = findViewById(R.id.drawerLayout)
        val navigationView: NavigationView = findViewById(R.id.navigation_view_inner)

        // Khởi tạo nút AI có thể kéo lên kéo xuống
        setupDraggableAiButton()

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as androidx.navigation.fragment.NavHostFragment
        val navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_home, R.id.nav_weather, R.id.nav_settings),
            drawerLayout
        )

        // Cập nhật thông tin header
        updateDrawerHeader()

        // Thiết lập footer (đã được include trong layout, chỉ cần xử lý sự kiện)
        setupNavigationFooter()

        // Xử lý sự kiện chọn mục trong Navigation Drawer
        navigationView.setNavigationItemSelectedListener { menuItem ->
            val handled = when (menuItem.itemId) {
                R.id.nav_home,
                R.id.nav_weather,
                R.id.nav_health,
                R.id.nav_place_recommend,
                R.id.nav_communityFragment,
                R.id.nav_settings -> {
                    navController.popBackStack()
                    navController.navigate(menuItem.itemId)
                    true
                }
                R.id.nav_gemini_ai -> {
                    navController.popBackStack()
                    navController.navigate(R.id.geminiAIFragment)
                    true
                }
                else -> false
            }
            menuItem.isChecked = true
            drawerLayout.closeDrawer(GravityCompat.START)
            handled
        }
    }

    // Thiết lập nút AI có thể kéo
    private fun setupDraggableAiButton() {
        fabAskAi = findViewById(R.id.fab_ask_ai)

        fabAskAi.setOnClickListener {
            val navController = findNavController(R.id.nav_host_fragment)
            navController.popBackStack()
            navController.navigate(R.id.geminiAIFragment)
        }
    }

    // Cập nhật thông tin người dùng ở phần header của Navigation Drawer
    private fun updateDrawerHeader() {
        val headerView: View = findViewById(R.id.nav_header)

        val avatarImageView = headerView.findViewById<ImageView>(R.id.avatar)
        val usernameTextView = headerView.findViewById<TextView>(R.id.username)
        val logoutButton = headerView.findViewById<Button>(R.id.btnLogout)

        val account = com.google.android.gms.auth.api.signin.GoogleSignIn.getLastSignedInAccount(this)

        if (account != null) {
            val displayName = account.displayName
            val email = account.email ?: "unknown@example.com"

            val username = if (!displayName.isNullOrEmpty()) {
                displayName
            } else {
                email.split("@").firstOrNull() ?: "Người dùng"
            }

            usernameTextView.text = username

            if (account.photoUrl != null) {
                Glide.with(this)
                    .load(account.photoUrl)
                    .placeholder(R.drawable.image_user)
                    .error(R.drawable.image_user)
                    .into(avatarImageView)
            } else {
                avatarImageView.setImageResource(R.drawable.image_user)
            }
        } else {
            val username = userPrefs.getUserName()
            val email = userPrefs.getUserEmail() ?: "user@example.com"

            val displayName = if (!username.isNullOrEmpty()) {
                username
            } else {
                email.split("@").firstOrNull() ?: "Người dùng"
            }

            usernameTextView.text = displayName
            avatarImageView.setImageResource(R.drawable.image_user)
        }

        logoutButton.setOnClickListener {
            com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(
                this,
                com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
            ).signOut()

            userPrefs.clearUserData()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // Thiết lập footer
    private fun setupNavigationFooter() {
        val footerView: View = findViewById(R.id.nav_footer)

        val emailButton = footerView.findViewById<ImageView>(R.id.footer_email)
        val facebookButton = footerView.findViewById<ImageView>(R.id.footer_facebook)
        val instagramButton = footerView.findViewById<ImageView>(R.id.footer_instagram)
        val twitterButton = footerView.findViewById<ImageView>(R.id.footer_twitter)

        emailButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("mailto:"))
            startActivity(intent)
        }

        facebookButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com"))
            startActivity(intent)
        }

        instagramButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com"))
            startActivity(intent)
        }

        twitterButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.twitter.com"))
            startActivity(intent)
        }
    }

    // Hàm mở Drawer
    fun openDrawer() {
        drawerLayout.openDrawer(GravityCompat.START)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return NavigationUI.navigateUp(navController, appBarConfiguration)
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}