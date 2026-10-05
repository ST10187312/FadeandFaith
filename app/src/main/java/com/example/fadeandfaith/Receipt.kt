package com.example.fadeandfaith

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class Receipt : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore

    private lateinit var serviceText: TextView
    private lateinit var barberText: TextView
    private lateinit var dateText: TextView
    private lateinit var timeText: TextView
    private lateinit var priceText: TextView
    private lateinit var referenceText: TextView

    private lateinit var backButton: Button
    private lateinit var doneButton: Button

    private var bookingId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receipt)

        firestore = FirebaseFirestore.getInstance()

        serviceText = findViewById(R.id.serviceText)
        barberText = findViewById(R.id.barberText)
        dateText = findViewById(R.id.dateText)
        timeText = findViewById(R.id.timeText)
        priceText = findViewById(R.id.priceText)
        referenceText = findViewById(R.id.receiptNumberText)

        backButton = findViewById(R.id.backButton)
        doneButton = findViewById(R.id.paymentButton)

        bookingId = intent.getStringExtra("BOOKING_ID") ?: ""

        if (bookingId.isEmpty()) {
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

        firestore.collection("bookings")
            .document(bookingId)
            .get()
            .addOnSuccessListener { document ->

                if (!document.exists()) {
                    Toast.makeText(
                        this,
                        "Booking could not be found.",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                    return@addOnSuccessListener
                }

                val serviceName = document.getString("serviceName") ?: ""
                val barberName = document.getString("barberName") ?: ""
                val bookingDate = document.getString("bookingDate") ?: ""
                val bookingTime = document.getString("bookingTime") ?: ""
                val price = document.getDouble("price") ?: 0.0
                val status = document.getString("status") ?: "pending"

                serviceText.text = serviceName
                barberText.text = barberName
                dateText.text = bookingDate
                timeText.text = bookingTime
                priceText.text = String.format("R%.2f", price)

                referenceText.text =
                    if (status.equals("confirmed", ignoreCase = true)) {
                        // Short, readable reference built from the booking's
                        // own document ID rather than a separate payments table.
                        "Ref: ${bookingId.take(8).uppercase()}"
                    } else {
                        "Status: ${status.replaceFirstChar { it.uppercase() }}"
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Booking could not be loaded: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
    }
}