package com.example.fadeandfaith

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class Login : AppCompatActivity() {

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText

    private lateinit var loginButton: Button
    private lateinit var forgotPassword: TextView
    private lateinit var registerButton: TextView
    private lateinit var staffLoginButton: TextView

    private lateinit var database: DatabaseHelper
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        database = DatabaseHelper(this)
        auth = FirebaseAuth.getInstance()

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

                    loginButton.isEnabled = true

                    if (!task.isSuccessful) {
                        Toast.makeText(
                            this,
                            "Incorrect email or password.",
                            Toast.LENGTH_LONG
                        ).show()
                        return@addOnCompleteListener
                    }

                    val uid = auth.currentUser?.uid

                    if (uid == null) {
                        Toast.makeText(
                            this,
                            "Login failed. Please try again.",
                            Toast.LENGTH_LONG
                        ).show()
                        return@addOnCompleteListener
                    }

                    var userId = database.findLocalUserIdByFirebaseUid(uid)

                    if (userId == -1L) {
                        userId = database.createLocalProfileForFirebaseUser(
                            uid,
                            email.substringBefore("@"),
                            email,
                            ""
                        )
                    }

                    SessionManager(this).login(userId)

                    startActivity(
                        Intent(
                            this,
                            Home::class.java
                        )
                    )

                    finishAffinity()
                }
        }

        forgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPassword::class.java))
        }

        registerButton.setOnClickListener {
            startActivity(Intent(this, Register::class.java))
        }

        // STAFF LOGIN
        staffLoginButton.setOnClickListener {
            startActivity(Intent(this, StaffLogin::class.java))
        }
    }
}