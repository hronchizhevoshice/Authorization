package com.example.authorization

import android.content.Context
import android.content.SharedPreferences

object SessionManager {

    private const val PREFS_NAME = "auth_session"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_EMAIL = "email"
    private const val KEY_COMPANY = "company_name"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveUser(user: User) {
        prefs.edit()
            .putLong(KEY_USER_ID, user.id)
            .putString(KEY_EMAIL, user.email)
            .putString(KEY_COMPANY, user.companyName)
            .apply()
    }

    fun getUser(): User? {
        val id = prefs.getLong(KEY_USER_ID, -1L)
        if (id <= 0) return null
        val email = prefs.getString(KEY_EMAIL, null) ?: return null
        val company = prefs.getString(KEY_COMPANY, "") ?: ""
        return User(id = id, email = email, companyName = company)
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}