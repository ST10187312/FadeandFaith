package com.example.fadeandfaith

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class StaffAppointments : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var appointmentsContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_staff_appointments)

        db = DatabaseHelper(this)

        appointmentsContainer = findViewById(R.id.appointmentsContainer)

        findViewById<Button>(R.id.backButton).setOnClickListener {
            finish()
        }

        loadAppointments()
    }

    private fun loadAppointments() {
        appointmentsContainer.removeAllViews()

        val cursor = db.getStaffBookings()

        if (!cursor.moveToFirst()) {
            val emptyText = TextView(this)
            emptyText.text = "No appointments found."
            emptyText.textSize = 16f
            emptyText.setTextColor(getColor(android.R.color.white))
            emptyText.setPadding(20, 30, 20, 30)

            appointmentsContainer.addView(emptyText)
            cursor.close()
            return
        }

        do {
            val bookingId =
                cursor.getLong(cursor.getColumnIndexOrThrow("booking_id"))

            val customerName =
                cursor.getString(cursor.getColumnIndexOrThrow("customer_name"))

            val serviceName =
                cursor.getString(cursor.getColumnIndexOrThrow("service_name"))

            val barberName =
                cursor.getString(cursor.getColumnIndexOrThrow("barber_name"))

            val date =
                cursor.getString(cursor.getColumnIndexOrThrow("booking_date"))

            val time =
                cursor.getString(cursor.getColumnIndexOrThrow("booking_time"))

            val price =
                cursor.getDouble(cursor.getColumnIndexOrThrow("price"))

            val status =
                cursor.getString(cursor.getColumnIndexOrThrow("status"))

            val card = LinearLayout(this)
            card.orientation = LinearLayout.VERTICAL
            card.setPadding(24, 24, 24, 24)
            card.setBackgroundResource(R.drawable.homecard)

            val details = TextView(this)

            details.text =
                "Booking #$bookingId\n\n" +
                        "Customer: $customerName\n" +
                        "Service: $serviceName\n" +
                        "Barber: $barberName\n" +
                        "Date: $date\n" +
                        "Time: $time\n" +
                        "Price: R${String.format("%.2f", price)}\n" +
                        "Status: $status"

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
                updateStatus(bookingId, "CONFIRMED")
            }

            completeButton.setOnClickListener {
                updateStatus(bookingId, "COMPLETED")
            }

            cancelButton.setOnClickListener {
                updateStatus(bookingId, "CANCELLED")
            }

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            params.setMargins(0, 0, 0, 20)

            appointmentsContainer.addView(card, params)

        } while (cursor.moveToNext())

        cursor.close()
    }

    private fun updateStatus(bookingId: Long, status: String) {

        val updated = db.updateBookingStatusByStaff(
            bookingId,
            status
        )

        if (updated) {
            Toast.makeText(
                this,
                "Appointment updated",
                Toast.LENGTH_SHORT
            ).show()

            loadAppointments()
        } else {
            Toast.makeText(
                this,
                "Could not update appointment",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}