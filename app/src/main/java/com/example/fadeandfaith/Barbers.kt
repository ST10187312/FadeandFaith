package com.example.fadeandfaith

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

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
            intent.getBooleanExtra("SELECTION_MODE", false)

        initializeViews()
        setupButtons()
    }

    override fun onStart() {
        super.onStart()
        loadPhotos()
    }

    // If a barber has uploaded their own photo, show it instead of the
    // built-in placeholder.
    private fun loadPhotos() {

        val photos = mapOf(
            "James" to findViewById<ImageView>(R.id.JAMES),
            "Michael" to findViewById<ImageView>(R.id.michael),
            "David" to findViewById<ImageView>(R.id.david)
        )

        for ((barberId, imageView) in photos) {

            FirebaseFirestore.getInstance()
                .collection("barbers")
                .document(barberId)
                .get()
                .addOnSuccessListener { document ->

                    val bitmap = ImageStore.base64ToBitmap(document.getString("photoBase64"))

                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap)
                    }
                }
        }
    }

    private fun initializeViews() {

        backButton = findViewById(R.id.backButton)

        jamesBookButton = findViewById(R.id.jamesBookButton)
        michaelBookButton = findViewById(R.id.michaelBookButton)
        davidBookButton = findViewById(R.id.davidBookButton)

        navHome = findViewById(R.id.navHome)
        navServices = findViewById(R.id.navServices)
        navBooking = findViewById(R.id.navBooking)
        navProfile = findViewById(R.id.navProfile)
    }

    private fun setupButtons() {

        backButton.setOnClickListener {
            finish()
        }

        // These three must match your Firestore barbers/{name} document
        // names exactly, including capitalization.
        jamesBookButton.setOnClickListener {
            openBarber("James")
        }

        michaelBookButton.setOnClickListener {
            openBarber("Michael")
        }

        davidBookButton.setOnClickListener {
            openBarber("David")
        }

        navHome.setOnClickListener {
            startActivity(Intent(this, Home::class.java))
            finish()
        }

        navServices.setOnClickListener {
            startActivity(Intent(this, Services::class.java))
            finish()
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

    private fun openBarber(barberId: String) {

        val intent = Intent(this, BarberDetails::class.java)

        intent.putExtra("BARBER_ID", barberId)
        intent.putExtra("SELECTION_MODE", selectionMode)

        startActivityForResult(intent, REQUEST_BARBER)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (
            requestCode == REQUEST_BARBER &&
            resultCode == RESULT_OK &&
            data != null
        ) {

            val barberId = data.getStringExtra("BARBER_ID") ?: ""
            val barberName = data.getStringExtra("BARBER_NAME") ?: ""

            if (selectionMode) {

                // Opened from the Booking screen: hand the choice back to it.
                val resultIntent = Intent()
                resultIntent.putExtra("BARBER_ID", barberId)
                resultIntent.putExtra("BARBER_NAME", barberName)

                setResult(RESULT_OK, resultIntent)
                finish()

            } else {

                // Opened from Home or the menu: open Booking with the
                // barber already chosen.
                val bookingIntent = Intent(this, Booking::class.java)
                bookingIntent.putExtra("BARBER_ID", barberId)
                bookingIntent.putExtra("BARBER_NAME", barberName)

                startActivity(bookingIntent)
            }
        }
    }

    companion object {
        private const val REQUEST_BARBER = 200
    }
}