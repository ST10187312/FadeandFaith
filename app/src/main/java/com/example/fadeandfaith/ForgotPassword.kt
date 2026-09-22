package com.example.fadeandfaith

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ForgotPassword : AppCompatActivity() {

    private lateinit var emailInput: EditText
    private lateinit var resetButton: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_forgot_password)

        auth = FirebaseAuth.getInstance()

        emailInput = findViewById(R.id.emailInput)
        resetButton = findViewById(R.id.resetButton)

        resetButton.setOnClickListener {

            val email = emailInput.text.toString().trim()

            if (email.isEmpty()) {
                emailInput.error = "Enter your email"
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailInput.error = "Enter a valid email"
                return@setOnClickListener
            }

            resetButton.isEnabled = false

            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(this) { task ->

                    resetButton.isEnabled = true

                    if (task.isSuccessful) {
                        Toast.makeText(
                            this,
                            "If an account exists for $email, a reset link has been sent.",
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    } else {
                        Toast.makeText(
                            this,
                            task.exception?.localizedMessage
                                ?: "Couldn't send reset email. Try again.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }
}