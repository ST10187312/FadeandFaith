package com.example.fadeandfaith

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class StaffLogin : AppCompatActivity() {

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText

    private lateinit var loginButton: Button
    private lateinit var backToLoginButton: TextView

    private lateinit var database: DatabaseHelper
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_staff_login)

        database = DatabaseHelper(this)
        auth = FirebaseAuth.getInstance()

        initializeViews()
        setupButtons()
    }

    private fun initializeViews() {

        emailInput =
            findViewById(R.id.staffEmailInput)

        passwordInput =
            findViewById(R.id.staffPasswordInput)

        loginButton =
            findViewById(R.id.staffLoginButton)

        backToLoginButton =
            findViewById(R.id.backToLoginButton)
    }

    private fun setupButtons() {

        loginButton.setOnClickListener {

            val email =
                emailInput.text.toString().trim()

            val password =
                passwordInput.text.toString()

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

                emailInput.error =
                    "Enter a valid email address"

                emailInput.requestFocus()

                return@setOnClickListener
            }

            if (password.isEmpty()) {

                passwordInput.error =
                    "Enter your password"

                passwordInput.requestFocus()

                return@setOnClickListener
            }

            loginButton.isEnabled = false

            auth.signInWithEmailAndPassword(
                email,
                password
            ).addOnCompleteListener(this) { task ->

                loginButton.isEnabled = true

                if (!task.isSuccessful) {

                    Toast.makeText(
                        this,
                        "Incorrect staff email or password.",
                        Toast.LENGTH_LONG
                    ).show()

                    return@addOnCompleteListener
                }

                val uid =
                    auth.currentUser?.uid

                if (uid == null) {

                    Toast.makeText(
                        this,
                        "Login failed. Please try again.",
                        Toast.LENGTH_LONG
                    ).show()

                    return@addOnCompleteListener
                }

                val userId =
                    database.findLocalUserIdByFirebaseUid(uid)

                if (userId <= 0L) {

                    Toast.makeText(
                        this,
                        "Staff account is not registered in the system.",
                        Toast.LENGTH_LONG
                    ).show()

                    auth.signOut()

                    return@addOnCompleteListener
                }

                val role =
                    database.getUserRole(userId)

                if (!role.equals("STAFF", ignoreCase = true)) {

                    Toast.makeText(
                        this,
                        "This account does not have staff access.",
                        Toast.LENGTH_LONG
                    ).show()

                    auth.signOut()

                    return@addOnCompleteListener
                }

                SessionManager(this).login(userId)

                startActivity(
                    Intent(
                        this,
                        StaffDashboard::class.java
                    )
                )

                finishAffinity()
            }
        }

        backToLoginButton.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    Login::class.java
                )
            )

            finish()
        }
    }
}