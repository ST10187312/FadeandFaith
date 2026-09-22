package com.example.fadeandfaith

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class StaffDashboard : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper

    private lateinit var staffNameText: TextView
    private lateinit var appointmentsText: TextView

    private lateinit var appointmentsButton: LinearLayout
    private lateinit var availabilityButton: LinearLayout
    private lateinit var pricesButton: LinearLayout
    private lateinit var settingsButton: LinearLayout
    private lateinit var logoutButton: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_staff_dasboard
        )

        requestNotificationPermissionIfNeeded()

        databaseHelper =
            DatabaseHelper(this)

        initializeViews()
        loadDashboard()
        setupButtons()
    }


    private fun requestNotificationPermissionIfNeeded() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
    }

    private fun initializeViews() {

        staffNameText =
            findViewById(
                R.id.staffNameText
            )

        appointmentsText =
            findViewById(
                R.id.appointmentsText
            )

        appointmentsButton =
            findViewById(
                R.id.appointmentsButton
            )

        availabilityButton =
            findViewById(
                R.id.availabilityButton
            )

        pricesButton =
            findViewById(
                R.id.pricesButton
            )

        settingsButton =
            findViewById(
                R.id.settingsButton
            )

        logoutButton =
            findViewById(
                R.id.logoutButton
            )
    }

    private fun loadDashboard() {

        val session =
            SessionManager(this)

        val userId =
            session.getUserId()

        val cursor =
            databaseHelper.getStaffMembers()

        cursor.use {

            while (it.moveToNext()) {

                val id =
                    it.getLong(
                        it.getColumnIndexOrThrow(
                            "user_id"
                        )
                    )

                if (id == userId) {

                    staffNameText.text =
                        it.getString(
                            it.getColumnIndexOrThrow(
                                "name"
                            )
                        )

                    break
                }
            }
        }

        val bookings =
            databaseHelper.getStaffBookings()

        var count = 0

        bookings.use {

            while (it.moveToNext()) {

                val status =
                    it.getString(
                        it.getColumnIndexOrThrow(
                            "status"
                        )
                    )

                if (
                    status == "CONFIRMED" ||
                    status == "PENDING"
                ) {
                    count++
                }
            }
        }

        appointmentsText.text =
            count.toString()
    }

    private fun setupButtons() {

        appointmentsButton.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    StaffAppointments::class.java
                )
            )
        }

        availabilityButton.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    StaffAvailability::class.java
                )
            )
        }

        pricesButton.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    StaffPrices::class.java
                )
            )
        }

        settingsButton.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    Settings::class.java
                )
            )
        }

        logoutButton.setOnClickListener {

            SessionManager(this).logout()

            startActivity(
                Intent(
                    this,
                    Login::class.java
                )
            )

            finishAffinity()
        }
    }
}