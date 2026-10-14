package com.example.fadeandfaith

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class BookingConfirmation : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var sessionManager: SessionManager

    private lateinit var backButton: ImageView
    private lateinit var serviceText: TextView
    private lateinit var priceText: TextView
    private lateinit var barberText: TextView
    private lateinit var dateText: TextView
    private lateinit var timeText: TextView
    private lateinit var confirmButton: MaterialButton
    private lateinit var changeButton: TextView

    private var serviceId: String = ""
    private var barberId: String = ""
    private var servicePrice = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_booking_confirmation)

        firestore = FirebaseFirestore.getInstance()
        sessionManager = SessionManager(this)

        initializeViews()
        loadBookingDetails()
        setupClickListeners()
    }

    private fun initializeViews() {

        backButton = findViewById(R.id.backButton)
        serviceText = findViewById(R.id.serviceText)
        priceText = findViewById(R.id.priceText)
        barberText = findViewById(R.id.barberText)
        dateText = findViewById(R.id.dateText)
        timeText = findViewById(R.id.timeText)
        confirmButton = findViewById(R.id.confirmButton)
        changeButton = findViewById(R.id.changeButton)
    }

    private fun loadBookingDetails() {

        serviceId = intent.getStringExtra("SERVICE_ID") ?: ""
        barberId = intent.getStringExtra("BARBER_ID") ?: ""
        servicePrice = intent.getDoubleExtra("SERVICE_PRICE_VALUE", 0.0)

        serviceText.text = intent.getStringExtra("SERVICE_NAME") ?: ""
        priceText.text = intent.getStringExtra("SERVICE_PRICE") ?: ""
        barberText.text = intent.getStringExtra("BARBER_NAME") ?: ""
        dateText.text = intent.getStringExtra("BOOKING_DATE") ?: ""
        timeText.text = intent.getStringExtra("BOOKING_TIME") ?: ""
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

        if (userId == null) {
            Toast.makeText(this, "Please log in again.", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, Login::class.java))
            finish()
            return
        }

        if (serviceId.isEmpty()) {
            Toast.makeText(this, "Invalid service.", Toast.LENGTH_LONG).show()
            return
        }

        if (barberId.isEmpty()) {
            Toast.makeText(this, "Invalid barber.", Toast.LENGTH_LONG).show()
            return
        }

        val bookingDate = dateText.text.toString().trim()
        val bookingTime = timeText.text.toString().trim()

        if (bookingDate.isEmpty() || bookingTime.isEmpty()) {
            Toast.makeText(this, "Please select a date and time.", Toast.LENGTH_LONG).show()
            return
        }

        if (servicePrice <= 0) {
            Toast.makeText(this, "Invalid service price.", Toast.LENGTH_LONG).show()
            return
        }

        confirmButton.isEnabled = false

        // Fetch the customer's name first, so it can be saved directly on
        // the booking document -- avoids a second lookup elsewhere.
        firestore.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { userDoc ->

                val customerName = userDoc.getString("name") ?: "Customer"

                // Last-moment check: make sure nobody grabbed this exact
                // slot while this screen was open.
                firestore.collection("bookings")
                    .whereEqualTo("barberId", barberId)
                    .whereEqualTo("bookingDate", bookingDate)
                    .whereEqualTo("bookingTime", bookingTime)
                    .get()
                    .addOnSuccessListener { snapshot ->

                        val alreadyTaken = snapshot.documents.any { doc ->
                            val status = doc.getString("status") ?: ""
                            !status.equals("cancelled", ignoreCase = true)
                        }

                        if (alreadyTaken) {
                            confirmButton.isEnabled = true
                            Toast.makeText(
                                this,
                                "That time is no longer available. Please choose another time.",
                                Toast.LENGTH_LONG
                            ).show()
                            finish()
                            return@addOnSuccessListener
                        }

                        val bookingData = hashMapOf(
                            "userId" to userId,
                            "customerName" to customerName,
                            "barberId" to barberId,
                            "serviceId" to serviceId,
                            "serviceName" to serviceText.text.toString(),
                            "barberName" to barberText.text.toString(),
                            "bookingDate" to bookingDate,
                            "bookingTime" to bookingTime,
                            "price" to servicePrice,
                            "paymentMethod" to getSharedPreferences("ff_prefs", MODE_PRIVATE)
                                .getString("payment_method", "Cash at the barbershop"),
                            "status" to "pending",
                            "createdAt" to FieldValue.serverTimestamp()
                        )

                        firestore.collection("bookings")
                            .add(bookingData)
                            .addOnSuccessListener { docRef ->

                                Toast.makeText(
                                    this,
                                    "Booking created successfully.",
                                    Toast.LENGTH_SHORT
                                ).show()

                                // Tell the assigned barber's staff device, in
                                // real time, instead of calling a Cloud
                                // Function.
                                val staffNotification = hashMapOf(
                                    "audience" to "staff",
                                    "barberId" to barberId,
                                    "title" to "New booking",
                                    "message" to "$customerName booked ${serviceText.text} on $bookingDate at $bookingTime",
                                    "type" to "booking",
                                    "isRead" to false,
                                    "createdAt" to FieldValue.serverTimestamp()
                                )

                                firestore.collection("notifications").add(staffNotification)

                                val resultIntent = Intent(this, MyBookings::class.java)
                                resultIntent.putExtra("BOOKING_ID", docRef.id)
                                resultIntent.flags =
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

                                startActivity(resultIntent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                confirmButton.isEnabled = true
                                Toast.makeText(
                                    this,
                                    "The booking could not be created: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }
                    .addOnFailureListener { e ->
                        confirmButton.isEnabled = true
                        Toast.makeText(
                            this,
                            "Could not verify availability: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
            .addOnFailureListener { e ->
                confirmButton.isEnabled = true
                Toast.makeText(
                    this,
                    "Could not load your profile: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}