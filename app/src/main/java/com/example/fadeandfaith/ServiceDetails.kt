package com.example.fadeandfaith

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class ServiceDetails : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper

    private lateinit var backButton: ImageView
    private lateinit var serviceNameText: TextView
    private lateinit var serviceDescriptionText: TextView
    private lateinit var servicePriceText: TextView
    private lateinit var serviceDurationText: TextView
    private lateinit var bookButton: MaterialButton

    private var serviceId: Long = -1L
    private var selectionMode = false

    private var serviceName = ""
    private var servicePrice = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_service_details)

        databaseHelper = DatabaseHelper(this)

        serviceId =
            intent.getLongExtra(
                "SERVICE_ID",
                -1L
            )

        selectionMode =
            intent.getBooleanExtra(
                "SELECTION_MODE",
                false
            )

        initializeViews()
        loadService()
        setupClickListeners()
    }

    private fun initializeViews() {

        backButton =
            findViewById(R.id.backButton)

        serviceNameText =
            findViewById(R.id.serviceName)

        serviceDescriptionText =
            findViewById(R.id.serviceDescription)

        servicePriceText =
            findViewById(R.id.servicePrice)

        serviceDurationText =
            findViewById(R.id.serviceDuration)

        bookButton =
            findViewById(R.id.bookButton)
    }

    private fun loadService() {

        if (serviceId <= 0) {

            Toast.makeText(
                this,
                "Service could not be found.",
                Toast.LENGTH_LONG
            ).show()

            finish()
            return
        }

        val cursor =
            databaseHelper.getService(
                serviceId
            )

        if (cursor == null) {

            Toast.makeText(
                this,
                "Service could not be found.",
                Toast.LENGTH_LONG
            ).show()

            finish()
            return
        }

        cursor.use {

            if (!it.moveToFirst()) {

                Toast.makeText(
                    this,
                    "Service could not be found.",
                    Toast.LENGTH_LONG
                ).show()

                finish()
                return
            }

            serviceName =
                it.getString(
                    it.getColumnIndexOrThrow(
                        "name"
                    )
                )

            val description =
                it.getString(
                    it.getColumnIndexOrThrow(
                        "description"
                    )
                )

            servicePrice =
                it.getDouble(
                    it.getColumnIndexOrThrow(
                        "price"
                    )
                )

            val duration =
                it.getInt(
                    it.getColumnIndexOrThrow(
                        "duration"
                    )
                )

            serviceNameText.text =
                serviceName

            serviceDescriptionText.text =
                description

            servicePriceText.text =
                "R${String.format("%.2f", servicePrice)}"

            serviceDurationText.text =
                "$duration minutes"
        }
    }

    private fun setupClickListeners() {

        backButton.setOnClickListener {
            finish()
        }

        bookButton.setOnClickListener {

            val resultIntent =
                Intent()

            resultIntent.putExtra(
                "SERVICE_ID",
                serviceId
            )

            resultIntent.putExtra(
                "SERVICE_NAME",
                serviceName
            )

            resultIntent.putExtra(
                "SERVICE_PRICE_VALUE",
                servicePrice
            )

            resultIntent.putExtra(
                "SERVICE_PRICE",
                "R${String.format("%.2f", servicePrice)}"
            )

            setResult(
                RESULT_OK,
                resultIntent
            )

            finish()
        }
    }
}