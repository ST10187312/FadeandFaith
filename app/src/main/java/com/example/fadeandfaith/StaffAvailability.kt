package com.example.fadeandfaith

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class StaffAvailability : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var sessionManager: SessionManager

    private lateinit var dateInput: EditText
    private lateinit var startTimeInput: EditText
    private lateinit var endTimeInput: EditText
    private lateinit var availableSwitch: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_staff_availability)

        firestore = FirebaseFirestore.getInstance()
        sessionManager = SessionManager(this)

        dateInput = findViewById(R.id.dateInput)
        startTimeInput = findViewById(R.id.startTimeInput)
        endTimeInput = findViewById(R.id.endTimeInput)
        availableSwitch = findViewById(R.id.availableSwitch)

        findViewById<Button>(R.id.backButton).setOnClickListener {
            finish()
        }

        dateInput.setOnClickListener {
            chooseDate()
        }

        startTimeInput.setOnClickListener {
            chooseTime(startTimeInput)
        }

        endTimeInput.setOnClickListener {
            chooseTime(endTimeInput)
        }

        findViewById<Button>(R.id.saveAvailabilityButton).setOnClickListener {
            saveAvailability()
        }
    }

    private fun chooseDate() {

        val calendar = Calendar.getInstance()

        val datePicker = DatePickerDialog(
            this,
            { _, year, month, day ->

                val chosenDate = Calendar.getInstance()
                chosenDate.set(year, month, day)

                val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                dateInput.setText(formatter.format(chosenDate.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePicker.datePicker.minDate = System.currentTimeMillis()
        datePicker.show()
    }

    private fun chooseTime(target: EditText) {

        val calendar = Calendar.getInstance()

        val timePicker = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                target.setText(
                    String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
                )
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )

        timePicker.show()
    }

    private fun saveAvailability() {

        val staffUserId = sessionManager.getUserId()

        if (staffUserId == null) {
            Toast.makeText(this, "Staff session not found", Toast.LENGTH_SHORT).show()
            return
        }

        val barberId = sessionManager.getStaffBarberId()

        if (barberId == null) {
            Toast.makeText(
                this,
                "Staff profile is not linked to a barber",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val date = dateInput.text.toString().trim()
        val startTime = startTimeInput.text.toString().trim()
        val endTime = endTimeInput.text.toString().trim()
        val available = availableSwitch.isChecked

        if (date.isEmpty() || startTime.isEmpty() || endTime.isEmpty()) {
            Toast.makeText(this, "Please complete all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        formatter.isLenient = false

        try {
            formatter.parse(date)
        } catch (e: Exception) {
            Toast.makeText(this, "Please select a valid date", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isValidTime(startTime) || !isValidTime(endTime)) {
            Toast.makeText(
                this,
                "Please select valid start and end times",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val startMinutes = timeToMinutes(startTime)
        val endMinutes = timeToMinutes(endTime)

        if (endMinutes <= startMinutes) {
            Toast.makeText(
                this,
                "End time must be after start time",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val availabilityData = hashMapOf(
            "barberId" to barberId,
            "date" to date,
            "startTime" to startMinutes,
            "endTime" to endMinutes,
            "availability" to available
        )

        firestore.collection("availability")
            .add(availabilityData)
            .addOnSuccessListener {
                Toast.makeText(this, "Availability saved for $date", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Could not save availability: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun isValidTime(time: String): Boolean {
        return Regex("^([01]\\d|2[0-3]):[0-5]\\d$").matches(time)
    }

    private fun timeToMinutes(time: String): Int {

        val parts = time.split(":")

        if (parts.size != 2) {
            return -1
        }

        return parts[0].toInt() * 60 + parts[1].toInt()
    }
}