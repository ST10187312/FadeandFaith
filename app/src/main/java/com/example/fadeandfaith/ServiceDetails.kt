package com.example.fadeandfaith

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore

class ServiceDetails : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore

    private lateinit var backButton: ImageView
    private lateinit var serviceNameText: TextView
    private lateinit var serviceDescriptionText: TextView
    private lateinit var servicePriceText: TextView
    private lateinit var serviceDurationText: TextView
    private lateinit var bookButton: MaterialButton

    private var serviceId: String = ""

    private var serviceName = ""
    private var servicePrice = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_service_details)

        firestore = FirebaseFirestore.getInstance()

        serviceId = intent.getStringExtra("SERVICE_ID") ?: ""

        initializeViews()
        loadService()
        setupClickListeners()
    }

    private fun initializeViews() {

        backButton = findViewById(R.id.backButton)
        serviceNameText = findViewById(R.id.serviceName)
        serviceDescriptionText = findViewById(R.id.serviceDescription)
        servicePriceText = findViewById(R.id.servicePrice)
        serviceDurationText = findViewById(R.id.serviceDuration)
        bookButton = findViewById(R.id.bookButton)
    }

    private fun loadService() {

        if (serviceId.isEmpty()) {
            Toast.makeText(
                this,
                "Service could not be found.",
                Toast.LENGTH_LONG
            ).show()
            finish()
            return
        }

        firestore.collection("services")
            .document(serviceId)
            .get()
            .addOnSuccessListener { document ->

                if (!document.exists()) {
                    Toast.makeText(
                        this,
                        "Service could not be found.",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                    return@addOnSuccessListener
                }

                serviceName = document.getString("name") ?: ""
                val description = document.getString("description") ?: ""
                servicePrice = document.getDouble("price") ?: 0.0
                val duration = document.getLong("duration")?.toInt() ?: 0

                serviceNameText.text = serviceName
                serviceDescriptionText.text = description

                servicePriceText.text =
                    "R${String.format("%.2f", servicePrice)}"

                serviceDurationText.text = "$duration minutes"
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Service could not be loaded: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
    }

    private fun setupClickListeners() {

        backButton.setOnClickListener {
            finish()
        }

        bookButton.setOnClickListener {

            val resultIntent = Intent()

            resultIntent.putExtra("SERVICE_ID", serviceId)
            resultIntent.putExtra("SERVICE_NAME", serviceName)
            resultIntent.putExtra("SERVICE_PRICE_VALUE", servicePrice)

            resultIntent.putExtra(
                "SERVICE_PRICE",
                "R${String.format("%.2f", servicePrice)}"
            )

            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }
}