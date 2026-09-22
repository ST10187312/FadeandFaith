package com.example.fadeandfaith

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class BookingConfirmation : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager

    private lateinit var backButton: ImageView
    private lateinit var serviceText: TextView
    private lateinit var priceText: TextView
    private lateinit var barberText: TextView
    private lateinit var dateText: TextView
    private lateinit var timeText: TextView
    private lateinit var confirmButton: MaterialButton
    private lateinit var changeButton: TextView

    private var serviceId: Long = -1L
    private var barberId: Long = -1L
    private var servicePrice = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_booking_confirmation
        )

        databaseHelper =
            DatabaseHelper(this)

        sessionManager =
            SessionManager(this)

        initializeViews()
        loadBookingDetails()
        setupClickListeners()
    }

    private fun initializeViews() {

        backButton =
            findViewById(R.id.backButton)

        serviceText =
            findViewById(R.id.serviceText)

        priceText =
            findViewById(R.id.priceText)

        barberText =
            findViewById(R.id.barberText)

        dateText =
            findViewById(R.id.dateText)

        timeText =
            findViewById(R.id.timeText)

        confirmButton =
            findViewById(R.id.confirmButton)

        changeButton =
            findViewById(R.id.changeButton)
    }

    private fun loadBookingDetails() {

        serviceId =
            intent.getLongExtra(
                "SERVICE_ID",
                -1L
            )

        barberId =
            intent.getLongExtra(
                "BARBER_ID",
                -1L
            )

        servicePrice =
            intent.getDoubleExtra(
                "SERVICE_PRICE_VALUE",
                0.0
            )

        serviceText.text =
            intent.getStringExtra(
                "SERVICE_NAME"
            ) ?: ""

        priceText.text =
            intent.getStringExtra(
                "SERVICE_PRICE"
            ) ?: ""

        barberText.text =
            intent.getStringExtra(
                "BARBER_NAME"
            ) ?: ""

        dateText.text =
            intent.getStringExtra(
                "BOOKING_DATE"
            ) ?: ""

        timeText.text =
            intent.getStringExtra(
                "BOOKING_TIME"
            ) ?: ""
    }

    private fun setupClickListeners() {

        backButton.setOnClickListener {
            finish()
        }

        changeButton.setOnClickListener {
            finish()
        }

        confirmButton.setOnClickListener {
            createBooking()
        }
    }

    private fun createBooking() {

        val userId = sessionManager.getUserId()

        if (userId <= 0) {

            Toast.makeText(
                this,
                "Please log in again.",
                Toast.LENGTH_LONG
            ).show()

            startActivity(
                Intent(
                    this,
                    Login::class.java
                )
            )

            finish()
            return
        }

        if (serviceId <= 0) {

            Toast.makeText(
                this,
                "Invalid service.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        if (barberId <= 0) {

            Toast.makeText(
                this,
                "Invalid barber.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        val bookingDate =
            dateText.text.toString().trim()

        val bookingTime =
            timeText.text.toString().trim()

        if (
            bookingDate.isEmpty() ||
            bookingTime.isEmpty()
        ) {

            Toast.makeText(
                this,
                "Please select a date and time.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        if (servicePrice <= 0) {

            Toast.makeText(
                this,
                "Invalid service price.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        try {

            val stillAvailable =
                databaseHelper.canBookService(
                    serviceId = serviceId,
                    barberId = barberId,
                    date = bookingDate,
                    time = bookingTime
                )

            if (!stillAvailable) {

                Toast.makeText(
                    this,
                    "That time is no longer available. Please choose another time.",
                    Toast.LENGTH_LONG
                ).show()

                finish()
                return
            }

            val bookingId =
                databaseHelper.createBooking(
                    userId = userId,
                    serviceId = serviceId,
                    barberId = barberId,
                    bookingDate = bookingDate,
                    bookingTime = bookingTime,
                    price = servicePrice
                )

            if (bookingId <= 0) {

                Toast.makeText(
                    this,
                    "The booking could not be created.",
                    Toast.LENGTH_LONG
                ).show()

                return
            }

            Toast.makeText(
                this,
                "Booking created successfully.",
                Toast.LENGTH_SHORT
            ).show()

            notifyStaffOfNewBooking(
                userId = userId,
                bookingDate = bookingDate,
                bookingTime = bookingTime
            )

            val intent =
                Intent(
                    this,
                    MyBookings::class.java
                )

            intent.putExtra(
                "BOOKING_ID",
                bookingId
            )

            intent.flags =
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP

            startActivity(intent)

            finish()

        } catch (e: Exception) {

            Toast.makeText(
                this,
                "Could not save booking. Please try again.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun notifyStaffOfNewBooking(
        userId: Long,
        bookingDate: String,
        bookingTime: String
    ) {
        val customerName =
            databaseHelper.getUserName(userId) ?: "A customer"

        com.google.firebase.functions.FirebaseFunctions.getInstance()
            .getHttpsCallable("notifyStaffOfBooking")
            .call(
                hashMapOf(
                    "customerName" to customerName,
                    "serviceName" to serviceText.text.toString(),
                    "bookingDate" to bookingDate,
                    "bookingTime" to bookingTime
                )
            )
    }
}