package com.example.fadeandfaith

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class BarberDetails : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper

    private lateinit var backButton: ImageView
    private lateinit var barberNameText: TextView
    private lateinit var barberRoleText: TextView
    private lateinit var barberDescriptionText: TextView
    private lateinit var barberRatingText: TextView
    private lateinit var barberExperienceText: TextView
    private lateinit var bookButton: MaterialButton

    private var barberId: Long = -1L
    private var selectionMode = false

    private var barberName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_barber_details)

        databaseHelper = DatabaseHelper(this)

        barberId =
            intent.getLongExtra(
                "BARBER_ID",
                -1L
            )

        selectionMode =
            intent.getBooleanExtra(
                "SELECTION_MODE",
                false
            )

        initializeViews()
        loadBarber()
        setupClickListeners()
    }

    private fun initializeViews() {

        backButton =
            findViewById(R.id.backButton)

        barberNameText =
            findViewById(R.id.barberName)

        barberRoleText =
            findViewById(R.id.barberRole)

        barberDescriptionText =
            findViewById(R.id.barberDescription)

        barberRatingText =
            findViewById(R.id.barberRating)

        barberExperienceText =
            findViewById(R.id.barberExperience)

        bookButton =
            findViewById(R.id.bookButton)
    }

    private fun loadBarber() {

        if (barberId <= 0) {

            Toast.makeText(
                this,
                "Barber could not be found.",
                Toast.LENGTH_LONG
            ).show()

            finish()
            return
        }

        val cursor =
            databaseHelper.getBarber(
                barberId
            )

        if (cursor == null) {

            Toast.makeText(
                this,
                "Barber could not be found.",
                Toast.LENGTH_LONG
            ).show()

            finish()
            return
        }

        cursor.use {

            if (!it.moveToFirst()) {

                Toast.makeText(
                    this,
                    "Barber could not be found.",
                    Toast.LENGTH_LONG
                ).show()

                finish()
                return
            }

            barberName =
                it.getString(
                    it.getColumnIndexOrThrow(
                        "name"
                    )
                )

            val role =
                it.getString(
                    it.getColumnIndexOrThrow(
                        "role"
                    )
                )

            val description =
                it.getString(
                    it.getColumnIndexOrThrow(
                        "description"
                    )
                )

            val rating =
                it.getDouble(
                    it.getColumnIndexOrThrow(
                        "rating"
                    )
                )

            val experience =
                it.getInt(
                    it.getColumnIndexOrThrow(
                        "experience"
                    )
                )

            barberNameText.text =
                barberName

            barberRoleText.text =
                role

            barberDescriptionText.text =
                description

            barberRatingText.text =
                String.format(
                    "%.1f",
                    rating
                )

            barberExperienceText.text =
                "$experience years experience"
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
                "BARBER_ID",
                barberId
            )

            resultIntent.putExtra(
                "BARBER_NAME",
                barberName
            )

            setResult(
                RESULT_OK,
                resultIntent
            )

            finish()
        }
    }
}