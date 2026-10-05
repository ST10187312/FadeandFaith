package com.example.fadeandfaith

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class Register : AppCompatActivity() {

    private lateinit var nameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var phoneInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var confirmPasswordInput: EditText

    private lateinit var registerButton: Button
    private lateinit var loginButton: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        initializeViews()
        setupButtons()
    }

    private fun initializeViews() {
        nameInput = findViewById(R.id.nameInput)
        emailInput = findViewById(R.id.emailInput)
        phoneInput = findViewById(R.id.phoneInput)
        passwordInput = findViewById(R.id.passwordInput)
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput)

        registerButton = findViewById(R.id.registerButton)
        loginButton = findViewById(R.id.loginButton)
    }

    private fun setupButtons() {

        registerButton.setOnClickListener {

            val name = nameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val phone = phoneInput.text.toString().trim()
            val password = passwordInput.text.toString()
            val confirmPassword = confirmPasswordInput.text.toString()

            if (name.isEmpty()) {
                nameInput.error = "Enter your full name"
                nameInput.requestFocus()
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailInput.error = "Enter a valid email address"
                emailInput.requestFocus()
                return@setOnClickListener
            }

            if (phone.isEmpty()) {
                phoneInput.error = "Enter your phone number"
                phoneInput.requestFocus()
                return@setOnClickListener
            }

            if (password.length < 8) {
                passwordInput.error = "Password must contain at least 8 characters"
                passwordInput.requestFocus()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                confirmPasswordInput.error = "Passwords do not match"
                confirmPasswordInput.requestFocus()
                return@setOnClickListener
            }

            registerButton.isEnabled = false

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->

                    if (!task.isSuccessful) {

                        registerButton.isEnabled = true

                        val message = when (task.exception) {
                            is FirebaseAuthUserCollisionException ->
                                "An account with this email already exists."
                            is FirebaseAuthWeakPasswordException ->
                                "Please choose a stronger password."
                            else ->
                                task.exception?.localizedMessage
                                    ?: "Registration failed. Please try again."
                        }

                        if (task.exception is FirebaseAuthUserCollisionException) {
                            emailInput.error = message
                            emailInput.requestFocus()
                        }

                        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                        return@addOnCompleteListener
                    }

                    val uid = auth.currentUser?.uid

                    if (uid == null) {
                        registerButton.isEnabled = true
                        Toast.makeText(
                            this,
                            "Registration failed. Please try again.",
                            Toast.LENGTH_LONG
                        ).show()
                        return@addOnCompleteListener
                    }

                    val userData = hashMapOf(
                        "name" to name,
                        "email" to email.lowercase(),
                        "phone" to phone,
                        "location" to "",
                        "role" to "CUSTOMER",
                        "createdAt" to FieldValue.serverTimestamp()
                    )

                    firestore.collection("users")
                        .document(uid)
                        .set(userData)
                        .addOnSuccessListener {

                            SessionManager(this).login(uid)

                            Toast.makeText(
                                this,
                                "Account created successfully.",
                                Toast.LENGTH_SHORT
                            ).show()

                            startActivity(Intent(this, Home::class.java))
                            finishAffinity()
                        }
                        .addOnFailureListener { e ->

                            registerButton.isEnabled = true

                            Toast.makeText(
                                this,
                                "Account created, but saving your profile failed: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                }
        }

        loginButton.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
            finish()
        }
    }
}