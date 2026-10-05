package com.example.fadeandfaith

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class Settings : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    private lateinit var backButton: ImageView

    private lateinit var notificationOption: LinearLayout
    private lateinit var languageOption: LinearLayout
    private lateinit var aboutOption: LinearLayout
    private lateinit var privacyOption: LinearLayout
    private lateinit var termsOption: LinearLayout

    private lateinit var darkModeSwitch: Switch
    private lateinit var notificationSwitch: Switch

    private lateinit var navHome: LinearLayout
    private lateinit var navServices: LinearLayout
    private lateinit var navBooking: LinearLayout
    private lateinit var navProfile: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_settings)

        sessionManager = SessionManager(this)

        initializeViews()
        setupSwitches()
        setupClickListeners()
    }

    private fun initializeViews() {

        backButton = findViewById(R.id.backButton)

        notificationOption = findViewById(R.id.notificationOption)
        languageOption = findViewById(R.id.languageOption)
        aboutOption = findViewById(R.id.aboutOption)
        privacyOption = findViewById(R.id.privacyOption)
        termsOption = findViewById(R.id.termsOption)

        darkModeSwitch = findViewById(R.id.darkModeSwitch)
        notificationSwitch = findViewById(R.id.notificationSwitch)

        navHome = findViewById(R.id.navHome)
        navServices = findViewById(R.id.navServices)
        navBooking = findViewById(R.id.navBooking)
        navProfile = findViewById(R.id.navProfile)
    }

    private fun setupSwitches() {

        darkModeSwitch.isChecked = sessionManager.isDarkModeEnabled()

        darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->

            sessionManager.setDarkModeEnabled(isChecked)

            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) {
                    AppCompatDelegate.MODE_NIGHT_YES
                } else {
                    AppCompatDelegate.MODE_NIGHT_NO
                }
            )

            recreate()
        }

        notificationSwitch.isChecked = true

        notificationSwitch.setOnCheckedChangeListener { _, isChecked ->

            Toast.makeText(
                this,
                if (isChecked) "Notifications enabled." else "Notifications disabled.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setupClickListeners() {

        backButton.setOnClickListener {
            finish()
        }

        notificationOption.setOnClickListener {
            startActivity(Intent(this, Notifications::class.java))
        }

        languageOption.setOnClickListener {
            startActivity(Intent(this, Language::class.java))
        }

        aboutOption.setOnClickListener {
            startActivity(Intent(this, About::class.java))
        }

        privacyOption.setOnClickListener {
            startActivity(Intent(this, Privacy::class.java))
        }

        termsOption.setOnClickListener {
            startActivity(Intent(this, Terms::class.java))
        }

        navHome.setOnClickListener {
            startActivity(Intent(this, Home::class.java))
            finish()
        }

        navServices.setOnClickListener {
            startActivity(Intent(this, Services::class.java))
            finish()
        }

        navBooking.setOnClickListener {
            startActivity(Intent(this, MyBookings::class.java))
        }

        navProfile.setOnClickListener {
            startActivity(Intent(this, Profile::class.java))
            finish()
        }
    }
}