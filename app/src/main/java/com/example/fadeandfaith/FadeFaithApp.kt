package com.example.fadeandfaith

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class FadeFaithApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val darkModeEnabled = SessionManager(this).isDarkModeEnabled()

        AppCompatDelegate.setDefaultNightMode(
            if (darkModeEnabled) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }
}