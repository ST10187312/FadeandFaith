package com.example.fadeandfaith

import android.content.Context

class SessionManager(context: Context) {

    private val preferences =
        context.getSharedPreferences(
            "fadefaith_session",
            Context.MODE_PRIVATE
        )

    fun login(userId: Long) {
        preferences.edit()
            .putLong("user_id", userId)
            .putBoolean("logged_in", true)
            .apply()
    }

    fun logout() {
        preferences.edit()
            .clear()
            .apply()
    }

    fun isLoggedIn(): Boolean {
        return preferences.getBoolean("logged_in", false)
    }

    fun getUserId(): Long {
        return preferences.getLong("user_id", -1L)
    }
}