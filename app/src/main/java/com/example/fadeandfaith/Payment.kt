package com.example.fadeandfaith

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import java.util.UUID

class Payment : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper

    private lateinit var backButton: ImageView
    private lateinit var amountText: TextView
    private lateinit var payButton: MaterialButton

    private var bookingId: Long = -1L
    private var amount: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_payment
        )

        databaseHelper =
            DatabaseHelper(this)

        bookingId =
            intent.getLongExtra(
                "BOOKING_ID",
                -1L
            )

        initializeViews()
        loadPaymentDetails()
        setupClickListeners()
    }

    private fun initializeViews() {

        backButton =
            findViewById(R.id.backButton)

        amountText =
            findViewById(R.id.Amount)

        payButton =
            findViewById(R.id.payButton)
    }

    private fun loadPaymentDetails() {

        if (bookingId <= 0) {

            Toast.makeText(
                this,
                "Invalid booking.",
                Toast.LENGTH_LONG
            ).show()

            finish()
            return
        }

        val bookingAmount =
            databaseHelper.getBookingPaymentAmount(
                bookingId
            )

        if (bookingAmount == null) {

            Toast.makeText(
                this,
                "Booking could not be found.",
                Toast.LENGTH_LONG
            ).show()

            finish()
            return
        }

        amount = bookingAmount

        amountText.text =
            String.format(
                "R%.2f",
                amount
            )
    }

    private fun setupClickListeners() {

        backButton.setOnClickListener {
            finish()
        }

        payButton.setOnClickListener {
            processPayment()
        }
    }

    private fun processPayment() {

        if (bookingId <= 0) {
            return
        }

        if (amount <= 0) {

            Toast.makeText(
                this,
                "Invalid payment amount.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        payButton.isEnabled = false

        try {

            val transactionReference =
                "FF-" +
                        UUID.randomUUID()
                            .toString()
                            .replace(
                                "-",
                                ""
                            )
                            .take(12)
                            .uppercase()

            val paymentId =
                databaseHelper.createPayment(
                    bookingId = bookingId,
                    amount = amount,
                    paymentMethod = "CARD",
                    paymentStatus = "PAID",
                    transactionReference =
                        transactionReference
                )

            if (paymentId <= 0) {

                payButton.isEnabled = true

                Toast.makeText(
                    this,
                    "Payment could not be processed.",
                    Toast.LENGTH_LONG
                ).show()

                return
            }

            val bookingUpdated =
                databaseHelper.updateBookingStatus(
                    bookingId,
                    "CONFIRMED"
                )

            if (!bookingUpdated) {

                payButton.isEnabled = true

                Toast.makeText(
                    this,
                    "Payment was recorded, but the booking could not be confirmed.",
                    Toast.LENGTH_LONG
                ).show()

                return
            }

            val intent =
                Intent(
                    this,
                    PaymentSuccess::class.java
                )

            intent.putExtra(
                "BOOKING_ID",
                bookingId
            )

            intent.putExtra(
                "TRANSACTION_REFERENCE",
                transactionReference
            )

            startActivity(intent)

            finish()

        } catch (e: Exception) {

            payButton.isEnabled = true

            Toast.makeText(
                this,
                "Payment failed: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}