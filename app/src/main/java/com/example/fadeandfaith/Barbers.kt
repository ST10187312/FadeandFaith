package com.example.fadeandfaith

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class Barbers : AppCompatActivity() {

    private lateinit var backButton: ImageView

    private lateinit var jamesBookButton: Button
    private lateinit var michaelBookButton: Button
    private lateinit var davidBookButton: Button

    private lateinit var navHome: LinearLayout
    private lateinit var navServices: LinearLayout
    private lateinit var navBooking: LinearLayout
    private lateinit var navProfile: LinearLayout

    private var selectionMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_barbers)

        selectionMode =
            intent.getBooleanExtra(
                "SELECTION_MODE",
                false
            )

        initializeViews()
        setupButtons()
    }

    private fun initializeViews() {

        backButton =
            findViewById(R.id.backButton)

        jamesBookButton =
            findViewById(R.id.jamesBookButton)

        michaelBookButton =
            findViewById(R.id.michaelBookButton)

        davidBookButton =
            findViewById(R.id.davidBookButton)

        navHome =
            findViewById(R.id.navHome)

        navServices =
            findViewById(R.id.navServices)

        navBooking =
            findViewById(R.id.navBooking)

        navProfile =
            findViewById(R.id.navProfile)
    }

    private fun setupButtons() {

        backButton.setOnClickListener {
            finish()
        }

        jamesBookButton.setOnClickListener {
            openBarber(1L)
        }

        michaelBookButton.setOnClickListener {
            openBarber(2L)
        }

        davidBookButton.setOnClickListener {
            openBarber(3L)
        }

        navHome.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    Home::class.java
                )
            )

            finish()
        }

        navServices.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    Services::class.java
                )
            )

            finish()
        }

        navBooking.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    Booking::class.java
                )
            )

            finish()
        }

        navProfile.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    Profile::class.java
                )
            )

            finish()
        }
    }

    private fun openBarber(barberId: Long) {

        val intent =
            Intent(
                this,
                BarberDetails::class.java
            )

        intent.putExtra(
            "BARBER_ID",
            barberId
        )

        intent.putExtra(
            "SELECTION_MODE",
            selectionMode
        )

        startActivityForResult(
            intent,
            REQUEST_BARBER
        )
    }

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
            requestCode == REQUEST_BARBER &&
            resultCode == RESULT_OK &&
            data != null
        ) {

            val resultIntent =
                Intent()

            resultIntent.putExtra(
                "BARBER_ID",
                data.getLongExtra(
                    "BARBER_ID",
                    -1L
                )
            )

            resultIntent.putExtra(
                "BARBER_NAME",
                data.getStringExtra(
                    "BARBER_NAME"
                ) ?: ""
            )

            setResult(
                RESULT_OK,
                resultIntent
            )

            finish()
        }
    }

    companion object {

        private const val REQUEST_BARBER =
            200
    }
}