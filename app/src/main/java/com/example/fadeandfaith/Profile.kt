package com.example.fadeandfaith

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Profile : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var sessionManager: SessionManager

    private lateinit var backButton: ImageView
    private lateinit var profileImage: ImageView
    private lateinit var nameText: TextView
    private lateinit var emailText: TextView
    private lateinit var phoneText: TextView

    private lateinit var editProfileButton: MaterialButton
    private lateinit var bookingsButton: LinearLayout
    private lateinit var paymentButton: LinearLayout
    private lateinit var changePasswordButton: LinearLayout
    private lateinit var settingsButton: LinearLayout
    private lateinit var logoutButton: LinearLayout

    private lateinit var navHome: LinearLayout
    private lateinit var navServices: LinearLayout
    private lateinit var navBooking: LinearLayout
    private lateinit var navProfile: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_profile)

        firestore = FirebaseFirestore.getInstance()
        sessionManager = SessionManager(this)

        initializeViews()
        setupClickListeners()
    }

    override fun onStart() {
        super.onStart()

        // Reloaded every time this screen appears, so changes made in
        // Edit Profile show up as soon as you come back.
        loadProfile()
    }

    private fun loadProfile() {

        val userId = sessionManager.getUserId() ?: return

        firestore.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->

                val name = document.getString("name") ?: ""

                nameText.text = name
                    .split(" ")
                    .joinToString(" ") { word ->
                        word.replaceFirstChar { it.uppercase() }
                    }

                emailText.text = document.getString("email") ?: ""
                phoneText.text = document.getString("phone") ?: ""
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Could not load your profile: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun initializeViews() {

        backButton = findViewById(R.id.backButton)

        profileImage = findViewById(R.id.profileImage)
        nameText = findViewById(R.id.nameText)
        emailText = findViewById(R.id.emailText)
        phoneText = findViewById(R.id.phoneText)

        editProfileButton = findViewById(R.id.editProfileButton)

        bookingsButton = findViewById(R.id.bookingsButton)
        paymentButton = findViewById(R.id.paymentButton)
        changePasswordButton = findViewById(R.id.changePasswordButton)
        settingsButton = findViewById(R.id.settingsButton)
        logoutButton = findViewById(R.id.logoutButton)

        navHome = findViewById(R.id.navHome)
        navServices = findViewById(R.id.navServices)
        navBooking = findViewById(R.id.navBooking)
        navProfile = findViewById(R.id.navProfile)
    }

    private fun setupClickListeners() {

        backButton.setOnClickListener {
            finish()
        }

        editProfileButton.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        bookingsButton.setOnClickListener {
            startActivity(Intent(this, MyBookings::class.java))
        }

        paymentButton.setOnClickListener {
            startActivity(Intent(this, PaymentMethodActivity::class.java))
        }

        changePasswordButton.setOnClickListener {
            startActivity(Intent(this, ChangePasswordActivity::class.java))
        }

        settingsButton.setOnClickListener {
            startActivity(Intent(this, Settings::class.java))
        }

        logoutButton.setOnClickListener {

            // The fix: actually sign out of Firebase and clear the saved
            // session, instead of only moving to the Login screen.
            FirebaseAuth.getInstance().signOut()
            sessionManager.logout()

            Toast.makeText(
                this,
                "Logged out.",
                Toast.LENGTH_SHORT
            ).show()

            val intent = Intent(this, Login::class.java)

            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
            finish()
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
            startActivity(Intent(this, MyBookings::class.java))
        }

        navProfile.setOnClickListener {
            // Already on Profile
        }
    }
}