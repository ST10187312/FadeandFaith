package com.example.fadeandfaith

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore

class BarberDetails : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore

    private lateinit var backButton: ImageView
    private lateinit var barberImage: ImageView
    private lateinit var barberNameText: TextView
    private lateinit var barberRoleText: TextView
    private lateinit var barberDescriptionText: TextView
    private lateinit var barberRatingText: TextView
    private lateinit var barberExperienceText: TextView
    private lateinit var bookButton: MaterialButton

    private var barberId: String = ""
    private var barberName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_barber_details)

        firestore = FirebaseFirestore.getInstance()

        barberId = intent.getStringExtra("BARBER_ID") ?: ""

        initializeViews()
        loadBarber()
        setupClickListeners()
    }

    private fun initializeViews() {

        backButton = findViewById(R.id.backButton)
        barberImage = findViewById(R.id.barberImage)
        barberNameText = findViewById(R.id.barberName)
        barberRoleText = findViewById(R.id.barberRole)
        barberDescriptionText = findViewById(R.id.barberDescription)
        barberRatingText = findViewById(R.id.barberRating)
        barberExperienceText = findViewById(R.id.barberExperience)
        bookButton = findViewById(R.id.bookButton)
    }

    private fun loadBarber() {

        if (barberId.isEmpty()) {
            Toast.makeText(
                this,
                "Barber could not be found.",
                Toast.LENGTH_LONG
            ).show()
            finish()
            return
        }

        firestore.collection("barbers")
            .document(barberId)
            .get()
            .addOnSuccessListener { document ->

                if (!document.exists()) {
                    Toast.makeText(
                        this,
                        "Barber could not be found.",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                    return@addOnSuccessListener
                }

                barberName = document.getString("name") ?: ""
                val role = document.getString("role") ?: ""

                // Not every barber document has this field yet -- defaults
                // to blank instead of crashing if it's missing.
                val description = document.getString("description") ?: ""

                val rating = document.getDouble("rating") ?: 0.0
                val experience = document.getLong("experience")?.toInt() ?: 0

                barberNameText.text = barberName
                barberRoleText.text = role
                barberDescriptionText.text = description

                barberRatingText.text = String.format("%.1f", rating)
                barberExperienceText.text = "$experience years experience"

                val uploadedPhoto = ImageStore.base64ToBitmap(document.getString("photoBase64"))

                if (uploadedPhoto != null) {
                    barberImage.setImageBitmap(uploadedPhoto)
                } else {
                    barberImage.setImageResource(
                        when (barberId.lowercase()) {
                            "michael" -> R.drawable.michael
                            "david" -> R.drawable.david
                            else -> R.drawable.james
                        }
                    )
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Barber could not be loaded: ${e.message}",
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

            resultIntent.putExtra("BARBER_ID", barberId)
            resultIntent.putExtra("BARBER_NAME", barberName)

            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }
}