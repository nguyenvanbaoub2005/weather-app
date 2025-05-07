package com.example.textn.ui.view.activity

import android.content.Intent
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
import com.example.textn.R
import com.example.textn.data.local.UserPreferences
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var userPrefs: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Khởi tạo UserPreferences
        userPrefs = UserPreferences(this)

        // Ánh xạ các thành phần
        drawerLayout = findViewById(R.id.drawerLayout)
        val navigationView: NavigationView = findViewById(R.id.navigation_view)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as androidx.navigation.fragment.NavHostFragment
        val navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_home, R.id.nav_weather, R.id.nav_settings),
            drawerLayout
        )

        // Cập nhật thông tin header
        updateDrawerHeader()

        // Xử lý sự kiện chọn mục trong Navigation Drawer
        navigationView.setNavigationItemSelectedListener { menuItem ->
            val handled = when (menuItem.itemId) {
                R.id.nav_home,
                R.id.nav_weather,
                R.id.nav_health,
                R.id.nav_settings -> {
                    navController.popBackStack()
                    navController.navigate(menuItem.itemId)
                    true
                }
                R.id.nav_gemini_ai -> {
                    // Điều hướng đến Gemini AI Fragment
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

    // Cập nhật thông tin người dùng ở phần header của Navigation Drawer
    private fun updateDrawerHeader() {
        val username = userPrefs.getUserName() ?: "Nguyễn Văn Bảo"
        val email = userPrefs.getUserEmail() ?: "Chưa có email"

        val navigationView: NavigationView = findViewById(R.id.navigation_view)
        val headerView: View = navigationView.getHeaderView(0)

        val avatarImageView = headerView.findViewById<ImageView>(R.id.avatar)
        val usernameTextView = headerView.findViewById<TextView>(R.id.username)
        val proStatusTextView = headerView.findViewById<TextView>(R.id.proStatus)
        val logoutButton = headerView.findViewById<Button>(R.id.logoutButton)

        usernameTextView.text = username
        proStatusTextView.text = if (email.isNotEmpty()) "Có PRO" else "Không phải PRO"
        avatarImageView.setImageResource(R.drawable.__1_bao)

        // Bắt sự kiện Đăng xuất
        logoutButton.setOnClickListener {
            userPrefs.clearUserData()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
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
