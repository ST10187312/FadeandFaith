package com.example.fadeandfaith

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class Home : AppCompatActivity() {

    private lateinit var notificationButton: ImageView
    private lateinit var profileButton: ImageView
    //private lateinit var bookNowButton: MaterialButton
    private lateinit var seeServicesButton: TextView
    private lateinit var seeBarbersButton: TextView

    private lateinit var navHome: LinearLayout
    private lateinit var navServices: LinearLayout
    private lateinit var navBooking: LinearLayout
    private lateinit var navProfile: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_home)

        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {

        notificationButton =
            findViewById(R.id.notificationButton)

        profileButton =
            findViewById(R.id.profileButton)

        // bookNowButton =
        //  findViewById(R.id.bookNowButton)

        seeServicesButton =
            findViewById(R.id.seeServicesButton)

        seeBarbersButton =
            findViewById(R.id.seeBarbersButton)

        navHome =
            findViewById(R.id.navHome)

        navServices =
            findViewById(R.id.navServices)

        navBooking =
            findViewById(R.id.navBooking)

        navProfile =
            findViewById(R.id.navProfile)
    }

    private fun setupClickListeners() {

        // NOTIFICATIONS
        notificationButton.setOnClickListener {

            Toast.makeText(
                this,
                "Notifications coming soon.",
                Toast.LENGTH_SHORT
            ).show()
        }

        // PROFILE ICON
        profileButton.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    Profile::class.java
                )
            )
        }



        // SERVICES
        seeServicesButton.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    Services::class.java
                )
            )
        }

        // BARBERS
        seeBarbersButton.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    Barbers::class.java
                )
            )
        }

        // BOTTOM NAVIGATION - HOME
        navHome.setOnClickListener {

            // Already on Home
        }

        // BOTTOM NAVIGATION - SERVICES
        navServices.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    Services::class.java
                )
            )
        }

        // BOTTOM NAVIGATION - BOOKING
        navBooking.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    Booking::class.java
                )
            )
        }

        // BOTTOM NAVIGATION - PROFILE
        navProfile.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    Profile::class.java
                )
            )
        }
    }
}