package com.example.fadeandfaith

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class PaymentSuccess : AppCompatActivity() {

    private lateinit var referenceText: TextView
    private lateinit var viewBookingButton: Button

    private var bookingId: Long = -1L
    private var transactionReference: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_success)

        referenceText = findViewById(R.id.referenceText)
        viewBookingButton = findViewById(R.id.viewBookingButton)

        bookingId = intent.getLongExtra("BOOKING_ID", -1L)

        transactionReference =
            intent.getStringExtra("TRANSACTION_REFERENCE") ?: ""

        referenceText.text =
            if (transactionReference.isNotEmpty()) {
                transactionReference
            } else {
                "Payment completed"
            }

        viewBookingButton.setOnClickListener {

            if (bookingId <= 0L) {
                finish()
                return@setOnClickListener
            }

            val intent = Intent(this, Receipt::class.java)

            intent.putExtra(
                "BOOKING_ID",
                bookingId
            )

            startActivity(intent)
            finish()
        }
    }
}