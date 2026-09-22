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
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Booking : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager

    private lateinit var backButton: ImageView

    private lateinit var serviceSelection: LinearLayout
    private lateinit var barberSelection: LinearLayout

    private lateinit var selectedServiceText: TextView
    private lateinit var selectedBarberText: TextView
    private lateinit var selectedDateText: TextView
    private lateinit var selectedTimeText: TextView
    private lateinit var priceText: TextView

    private lateinit var dateButton: MaterialButton
    private lateinit var timeButton: MaterialButton
    private lateinit var continueButton: MaterialButton

    private var selectedServiceId: Long = -1L
    private var selectedBarberId: Long = -1L

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

        databaseHelper = DatabaseHelper(this)
        sessionManager = SessionManager(this)

        initializeViews()
        setupListeners()
    }

    private fun initializeViews() {

        backButton = findViewById(R.id.backButton)

        serviceSelection = findViewById(R.id.serviceSelection)
        barberSelection = findViewById(R.id.barberSelection)

        selectedServiceText =
            findViewById(R.id.selectedServiceText)

        selectedBarberText =
            findViewById(R.id.selectedBarberText)

        selectedDateText =
            findViewById(R.id.selectedDateText)

        selectedTimeText =
            findViewById(R.id.selectedTimeText)

        priceText =
            findViewById(R.id.priceText)

        dateButton =
            findViewById(R.id.dateButton)

        timeButton =
            findViewById(R.id.timeButton)

        continueButton =
            findViewById(R.id.continueButton)
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

        val intent =
            Intent(
                this,
                Services::class.java
            )

        intent.putExtra(
            "SELECTION_MODE",
            true
        )

        startActivityForResult(
            intent,
            SERVICE_REQUEST
        )
    }

    private fun openBarbers() {

        val intent =
            Intent(
                this,
                Barbers::class.java
            )

        intent.putExtra(
            "SELECTION_MODE",
            true
        )

        startActivityForResult(
            intent,
            BARBER_REQUEST
        )
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {

        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )

        if (
            resultCode != RESULT_OK ||
            data == null
        ) {
            return
        }

        when (requestCode) {

            SERVICE_REQUEST -> {

                selectedServiceId =
                    data.getLongExtra(
                        "SERVICE_ID",
                        -1L
                    )

                selectedServiceName =
                    data.getStringExtra(
                        "SERVICE_NAME"
                    ) ?: ""

                selectedServicePrice =
                    data.getDoubleExtra(
                        "SERVICE_PRICE_VALUE",
                        0.0
                    )

                selectedServiceDuration =
                    databaseHelper.getServiceDuration(
                        selectedServiceId
                    ) ?: 0

                selectedServiceText.text =
                    selectedServiceName

                priceText.text =
                    String.format(
                        Locale.getDefault(),
                        "R%.0f",
                        selectedServicePrice
                    )

                selectedTime = ""

                selectedTimeText.text =
                    "Select time"
            }

            BARBER_REQUEST -> {

                selectedBarberId =
                    data.getLongExtra(
                        "BARBER_ID",
                        -1L
                    )

                selectedBarberName =
                    data.getStringExtra(
                        "BARBER_NAME"
                    ) ?: ""

                selectedBarberText.text =
                    selectedBarberName

                selectedTime = ""

                selectedTimeText.text =
                    "Select time"
            }
        }
    }

    private fun chooseDate() {

        val calendar =
            Calendar.getInstance()

        val datePicker =
            DatePickerDialog(
                this,
                { _, year, month, day ->

                    val chosenDate =
                        Calendar.getInstance()

                    chosenDate.set(
                        year,
                        month,
                        day
                    )

                    val formatter =
                        SimpleDateFormat(
                            "yyyy-MM-dd",
                            Locale.getDefault()
                        )

                    selectedDate =
                        formatter.format(
                            chosenDate.time
                        )

                    selectedDateText.text =
                        selectedDate

                    selectedTime = ""

                    selectedTimeText.text =
                        "Select time"
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )

        datePicker.datePicker.minDate =
            System.currentTimeMillis()

        datePicker.show()
    }

    private fun chooseTime() {

        if (selectedServiceId <= 0L) {

            Toast.makeText(
                this,
                "Please select a service first.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        if (selectedBarberId <= 0L) {

            Toast.makeText(
                this,
                "Please select a barber first.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        if (selectedDate.isEmpty()) {

            Toast.makeText(
                this,
                "Please select a date first.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val duration =
            databaseHelper.getServiceDuration(
                selectedServiceId
            ) ?: 0

        if (duration <= 0) {

            Toast.makeText(
                this,
                "Service duration could not be found.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        val availableTimes =
            databaseHelper.getAvailableTimeSlots(
                selectedBarberId,
                selectedDate,
                duration
            )

        if (availableTimes.isEmpty()) {

            Toast.makeText(
                this,
                "No available times for this barber on $selectedDate.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        val timeArray =
            availableTimes.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Select Time")
            .setItems(timeArray) { _, which ->

                selectedTime =
                    timeArray[which]

                selectedTimeText.text =
                    selectedTime
            }
            .show()
    }

    private fun continueToConfirmation() {

        if (!sessionManager.isLoggedIn()) {

            Toast.makeText(
                this,
                "Please log in before booking.",
                Toast.LENGTH_LONG
            ).show()

            startActivity(
                Intent(
                    this,
                    Login::class.java
                )
            )

            return
        }

        if (selectedServiceId <= 0L) {

            Toast.makeText(
                this,
                "Please select a service.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        if (selectedBarberId <= 0L) {

            Toast.makeText(
                this,
                "Please select a barber.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        if (selectedDate.isEmpty()) {

            Toast.makeText(
                this,
                "Please select a date.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        if (selectedTime.isEmpty()) {

            Toast.makeText(
                this,
                "Please select a time.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val stillAvailable =
            databaseHelper.canBookService(
                selectedServiceId,
                selectedBarberId,
                selectedDate,
                selectedTime
            )

        if (!stillAvailable) {

            Toast.makeText(
                this,
                "That time is no longer available. Please select another time.",
                Toast.LENGTH_LONG
            ).show()

            selectedTime = ""

            selectedTimeText.text =
                "Select time"

            return
        }

        val intent =
            Intent(
                this,
                BookingConfirmation::class.java
            )

        intent.putExtra(
            "SERVICE_ID",
            selectedServiceId
        )

        intent.putExtra(
            "BARBER_ID",
            selectedBarberId
        )

        intent.putExtra(
            "SERVICE_NAME",
            selectedServiceName
        )

        intent.putExtra(
            "SERVICE_PRICE_VALUE",
            selectedServicePrice
        )

        intent.putExtra(
            "SERVICE_PRICE",
            String.format(
                Locale.getDefault(),
                "R%.2f",
                selectedServicePrice
            )
        )

        intent.putExtra(
            "BARBER_NAME",
            selectedBarberName
        )

        intent.putExtra(
            "BOOKING_DATE",
            selectedDate
        )

        intent.putExtra(
            "BOOKING_TIME",
            selectedTime
        )

        startActivity(intent)
    }
}