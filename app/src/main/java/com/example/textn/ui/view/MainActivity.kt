package com.example.textn.ui.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.example.textn.R
import com.google.android.material.navigation.NavigationView
class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawerLayout)
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as androidx.navigation.fragment.NavHostFragment
        val navController = navHostFragment.navController


        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_home, R.id.nav_weather, R.id.nav_settings),
            drawerLayout
        )

        val navigationView: NavigationView = findViewById(R.id.navigation_view)

//        // Xử lý nút menu (ImageButton) để mở drawer
//        val btnMenu: ImageButton = findViewById(R.id.btnMenu)
//        btnMenu.setOnClickListener {
//            drawerLayout.openDrawer(GravityCompat.START)
//        }

        // Xử lý khi người dùng chọn item trong drawer menu
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

                else -> false
            }
            menuItem.isChecked = true
            drawerLayout.closeDrawer(GravityCompat.START)
            handled
        }
    }
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
