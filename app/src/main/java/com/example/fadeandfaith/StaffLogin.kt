package com.example.fadeandfaith

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class StaffLogin : AppCompatActivity() {

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText

    private lateinit var loginButton: Button
    private lateinit var backToLoginButton: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private val allowedRoles = listOf("STAFF", "BOSS")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_staff_login)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        initializeViews()
        setupButtons()
    }

    private fun initializeViews() {
        emailInput = findViewById(R.id.staffEmailInput)
        passwordInput = findViewById(R.id.staffPasswordInput)
        loginButton = findViewById(R.id.staffLoginButton)
        backToLoginButton = findViewById(R.id.backToLoginButton)
    }

    private fun setupButtons() {

        loginButton.setOnClickListener {

            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailInput.error = "Enter a valid email address"
                emailInput.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                passwordInput.error = "Enter your password"
                passwordInput.requestFocus()
                return@setOnClickListener
            }

            loginButton.isEnabled = false

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->

                    if (!task.isSuccessful) {
                        loginButton.isEnabled = true
                        Toast.makeText(
                            this,
                            "Incorrect staff email or password.",
                            Toast.LENGTH_LONG
                        ).show()
                        return@addOnCompleteListener
                    }

                    val uid = auth.currentUser?.uid

                    if (uid == null) {
                        loginButton.isEnabled = true
                        Toast.makeText(
                            this,
                            "Login failed. Please try again.",
                            Toast.LENGTH_LONG
                        ).show()
                        return@addOnCompleteListener
                    }

                    firestore.collection("users").document(uid).get()
                        .addOnSuccessListener { document ->

                            if (!document.exists()) {
                                loginButton.isEnabled = true
                                Toast.makeText(
                                    this,
                                    "Staff account is not registered in the system.",
                                    Toast.LENGTH_LONG
                                ).show()
                                auth.signOut()
                                return@addOnSuccessListener
                            }

                            val role = document.getString("role") ?: ""

                            if (!allowedRoles.any { it.equals(role, ignoreCase = true) }) {
                                loginButton.isEnabled = true
                                Toast.makeText(
                                    this,
                                    "This account does not have staff access.",
                                    Toast.LENGTH_LONG
                                ).show()
                                auth.signOut()
                                return@addOnSuccessListener
                            }

                            val staffName = document.getString("name") ?: ""

                            // Look up the matching staff directory entry
                            // (keyed by name) to find which barber this
                            // person is. The boss may not have a matching
                            // entry, which is fine -- barberId just stays
                            // null for them.
                            firestore.collection("staff").document(staffName).get()
                                .addOnSuccessListener { staffDoc ->

                                    val barberId = staffDoc.getString("barberID")

                                    SessionManager(this).apply {
                                        login(uid)
                                        setStaffRole(role)
                                        setStaffBarberId(barberId)
                                    }

                                    registerPushToken(uid)

                                    loginButton.isEnabled = true

                                    startActivity(Intent(this, StaffDashboard::class.java))
                                    finishAffinity()
                                }
                                .addOnFailureListener {

                                    SessionManager(this).apply {
                                        login(uid)
                                        setStaffRole(role)
                                    }

                                    registerPushToken(uid)

                                    loginButton.isEnabled = true

                                    startActivity(Intent(this, StaffDashboard::class.java))
                                    finishAffinity()
                                }
                        }
                        .addOnFailureListener { e ->
                            loginButton.isEnabled = true
                            Toast.makeText(
                                this,
                                "Could not verify staff access: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                }
        }

        backToLoginButton.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
            finish()
        }
    }

    /**
     * Saves this device's push token to Firestore so the notifyStaffOfBooking
     * Cloud Function can find it later. onNewToken() in
     * MyFirebaseMessagingService only fires once per install, so this call
     * here makes sure it also happens on every login, not just the first one.
     */
    private fun registerPushToken(uid: String) {

        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->

            FirebaseFirestore.getInstance()
                .collection("staff_tokens")
                .document(uid)
                .set(mapOf("token" to token))
        }
    }
}