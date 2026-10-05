package com.example.fadeandfaith

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class StaffAppointments : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var sessionManager: SessionManager
    private lateinit var appointmentsContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_staff_appointments)

        firestore = FirebaseFirestore.getInstance()
        sessionManager = SessionManager(this)

        appointmentsContainer = findViewById(R.id.appointmentsContainer)

        findViewById<Button>(R.id.backButton).setOnClickListener {
            finish()
        }

        loadAppointments()
    }

    private fun loadAppointments() {

        appointmentsContainer.removeAllViews()

        var query: Query = firestore.collection("bookings")

        if (!sessionManager.isBoss()) {

            val barberId = sessionManager.getStaffBarberId()

            if (barberId == null) {
                showEmptyMessage()
                return
            }

            query = query.whereEqualTo("barberId", barberId)
        }

        query.get()
            .addOnSuccessListener { snapshot ->

                if (snapshot.isEmpty) {
                    showEmptyMessage()
                    return@addOnSuccessListener
                }

                val documents = snapshot.documents.sortedByDescending {
                    it.getTimestamp("createdAt")
                }

                for (doc in documents) {
                    addAppointmentCard(doc)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Could not load appointments: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun showEmptyMessage() {

        val emptyText = TextView(this)
        emptyText.text = "No appointments found."
        emptyText.textSize = 16f
        emptyText.setTextColor(getColor(android.R.color.white))
        emptyText.setPadding(20, 30, 20, 30)

        appointmentsContainer.addView(emptyText)
    }

    private fun addAppointmentCard(document: DocumentSnapshot) {

        val bookingId = document.id
        val customerName = document.getString("customerName") ?: "Customer"
        val serviceName = document.getString("serviceName") ?: ""
        val barberName = document.getString("barberName") ?: ""
        val date = document.getString("bookingDate") ?: ""
        val time = document.getString("bookingTime") ?: ""
        val price = document.getDouble("price") ?: 0.0
        val status = document.getString("status") ?: "pending"

        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.setPadding(24, 24, 24, 24)
        card.setBackgroundResource(R.drawable.homecard)

        val details = TextView(this)

        details.text =
            "Customer: $customerName\n" +
                    "Service: $serviceName\n" +
                    "Barber: $barberName\n" +
                    "Date: $date\n" +
                    "Time: $time\n" +
                    "Price: R${String.format("%.2f", price)}\n" +
                    "Status: ${status.uppercase()}"

        details.textSize = 15f
        details.setTextColor(getColor(android.R.color.white))

        card.addView(details)

        val buttonRow = LinearLayout(this)
        buttonRow.orientation = LinearLayout.HORIZONTAL

        val confirmButton = Button(this)
        confirmButton.text = "Confirm"

        val completeButton = Button(this)
        completeButton.text = "Complete"

        val cancelButton = Button(this)
        cancelButton.text = "Cancel"

        buttonRow.addView(confirmButton)
        buttonRow.addView(completeButton)
        buttonRow.addView(cancelButton)

        card.addView(buttonRow)

        confirmButton.setOnClickListener {
            updateStatus(bookingId, "confirmed", document)
        }

        completeButton.setOnClickListener {
            updateStatus(bookingId, "completed", document)
        }

        cancelButton.setOnClickListener {
            updateStatus(bookingId, "cancelled", document)
        }

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        params.setMargins(0, 0, 0, 20)

        appointmentsContainer.addView(card, params)
    }

    private fun updateStatus(bookingId: String, status: String, document: DocumentSnapshot) {

        firestore.collection("bookings")
            .document(bookingId)
            .update("status", status)
            .addOnSuccessListener {

                Toast.makeText(this, "Appointment updated", Toast.LENGTH_SHORT).show()

                val customerId = document.getString("userId")
                val barberName = document.getString("barberName") ?: "your barber"

                if (customerId != null) {

                    val customerNotification = hashMapOf(
                        "audience" to "customer",
                        "userId" to customerId,
                        "title" to "Booking ${status.replaceFirstChar { it.uppercase() }}",
                        "message" to "Your appointment with $barberName has been $status",
                        "type" to "booking",
                        "isRead" to false,
                        "createdAt" to FieldValue.serverTimestamp()
                    )

                    firestore.collection("notifications").add(customerNotification)
                }

                loadAppointments()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Could not update appointment: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}