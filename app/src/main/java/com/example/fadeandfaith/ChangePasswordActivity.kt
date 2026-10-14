package com.example.fadeandfaith

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var backButton: ImageView

    private lateinit var currentPasswordInput: EditText
    private lateinit var newPasswordInput: EditText
    private lateinit var confirmPasswordInput: EditText

    private lateinit var changePasswordButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_change_password)

        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {

        backButton = findViewById(R.id.backButton)

        currentPasswordInput = findViewById(R.id.currentPasswordInput)
        newPasswordInput = findViewById(R.id.newPasswordInput)
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput)

        changePasswordButton = findViewById(R.id.changePasswordButton)
    }

    private fun setupClickListeners() {

        backButton.setOnClickListener {
            finish()
        }

        changePasswordButton.setOnClickListener {

            val currentPassword = currentPasswordInput.text.toString()
            val newPassword = newPasswordInput.text.toString()
            val confirmPassword = confirmPasswordInput.text.toString()

            if (currentPassword.isEmpty()) {
                currentPasswordInput.error = "Enter your current password"
                currentPasswordInput.requestFocus()
                return@setOnClickListener
            }

            if (newPassword.isEmpty()) {
                newPasswordInput.error = "Enter your new password"
                newPasswordInput.requestFocus()
                return@setOnClickListener
            }

            if (newPassword.length < 8) {
                newPasswordInput.error = "Password must contain at least 8 characters"
                newPasswordInput.requestFocus()
                return@setOnClickListener
            }

            if (confirmPassword.isEmpty()) {
                confirmPasswordInput.error = "Confirm your new password"
                confirmPasswordInput.requestFocus()
                return@setOnClickListener
            }

            if (newPassword != confirmPassword) {
                confirmPasswordInput.error = "Passwords do not match"
                confirmPasswordInput.requestFocus()
                return@setOnClickListener
            }

            if (currentPassword == newPassword) {
                newPasswordInput.error = "New password must be different"
                newPasswordInput.requestFocus()
                return@setOnClickListener
            }

            changePassword(currentPassword, newPassword)
        }
    }

    private fun changePassword(currentPassword: String, newPassword: String) {

        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email

        if (user == null || email == null) {
            Toast.makeText(this, "Please log in again.", Toast.LENGTH_LONG).show()
            return
        }

        changePasswordButton.isEnabled = false

        // Step 1: prove it's really you by re-checking the current password.
        val credential = EmailAuthProvider.getCredential(email, currentPassword)

        user.reauthenticate(credential)
            .addOnSuccessListener {

                // Step 2: now Firebase allows the change.
                user.updatePassword(newPassword)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            "Password changed successfully.",
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        changePasswordButton.isEnabled = true
                        Toast.makeText(
                            this,
                            "Could not change password: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
            .addOnFailureListener { e ->

                changePasswordButton.isEnabled = true

                if (e is FirebaseAuthInvalidCredentialsException) {
                    currentPasswordInput.error = "Current password is incorrect"
                    currentPasswordInput.requestFocus()
                } else {
                    Toast.makeText(
                        this,
                        "Could not verify your password: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}