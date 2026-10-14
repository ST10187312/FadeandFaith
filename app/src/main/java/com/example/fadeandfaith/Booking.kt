package com.example.fadeandfaith

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Booking : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var sessionManager: SessionManager

    private lateinit var backButton: ImageView

    private lateinit var serviceSelection: LinearLayout
    private lateinit var barberSelection: LinearLayout

    private lateinit var selectedServiceText: TextView
    private lateinit var selectedBarberText: TextView
    private lateinit var selectedDateText: TextView
    private lateinit var selectedTimeText: TextView
    private lateinit var priceText: TextView

    private lateinit var dateButton: com.google.android.material.button.MaterialButton
    private lateinit var timeButton: com.google.android.material.button.MaterialButton
    private lateinit var continueButton: com.google.android.material.button.MaterialButton

    private var selectedServiceId: String = ""
    private var selectedBarberId: String = ""

    private var selectedServiceName = ""
    private var selectedBarberName = ""

    private var selectedDate = ""
    private var selectedTime = ""

    private var selectedServicePrice = 0.0
    private var selectedServiceDuration = 0

    companion object {
        private const val SERVICE_REQUEST = 100
        private const val BARBER_REQUEST = 200
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_booking)

        firestore = FirebaseFirestore.getInstance()
        sessionManager = SessionManager(this)

        initializeViews()
        setupListeners()
        applyPreselection()
    }

    private fun initializeViews() {

        backButton = findViewById(R.id.backButton)

        serviceSelection = findViewById(R.id.serviceSelection)
        barberSelection = findViewById(R.id.barberSelection)

        selectedServiceText = findViewById(R.id.selectedServiceText)
        selectedBarberText = findViewById(R.id.selectedBarberText)
        selectedDateText = findViewById(R.id.selectedDateText)
        selectedTimeText = findViewById(R.id.selectedTimeText)
        priceText = findViewById(R.id.priceText)

        dateButton = findViewById(R.id.dateButton)
        timeButton = findViewById(R.id.timeButton)
        continueButton = findViewById(R.id.continueButton)
    }

    private fun setupListeners() {

        backButton.setOnClickListener {
            finish()
        }

        serviceSelection.setOnClickListener {
            openServices()
        }

        barberSelection.setOnClickListener {
            openBarbers()
        }

        dateButton.setOnClickListener {
            chooseDate()
        }

        timeButton.setOnClickListener {
            chooseTime()
        }

        continueButton.setOnClickListener {
            continueToConfirmation()
        }
    }

    private fun openServices() {

        val intent = Intent(this, Services::class.java)
        intent.putExtra("SELECTION_MODE", true)

        startActivityForResult(intent, SERVICE_REQUEST)
    }

    private fun openBarbers() {

        val intent = Intent(this, Barbers::class.java)
        intent.putExtra("SELECTION_MODE", true)

        startActivityForResult(intent, BARBER_REQUEST)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != RESULT_OK || data == null) {
            return
        }

        when (requestCode) {

            SERVICE_REQUEST -> {

                applyService(
                    data.getStringExtra("SERVICE_ID") ?: "",
                    data.getStringExtra("SERVICE_NAME") ?: "",
                    data.getDoubleExtra("SERVICE_PRICE_VALUE", 0.0)
                )
            }

            BARBER_REQUEST -> {

                applyBarber(
                    data.getStringExtra("BARBER_ID") ?: "",
                    data.getStringExtra("BARBER_NAME") ?: ""
                )
            }
        }
    }

    /**
     * If the user came here from the Services or Barbers screen by tapping
     * "Book", that screen sends the choice along so it is already filled in.
     */
    private fun applyPreselection() {

        val serviceId = intent.getStringExtra("SERVICE_ID") ?: ""
        if (serviceId.isNotEmpty()) {
            applyService(
                serviceId,
                intent.getStringExtra("SERVICE_NAME") ?: "",
                intent.getDoubleExtra("SERVICE_PRICE_VALUE", 0.0)
            )
        }

        val barberId = intent.getStringExtra("BARBER_ID") ?: ""
        if (barberId.isNotEmpty()) {
            applyBarber(
                barberId,
                intent.getStringExtra("BARBER_NAME") ?: ""
            )
        }
    }

    private fun applyService(id: String, name: String, price: Double) {

        selectedServiceId = id
        selectedServiceName = name
        selectedServicePrice = price

        selectedServiceText.text = selectedServiceName

        priceText.text =
            String.format(Locale.getDefault(), "R%.0f", selectedServicePrice)

        selectedTime = ""
        selectedTimeText.text = "Select time"
        selectedServiceDuration = 0

        if (selectedServiceId.isNotEmpty()) {
            firestore.collection("services")
                .document(selectedServiceId)
                .get()
                .addOnSuccessListener { document ->
                    selectedServiceDuration =
                        document.getLong("duration")?.toInt() ?: 0
                }
        }
    }

    private fun applyBarber(id: String, name: String) {

        selectedBarberId = id
        selectedBarberName = name

        selectedBarberText.text = selectedBarberName

        selectedTime = ""
        selectedTimeText.text = "Select time"
    }

    private fun chooseDate() {

        val calendar = Calendar.getInstance()

        val datePicker = DatePickerDialog(
            this,
            { _, year, month, day ->

                val chosenDate = Calendar.getInstance()
                chosenDate.set(year, month, day)

                val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                selectedDate = formatter.format(chosenDate.time)
                selectedDateText.text = selectedDate

                selectedTime = ""
                selectedTimeText.text = "Select time"
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePicker.datePicker.minDate = System.currentTimeMillis()
        datePicker.show()
    }

    private fun chooseTime() {

        if (selectedServiceId.isEmpty()) {
            Toast.makeText(this, "Please select a service first.", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedBarberId.isEmpty()) {
            Toast.makeText(this, "Please select a barber first.", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "Please select a date first.", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedServiceDuration <= 0) {
            Toast.makeText(
                this,
                "Service duration could not be found. Try reselecting the service.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        timeButton.isEnabled = false

        firestore.collection("availability")
            .whereEqualTo("barberId", selectedBarberId)
            .whereEqualTo("date", selectedDate)
            .whereEqualTo("availability", true)
            .get()
            .addOnSuccessListener { availabilitySnapshot ->

                if (availabilitySnapshot.isEmpty) {
                    timeButton.isEnabled = true
                    Toast.makeText(
                        this,
                        "No available times for this barber on $selectedDate.",
                        Toast.LENGTH_LONG
                    ).show()
                    return@addOnSuccessListener
                }

                getTakenTimesForDay(selectedBarberId, selectedDate) { takenTimes ->

                    timeButton.isEnabled = true

                    val slots = mutableListOf<String>()

                    for (doc in availabilitySnapshot.documents) {

                        val startMinutes = doc.getLong("startTime")?.toInt() ?: continue
                        val endMinutes = doc.getLong("endTime")?.toInt() ?: continue

                        var currentMinutes = startMinutes

                        while (currentMinutes + selectedServiceDuration <= endMinutes) {

                            val timeString = minutesToTimeString(currentMinutes)

                            if (timeString !in takenTimes) {
                                slots.add(timeString)
                            }

                            currentMinutes += selectedServiceDuration
                        }
                    }

                    if (slots.isEmpty()) {
                        Toast.makeText(
                            this,
                            "No available times for this barber on $selectedDate.",
                            Toast.LENGTH_LONG
                        ).show()
                        return@getTakenTimesForDay
                    }

                    val timeArray = slots.toTypedArray()

                    AlertDialog.Builder(this)
                        .setTitle("Select Time")
                        .setItems(timeArray) { _, which ->
                            selectedTime = timeArray[which]
                            selectedTimeText.text = selectedTime
                        }
                        .show()
                }
            }
            .addOnFailureListener { e ->
                timeButton.isEnabled = true
                Toast.makeText(
                    this,
                    "Could not load availability: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    /**
     * Fetches every non-cancelled booking for this barber on this date,
     * and returns the set of times already taken.
     */
    private fun getTakenTimesForDay(
        barberId: String,
        date: String,
        onResult: (Set<String>) -> Unit
    ) {
        firestore.collection("bookings")
            .whereEqualTo("barberId", barberId)
            .whereEqualTo("bookingDate", date)
            .get()
            .addOnSuccessListener { snapshot ->

                val taken = snapshot.documents
                    .filter { doc ->
                        val status = doc.getString("status") ?: ""
                        !status.equals("cancelled", ignoreCase = true)
                    }
                    .mapNotNull { it.getString("bookingTime") }
                    .toSet()

                onResult(taken)
            }
            .addOnFailureListener {
                // If this fails, fail open for the listing -- the final
                // check in continueToConfirmation() still protects against
                // an actual double-booking.
                onResult(emptySet())
            }
    }

    /**
     * One last check, right before confirming, that nobody else grabbed
     * this exact slot while the user was looking at the screen.
     */
    private fun isSlotTaken(
        barberId: String,
        date: String,
        time: String,
        onResult: (Boolean) -> Unit
    ) {
        firestore.collection("bookings")
            .whereEqualTo("barberId", barberId)
            .whereEqualTo("bookingDate", date)
            .whereEqualTo("bookingTime", time)
            .get()
            .addOnSuccessListener { snapshot ->

                val taken = snapshot.documents.any { doc ->
                    val status = doc.getString("status") ?: ""
                    !status.equals("cancelled", ignoreCase = true)
                }

                onResult(taken)
            }
            .addOnFailureListener {
                // If we can't verify, don't risk a double-booking.
                onResult(true)
            }
    }

    private fun minutesToTimeString(totalMinutes: Int): String {
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return String.format(Locale.getDefault(), "%02d:%02d", hours, minutes)
    }

    private fun continueToConfirmation() {

        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Please log in before booking.", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, Login::class.java))
            return
        }

        if (selectedServiceId.isEmpty()) {
            Toast.makeText(this, "Please select a service.", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedBarberId.isEmpty()) {
            Toast.makeText(this, "Please select a barber.", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "Please select a date.", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedTime.isEmpty()) {
            Toast.makeText(this, "Please select a time.", Toast.LENGTH_SHORT).show()
            return
        }

        continueButton.isEnabled = false

        isSlotTaken(selectedBarberId, selectedDate, selectedTime) { taken ->

            continueButton.isEnabled = true

            if (taken) {
                Toast.makeText(
                    this,
                    "That time is no longer available. Please select another time.",
                    Toast.LENGTH_LONG
                ).show()

                selectedTime = ""
                selectedTimeText.text = "Select time"
                return@isSlotTaken
            }

            val intent = Intent(this, BookingConfirmation::class.java)

            intent.putExtra("SERVICE_ID", selectedServiceId)
            intent.putExtra("BARBER_ID", selectedBarberId)
            intent.putExtra("SERVICE_NAME", selectedServiceName)
            intent.putExtra("SERVICE_PRICE_VALUE", selectedServicePrice)

            intent.putExtra(
                "SERVICE_PRICE",
                String.format(Locale.getDefault(), "R%.2f", selectedServicePrice)
            )

            intent.putExtra("BARBER_NAME", selectedBarberName)
            intent.putExtra("BOOKING_DATE", selectedDate)
            intent.putExtra("BOOKING_TIME", selectedTime)

            startActivity(intent)
        }
    }
}