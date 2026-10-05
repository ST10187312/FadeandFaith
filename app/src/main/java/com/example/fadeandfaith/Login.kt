package com.example.fadeandfaith

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class Login : AppCompatActivity() {

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText

    private lateinit var loginButton: Button
    private lateinit var forgotPassword: TextView
    private lateinit var registerButton: TextView
    private lateinit var staffLoginButton: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        initializeViews()
        setupButtons()
    }

    private fun initializeViews() {
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)

        loginButton = findViewById(R.id.loginButton)
        forgotPassword = findViewById(R.id.forgotPassword)
        registerButton = findViewById(R.id.registerButton)
        staffLoginButton = findViewById(R.id.staffLoginButton)
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
                            "Incorrect email or password.",
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

                    val userDocRef = firestore.collection("users").document(uid)

                    userDocRef.get()
                        .addOnSuccessListener { document ->

                            if (!document.exists()) {

                                // Edge case: signed into Firebase but no Firestore
                                // profile yet -- create a basic one so nothing
                                // downstream crashes on a missing document.
                                val userData = hashMapOf(
                                    "name" to email.substringBefore("@"),
                                    "email" to email,
                                    "phone" to "",
                                    "location" to "",
                                    "role" to "CUSTOMER",
                                    "createdAt" to FieldValue.serverTimestamp()
                                )

                                userDocRef.set(userData)
                            }

                            SessionManager(this).login(uid)

                            startActivity(Intent(this, Home::class.java))
                            finishAffinity()
                        }
                        .addOnFailureListener { e ->

                            loginButton.isEnabled = true

                            Toast.makeText(
                                this,
                                "Could not load your profile: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                }
        }

        forgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPassword::class.java))
        }

        registerButton.setOnClickListener {
            startActivity(Intent(this, Register::class.java))
        }

        staffLoginButton.setOnClickListener {
            startActivity(Intent(this, StaffLogin::class.java))
        }
    }
}