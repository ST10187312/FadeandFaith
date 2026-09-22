package com.example.fadeandfaith

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class Receipt : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper

    private lateinit var serviceText: TextView
    private lateinit var barberText: TextView
    private lateinit var dateText: TextView
    private lateinit var timeText: TextView
    private lateinit var priceText: TextView
    private lateinit var statusText: TextView
    private lateinit var referenceText: TextView

    private lateinit var backButton: Button
    private lateinit var doneButton: Button

    private var bookingId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receipt)

        databaseHelper = DatabaseHelper(this)

        serviceText = findViewById(R.id.serviceText)
        barberText = findViewById(R.id.barberText)
        dateText = findViewById(R.id.dateText)
        timeText = findViewById(R.id.timeText)
        priceText = findViewById(R.id.priceText)
        referenceText = findViewById(R.id.receiptNumberText)

        backButton = findViewById(R.id.backButton)
        doneButton = findViewById(R.id.paymentButton)

        bookingId = intent.getLongExtra("BOOKING_ID", -1L)

        if (bookingId <= 0L) {
            Toast.makeText(
                this,
                "Booking information could not be found.",
                Toast.LENGTH_LONG
            ).show()

            finish()
            return
        }

        loadReceipt()

        backButton.setOnClickListener {
            finish()
        }

        doneButton.setOnClickListener {
            val intent = Intent(this, MyBookings::class.java)
            intent.putExtra("BOOKING_ID", bookingId)
            startActivity(intent)
            finish()
        }
    }

    private fun loadReceipt() {

        val bookingCursor = databaseHelper.getBooking(bookingId)

        if (bookingCursor == null) {
            Toast.makeText(
                this,
                "Booking could not be found.",
                Toast.LENGTH_LONG
            ).show()

            finish()
            return
        }

        bookingCursor.use { cursor ->

            if (!cursor.moveToFirst()) {
                Toast.makeText(
                    this,
                    "Booking could not be found.",
                    Toast.LENGTH_LONG
                ).show()

                finish()
                return
            }

            val serviceName =
                cursor.getString(cursor.getColumnIndexOrThrow("service_name"))

            val barberName =
                cursor.getString(cursor.getColumnIndexOrThrow("barber_name"))

            val bookingDate =
                cursor.getString(cursor.getColumnIndexOrThrow("booking_date"))

            val bookingTime =
                cursor.getString(cursor.getColumnIndexOrThrow("booking_time"))

            val price =
                cursor.getDouble(cursor.getColumnIndexOrThrow("price"))

            val status =
                cursor.getString(cursor.getColumnIndexOrThrow("status"))

            serviceText.text = serviceName
            barberText.text = barberName
            dateText.text = bookingDate
            timeText.text = bookingTime
            priceText.text = String.format("R%.2f", price)

        }

        loadPayment()
    }

    private fun loadPayment() {

        val paymentCursor =
            databaseHelper.getPaymentForBooking(bookingId)

        if (paymentCursor == null) {
            referenceText.text = "Not paid"
            return
        }

        paymentCursor.use { cursor ->

            if (!cursor.moveToFirst()) {
                referenceText.text = "Not paid"
                return
            }

            val reference =
                cursor.getString(
                    cursor.getColumnIndexOrThrow("transaction_reference")
                )

            val paymentStatus =
                cursor.getString(
                    cursor.getColumnIndexOrThrow("payment_status")
                )

            referenceText.text =
                if (paymentStatus.equals("PAID", ignoreCase = true)) {
                    reference
                } else {
                    "Payment not completed"
                }
        }
    }
}