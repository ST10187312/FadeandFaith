package com.example.fadeandfaith

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Notifications : AppCompatActivity() {

    private lateinit var backButton: ImageView

    private lateinit var appointmentNotification: LinearLayout
    private lateinit var promotionNotification: LinearLayout
    private lateinit var systemNotification: LinearLayout

    private lateinit var appointmentIndicator: ImageView
    private lateinit var promotionIndicator: ImageView
    private lateinit var systemIndicator: ImageView

    private lateinit var emptyText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_notifications)

        initializeViews()
        setupNotifications()
        setupClickListeners()
    }

    private fun initializeViews() {

        backButton = findViewById(R.id.backButton)

        appointmentNotification =
            findViewById(R.id.appointmentNotification)

        promotionNotification =
            findViewById(R.id.promotionNotification)

        systemNotification =
            findViewById(R.id.systemNotification)

        appointmentIndicator =
            findViewById(R.id.appointmentIndicator)

        promotionIndicator =
            findViewById(R.id.promotionIndicator)

        systemIndicator =
            findViewById(R.id.systemIndicator)

        emptyText =
            findViewById(R.id.emptyText)
    }

    private fun setupNotifications() {

        appointmentIndicator.alpha = 1f
        promotionIndicator.alpha = 1f
        systemIndicator.alpha = 0.25f

        emptyText.visibility = LinearLayout.GONE
    }

    private fun setupClickListeners() {

        backButton.setOnClickListener {
            finish()
        }

        appointmentNotification.setOnClickListener {
            appointmentIndicator.alpha = 0.25f
        }

        promotionNotification.setOnClickListener {
            promotionIndicator.alpha = 0.25f
        }

        systemNotification.setOnClickListener {
            systemIndicator.alpha = 0.25f
        }
    }
}