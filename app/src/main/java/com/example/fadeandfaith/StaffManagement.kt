package com.example.fadeandfaith

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Boss only. Lists every staff member and lets the boss email them a
 * password reset link.
 */
class StaffManagement : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var staffContainer: LinearLayout
    private lateinit var emptyText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!SessionManager(this).isBoss()) {
            Toast.makeText(this, "Only the boss can manage staff.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setContentView(R.layout.activity_staff_management)

        firestore = FirebaseFirestore.getInstance()
        staffContainer = findViewById(R.id.staffContainer)
        emptyText = findViewById(R.id.emptyText)

        findViewById<ImageView>(R.id.backButton).setOnClickListener { finish() }

        loadStaff()
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun loadStaff() {

        firestore.collection("staff").get()
            .addOnSuccessListener { snapshot ->

                staffContainer.removeAllViews()

                if (snapshot.isEmpty) {
                    emptyText.visibility = View.VISIBLE
                    return@addOnSuccessListener
                }

                emptyText.visibility = View.GONE

                for (doc in snapshot.documents.sortedBy { it.getString("name") ?: it.id }) {
                    addStaffCard(
                        doc.getString("name") ?: doc.id,
                        doc.getString("email") ?: ""
                    )
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Could not load staff: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun addStaffCard(name: String, email: String) {

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.cardoutline)
            setPadding(dp(20), dp(20), dp(20), dp(20))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(14) }
        }

        card.addView(TextView(this).apply {
            text = name
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 18f
            typeface = Typeface.create("serif", Typeface.BOLD)
        })

        card.addView(TextView(this).apply {
            text = if (email.isEmpty()) "No email on file" else email
            setTextColor(0xFF999999.toInt())
            textSize = 13f
            setPadding(0, dp(4), 0, dp(14))
        })

        val resetButton = MaterialButton(this).apply {
            text = "SEND PASSWORD RESET"
            setTextColor(0xFF050505.toInt())
            textSize = 12f
            setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.CENTER
            setBackgroundResource(R.drawable.gold)
            backgroundTintList = null
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(48)
            )
            setOnClickListener { sendReset(name, email, this) }
        }

        card.addView(resetButton)
        staffContainer.addView(card)
    }

    private fun sendReset(name: String, email: String, button: MaterialButton) {

        if (email.isEmpty()) {
            Toast.makeText(this, "$name has no email address on file.", Toast.LENGTH_LONG).show()
            return
        }

        button.isEnabled = false

        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnSuccessListener {
                button.isEnabled = true
                Toast.makeText(
                    this,
                    "Reset link sent to $email",
                    Toast.LENGTH_LONG
                ).show()
            }
            .addOnFailureListener { e ->
                button.isEnabled = true
                Toast.makeText(
                    this,
                    "Could not send reset: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}