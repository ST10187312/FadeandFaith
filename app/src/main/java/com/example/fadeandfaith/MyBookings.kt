package com.example.fadeandfaith

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MyBookings : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager

    private lateinit var backButton: ImageView

    private lateinit var upcomingTab: TextView
    private lateinit var completedTab: TextView
    private lateinit var cancelledTab: TextView

    private lateinit var upcomingContent: LinearLayout
    private lateinit var completedContent: LinearLayout
    private lateinit var cancelledContent: LinearLayout

    private lateinit var serviceText: TextView
    private lateinit var barberText: TextView
    private lateinit var dateText: TextView
    private lateinit var timeText: TextView
    private lateinit var priceText: TextView
    private lateinit var statusText: TextView

    private lateinit var cancelButton: TextView
    private lateinit var receiptButton: Button

    private var currentBookingId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_my_bookings)

        databaseHelper = DatabaseHelper(this)
        sessionManager = SessionManager(this)

        initializeViews()
        setupListeners()
    }

    private fun initializeViews() {

        backButton = findViewById(R.id.backButton)

        upcomingTab = findViewById(R.id.upcomingTab)
        completedTab = findViewById(R.id.completedTab)
        cancelledTab = findViewById(R.id.cancelledTab)

        upcomingContent = findViewById(R.id.upcomingContent)
        completedContent = findViewById(R.id.completedContent)
        cancelledContent = findViewById(R.id.cancelledContent)

        serviceText = findViewById(R.id.serviceText)
        barberText = findViewById(R.id.barberText)
        dateText = findViewById(R.id.dateText)
        timeText = findViewById(R.id.timeText)
        priceText = findViewById(R.id.priceText)
        //statusText = findViewById(R.id.statusText)

        cancelButton = findViewById(R.id.cancelButton)
        receiptButton = findViewById(R.id.receiptButton)
    }

    private fun setupListeners() {

        backButton.setOnClickListener {
            finish()
        }

        upcomingTab.setOnClickListener {
            showTab("UPCOMING")
        }

        completedTab.setOnClickListener {
            showTab("COMPLETED")
        }

        cancelledTab.setOnClickListener {
            showTab("CANCELLED")
        }

        cancelButton.setOnClickListener {
            cancelCurrentBooking()
        }

        receiptButton.setOnClickListener {

            if (currentBookingId <= 0L) {

                Toast.makeText(
                    this,
                    "No booking selected.",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            val intent =
                Intent(
                    this,
                    Receipt::class.java
                )

            intent.putExtra(
                "BOOKING_ID",
                currentBookingId
            )

            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()

        loadBookings()
    }

    private fun loadBookings() {

        val userId =
            sessionManager.getUserId()

        if (userId <= 0L) {

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

        currentBookingId = -1L

        clearBookingDisplay()

        completedContent.removeAllViews()
        cancelledContent.removeAllViews()

        var hasUpcoming = false
        var hasCompleted = false
        var hasCancelled = false

        val cursor =
            databaseHelper.getUserBookings(userId)

        cursor.use {

            if (it.moveToFirst()) {

                do {

                    val bookingId =
                        it.getLong(
                            it.getColumnIndexOrThrow(
                                "booking_id"
                            )
                        )

                    val service =
                        it.getString(
                            it.getColumnIndexOrThrow(
                                "service_name"
                            )
                        )

                    val barber =
                        it.getString(
                            it.getColumnIndexOrThrow(
                                "barber_name"
                            )
                        )

                    val date =
                        it.getString(
                            it.getColumnIndexOrThrow(
                                "booking_date"
                            )
                        )

                    val time =
                        it.getString(
                            it.getColumnIndexOrThrow(
                                "booking_time"
                            )
                        )

                    val price =
                        it.getDouble(
                            it.getColumnIndexOrThrow(
                                "price"
                            )
                        )

                    val status =
                        it.getString(
                            it.getColumnIndexOrThrow(
                                "status"
                            )
                        ).uppercase()

                    when (status) {

                        "PENDING",
                        "CONFIRMED" -> {

                            if (!hasUpcoming) {

                                displayUpcomingBooking(
                                    bookingId,
                                    service,
                                    barber,
                                    date,
                                    time,
                                    price,
                                    status
                                )

                                hasUpcoming = true
                            }
                        }

                        "COMPLETED" -> {

                            addBookingSummary(
                                bookingId,
                                service,
                                barber,
                                date,
                                time,
                                price,
                                status,
                                completedContent
                            )

                            hasCompleted = true
                        }

                        "CANCELLED" -> {

                            addBookingSummary(
                                bookingId,
                                service,
                                barber,
                                date,
                                time,
                                price,
                                status,
                                cancelledContent
                            )

                            hasCancelled = true
                        }
                    }

                } while (it.moveToNext())
            }
        }

        if (!hasUpcoming) {

            addEmptyMessage(
                upcomingContent,
                "You have no upcoming bookings."
            )
        }

        if (!hasCompleted) {

            addEmptyMessage(
                completedContent,
                "You have no completed bookings."
            )
        }

        if (!hasCancelled) {

            addEmptyMessage(
                cancelledContent,
                "You have no cancelled bookings."
            )
        }

        showTab("UPCOMING")
    }

    private fun displayUpcomingBooking(
        bookingId: Long,
        service: String,
        barber: String,
        date: String,
        time: String,
        price: Double,
        status: String
    ) {

        currentBookingId = bookingId

        serviceText.text = service
        barberText.text = barber
        dateText.text = date
        timeText.text = time

        priceText.text =
            String.format(
                "R%.2f",
                price
            )

        //statusText.text = status

        upcomingContent.visibility =
            View.VISIBLE

        cancelButton.visibility =
            if (
                status == "PENDING" ||
                status == "CONFIRMED"
            ) {
                View.VISIBLE
            } else {
                View.GONE
            }

        receiptButton.visibility =
            View.VISIBLE
    }

    private fun addBookingSummary(
        bookingId: Long,
        service: String,
        barber: String,
        date: String,
        time: String,
        price: Double,
        status: String,
        container: LinearLayout
    ) {

        val bookingLayout =
            LinearLayout(this)

        bookingLayout.orientation =
            LinearLayout.VERTICAL

        bookingLayout.setPadding(
            20,
            20,
            20,
            20
        )

        val serviceView =
            TextView(this)

        serviceView.text =
            "Service: $service"

        serviceView.textSize =
            15f

        val barberView =
            TextView(this)

        barberView.text =
            "Barber: $barber"

        barberView.textSize =
            14f

        val dateView =
            TextView(this)

        dateView.text =
            "Date: $date"

        dateView.textSize =
            14f

        val timeView =
            TextView(this)

        timeView.text =
            "Time: $time"

        timeView.textSize =
            14f

        val priceView =
            TextView(this)

        priceView.text =
            String.format(
                "Price: R%.2f",
                price
            )

        priceView.textSize =
            14f

        val statusView =
            TextView(this)

        statusView.text =
            "Status: $status"

        statusView.textSize =
            14f

        bookingLayout.addView(
            serviceView
        )

        bookingLayout.addView(
            barberView
        )

        bookingLayout.addView(
            dateView
        )

        bookingLayout.addView(
            timeView
        )

        bookingLayout.addView(
            priceView
        )

        bookingLayout.addView(
            statusView
        )

        bookingLayout.setOnClickListener {

            val intent =
                Intent(
                    this,
                    Receipt::class.java
                )

            intent.putExtra(
                "BOOKING_ID",
                bookingId
            )

            startActivity(intent)
        }

        container.addView(
            bookingLayout
        )
    }

    private fun addEmptyMessage(
        container: LinearLayout,
        message: String
    ) {

        val textView =
            TextView(this)

        textView.text =
            message

        textView.textSize =
            14f

        textView.setPadding(
            20,
            30,
            20,
            30
        )

        container.addView(
            textView
        )
    }

    private fun clearBookingDisplay() {

        serviceText.text =
            "No booking"

        barberText.text =
            "No barber"

        dateText.text =
            "No date"

        timeText.text =
            "No time"

        priceText.text =
            "R0.00"


        cancelButton.visibility =
            View.GONE

        receiptButton.visibility =
            View.GONE
    }

    private fun showTab(
        tab: String
    ) {

        upcomingContent.visibility =
            if (
                tab == "UPCOMING"
            ) {
                View.VISIBLE
            } else {
                View.GONE
            }

        completedContent.visibility =
            if (
                tab == "COMPLETED"
            ) {
                View.VISIBLE
            } else {
                View.GONE
            }

        cancelledContent.visibility =
            if (
                tab == "CANCELLED"
            ) {
                View.VISIBLE
            } else {
                View.GONE
            }
    }

    private fun cancelCurrentBooking() {

        if (currentBookingId <= 0L) {

            Toast.makeText(
                this,
                "No booking selected.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val success =
            databaseHelper.cancelBooking(
                currentBookingId
            )

        if (success) {

            Toast.makeText(
                this,
                "Booking cancelled successfully.",
                Toast.LENGTH_SHORT
            ).show()

            currentBookingId = -1L

            loadBookings()

        } else {

            Toast.makeText(
                this,
                "Unable to cancel booking.",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}