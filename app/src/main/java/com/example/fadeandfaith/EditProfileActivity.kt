package com.example.fadeandfaith

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var sessionManager: SessionManager

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

        firestore = FirebaseFirestore.getInstance()
        sessionManager = SessionManager(this)

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

        // Email is the login identity, so it can be viewed but not edited.
        emailInput.isEnabled = false
    }

    private fun loadProfile() {

        val userId = sessionManager.getUserId()

        if (userId == null) {
            Toast.makeText(this, "Please log in again.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        firestore.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->

                nameInput.setText(document.getString("name") ?: "")
                emailInput.setText(document.getString("email") ?: "")
                phoneInput.setText(document.getString("phone") ?: "")
                locationInput.setText(document.getString("location") ?: "")
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Could not load your profile: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun setupClickListeners() {

        backButton.setOnClickListener {
            finish()
        }

        saveButton.setOnClickListener {
            saveProfile()
        }
    }

    private fun saveProfile() {

        val userId = sessionManager.getUserId()

        if (userId == null) {
            Toast.makeText(this, "Please log in again.", Toast.LENGTH_LONG).show()
            return
        }

        val name = nameInput.text.toString().trim()
        val phone = phoneInput.text.toString().trim()
        val location = locationInput.text.toString().trim()

        if (name.isEmpty()) {
            nameInput.error = "Enter your full name"
            nameInput.requestFocus()
            return
        }

        if (phone.isEmpty()) {
            phoneInput.error = "Enter your phone number"
            phoneInput.requestFocus()
            return
        }

        if (location.isEmpty()) {
            locationInput.error = "Enter your location"
            locationInput.requestFocus()
            return
        }

        saveButton.isEnabled = false

        firestore.collection("users")
            .document(userId)
            .update(
                mapOf(
                    "name" to name,
                    "phone" to phone,
                    "location" to location
                )
            )
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "Profile updated successfully.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
            .addOnFailureListener { e ->
                saveButton.isEnabled = true
                Toast.makeText(
                    this,
                    "Could not save your profile: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}