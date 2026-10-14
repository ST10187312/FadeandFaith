package com.example.fadeandfaith

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.firebase.firestore.FirebaseFirestore
import androidx.appcompat.app.AppCompatActivity

class Services : AppCompatActivity() {

    private lateinit var backButton: ImageView

    private lateinit var haircutBookButton: Button
    private lateinit var shaveBookButton: Button
    private lateinit var facialBookButton: Button
    private lateinit var premiumBookButton: Button

    private lateinit var navHome: LinearLayout
    private lateinit var navServices: LinearLayout
    private lateinit var navBooking: LinearLayout
    private lateinit var navProfile: LinearLayout

    private lateinit var priceViews: Map<String, TextView>

    private var selectionMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_services)

        selectionMode =
            intent.getBooleanExtra(
                "SELECTION_MODE",
                false
            )

        initializeViews()
        setupButtons()
    }

    override fun onStart() {
        super.onStart()
        loadPrices()
    }

    // Reads the live price of each service, so a price change made by the
    // boss shows up here as well as on Home and Service details.
    private fun loadPrices() {

        for ((serviceId, view) in priceViews) {

            FirebaseFirestore.getInstance()
                .collection("services")
                .document(serviceId)
                .get()
                .addOnSuccessListener { document ->

                    val price = document.getDouble("price") ?: return@addOnSuccessListener

                    view.text =
                        if (price % 1.0 == 0.0) "R${price.toInt()}"
                        else String.format("R%.2f", price)
                }
        }
    }

    private fun initializeViews() {

        backButton = findViewById(R.id.backButton)

        haircutBookButton = findViewById(R.id.haircutBookButton)
        shaveBookButton = findViewById(R.id.shaveBookButton)
        facialBookButton = findViewById(R.id.facialBookButton)
        premiumBookButton = findViewById(R.id.premiumBookButton)

        priceViews = mapOf(
            "haircut_beard" to findViewById<TextView>(R.id.haircutPrice),
            "shave_haircut" to findViewById<TextView>(R.id.shavePrice),
            "facial_shave" to findViewById<TextView>(R.id.facialPrice),
            "premium_experience" to findViewById<TextView>(R.id.premiumPrice)
        )

        navHome = findViewById(R.id.navHome)
        navServices = findViewById(R.id.navServices)
        navBooking = findViewById(R.id.navBooking)
        navProfile = findViewById(R.id.navProfile)
    }

    private fun setupButtons() {

        backButton.setOnClickListener {
            finish()
        }

        // These four IDs must match your Firestore services/{id} document
        // names exactly -- check your Firebase Console if any of these
        // don't line up with what you actually named them.
        haircutBookButton.setOnClickListener {
            openService("haircut_beard")
        }

        shaveBookButton.setOnClickListener {
            openService("shave_haircut")
        }

        facialBookButton.setOnClickListener {
            openService("facial_shave")
        }

        premiumBookButton.setOnClickListener {
            openService("premium_experience")
        }

        navHome.setOnClickListener {
            startActivity(Intent(this, Home::class.java))
            finish()
        }

        navServices.setOnClickListener {
            // Already on Services
        }

        navBooking.setOnClickListener {
            startActivity(Intent(this, Booking::class.java))
            finish()
        }

        navProfile.setOnClickListener {
            startActivity(Intent(this, Profile::class.java))
            finish()
        }
    }

    private fun openService(serviceId: String) {

        val intent = Intent(this, ServiceDetails::class.java)

        intent.putExtra("SERVICE_ID", serviceId)
        intent.putExtra("SELECTION_MODE", selectionMode)

        startActivityForResult(intent, REQUEST_SERVICE)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (
            requestCode == REQUEST_SERVICE &&
            resultCode == RESULT_OK &&
            data != null
        ) {

            val serviceId = data.getStringExtra("SERVICE_ID") ?: ""
            val serviceName = data.getStringExtra("SERVICE_NAME") ?: ""
            val servicePrice = data.getDoubleExtra("SERVICE_PRICE_VALUE", 0.0)

            if (selectionMode) {

                // Opened from the Booking screen: hand the choice back to it.
                val resultIntent = Intent()
                resultIntent.putExtra("SERVICE_ID", serviceId)
                resultIntent.putExtra("SERVICE_NAME", serviceName)
                resultIntent.putExtra("SERVICE_PRICE_VALUE", servicePrice)

                setResult(RESULT_OK, resultIntent)
                finish()

            } else {

                // Opened from Home or the menu: open Booking with the
                // service already chosen.
                val bookingIntent = Intent(this, Booking::class.java)
                bookingIntent.putExtra("SERVICE_ID", serviceId)
                bookingIntent.putExtra("SERVICE_NAME", serviceName)
                bookingIntent.putExtra("SERVICE_PRICE_VALUE", servicePrice)

                startActivity(bookingIntent)
            }
        }
    }

    companion object {
        private const val REQUEST_SERVICE = 100
    }
}