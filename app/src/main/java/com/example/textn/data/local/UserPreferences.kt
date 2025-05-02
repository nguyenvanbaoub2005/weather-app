package com.example.textn.data.local

import android.content.Context
import android.util.Log

class UserPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    fun saveUserEmail(email: String) {
        prefs.edit().putString("email", email).apply()
    }

    fun saveUserName(name: String) {
        prefs.edit().putString("name", name).apply()
    }

    fun getUserEmail(): String? = prefs.getString("email", null)

    fun getUserName(): String? = prefs.getString("name", null)

    fun clear() {
        prefs.edit().clear().apply()
        Log.d("UserPreferences", "All user data cleared.")
    }

    fun clearUserData() {
        val editor = prefs.edit()
        editor.remove("name")
        editor.remove("email")
        editor.apply()
        Log.d("UserPreferences", "User data cleared: name, email removed.")
    }
}
