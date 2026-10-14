package com.example.fadeandfaith

import android.content.Context

class SessionManager(context: Context) {

    private val preferences =
        context.getSharedPreferences(
            "fadefaith_session",
            Context.MODE_PRIVATE
        )

    fun login(uid: String) {
        preferences.edit()
            .putString("user_id", uid)
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

    fun getUserId(): String? {
        return preferences.getString("user_id", null)
    }

    fun setStaffBarberId(barberId: String?) {
        preferences.edit()
            .putString("staff_barber_id", barberId)
            .apply()
    }

    fun getStaffBarberId(): String? {
        return preferences.getString("staff_barber_id", null)
    }

    fun setStaffRole(role: String?) {
        preferences.edit()
            .putString("staff_role", role)
            .apply()
    }

    fun getStaffRole(): String? {
        return preferences.getString("staff_role", null)
    }

    fun isBoss(): Boolean {
        return getStaffRole().equals("BOSS", ignoreCase = true)
    }
    fun setDarkModeEnabled(enabled: Boolean) {
        preferences.edit()
            .putBoolean("dark_mode_enabled", enabled)
            .apply()
    }

    fun isDarkModeEnabled(): Boolean {
        // Defaults to true since that's been the app's only look until now.
        return preferences.getBoolean("dark_mode_enabled", true)
    }
    fun setLanguage(languageCode: String) {
        preferences.edit()
            .putString("language_code", languageCode)
            .apply()
    }

    fun getLanguage(): String {
        return preferences.getString("language_code", "en") ?: "en"
    }
}