package com.example.textn.ui.view.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.textn.databinding.ActivityAdminBinding
import com.example.textn.ui.view.fragment.PostManagementFragment
import com.example.textn.ui.view.fragment.UserManagementFragment
import com.google.android.material.tabs.TabLayoutMediator

class AdminActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupLogoutButton()
        setupViewPager()
    }

    private fun setupViewPager() {
        val fragments = listOf(UserManagementFragment(), PostManagementFragment())
        val adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = fragments.size
            override fun createFragment(position: Int): Fragment = fragments[position]
        }

        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Quản lý người dùng"
                1 -> "Quản lý bài viết"
                else -> ""
            }
        }.attach()
    }
    private fun setupLogoutButton() {
        binding.btnLogout.setOnClickListener {
            Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}