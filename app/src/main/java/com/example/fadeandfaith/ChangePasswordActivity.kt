package com.example.fadeandfaith

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

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

        currentPasswordInput =
            findViewById(R.id.currentPasswordInput)

        newPasswordInput =
            findViewById(R.id.newPasswordInput)

        confirmPasswordInput =
            findViewById(R.id.confirmPasswordInput)

        changePasswordButton =
            findViewById(R.id.changePasswordButton)
    }

    private fun setupClickListeners() {

        backButton.setOnClickListener {
            finish()
        }

        changePasswordButton.setOnClickListener {

            val currentPassword =
                currentPasswordInput.text.toString()

            val newPassword =
                newPasswordInput.text.toString()

            val confirmPassword =
                confirmPasswordInput.text.toString()

            if (currentPassword.isEmpty()) {

                currentPasswordInput.error =
                    "Enter your current password"

                currentPasswordInput.requestFocus()

                return@setOnClickListener
            }

            if (newPassword.isEmpty()) {

                newPasswordInput.error =
                    "Enter your new password"

                newPasswordInput.requestFocus()

                return@setOnClickListener
            }

            if (newPassword.length < 8) {

                newPasswordInput.error =
                    "Password must contain at least 8 characters"

                newPasswordInput.requestFocus()

                return@setOnClickListener
            }

            if (confirmPassword.isEmpty()) {

                confirmPasswordInput.error =
                    "Confirm your new password"

                confirmPasswordInput.requestFocus()

                return@setOnClickListener
            }

            if (newPassword != confirmPassword) {

                confirmPasswordInput.error =
                    "Passwords do not match"

                confirmPasswordInput.requestFocus()

                return@setOnClickListener
            }

            if (currentPassword == newPassword) {

                newPasswordInput.error =
                    "New password must be different"

                newPasswordInput.requestFocus()

                return@setOnClickListener
            }

            Toast.makeText(
                this,
                "Password change will be processed securely through the server.",
                Toast.LENGTH_LONG
            ).show()

            finish()
        }
    }
}