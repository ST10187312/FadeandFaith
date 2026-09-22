package com.example.fadeandfaith

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class EditProfileActivity : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var profileImage: ImageView

    private lateinit var nameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var phoneInput: EditText
    private lateinit var locationInput: EditText

    private lateinit var saveButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_edit_profile)

        initializeViews()
        loadProfile()
        setupClickListeners()
    }

    private fun initializeViews() {

        backButton = findViewById(R.id.backButton)
        profileImage = findViewById(R.id.profileImage)

        nameInput = findViewById(R.id.nameInput)
        emailInput = findViewById(R.id.emailInput)
        phoneInput = findViewById(R.id.phoneInput)
        locationInput = findViewById(R.id.locationInput)

        saveButton = findViewById(R.id.saveButton)
    }

    private fun loadProfile() {

        nameInput.setText("Siyabonga Makhubu")
        emailInput.setText("siyabongamakhubu22@gmail.com")
        phoneInput.setText("+27 00 000 0000")
        locationInput.setText("Johannesburg, Gauteng")
    }

    private fun setupClickListeners() {

        backButton.setOnClickListener {
            finish()
        }

        saveButton.setOnClickListener {

            val name = nameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val phone = phoneInput.text.toString().trim()
            val location = locationInput.text.toString().trim()

            if (name.isEmpty()) {
                nameInput.error = "Enter your full name"
                nameInput.requestFocus()
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                emailInput.error = "Enter your email"
                emailInput.requestFocus()
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS
                    .matcher(email)
                    .matches()
            ) {
                emailInput.error = "Enter a valid email address"
                emailInput.requestFocus()
                return@setOnClickListener
            }

            if (phone.isEmpty()) {
                phoneInput.error = "Enter your phone number"
                phoneInput.requestFocus()
                return@setOnClickListener
            }

            if (location.isEmpty()) {
                locationInput.error = "Enter your location"
                locationInput.requestFocus()
                return@setOnClickListener
            }

            Toast.makeText(
                this,
                "Profile updated successfully.",
                Toast.LENGTH_SHORT
            ).show()

            finish()
        }
    }
}