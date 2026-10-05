package com.example.fadeandfaith

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class Notifications : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var sessionManager: SessionManager

    private lateinit var backButton: ImageView
    private lateinit var appointmentNotification: LinearLayout
    private lateinit var promotionNotification: LinearLayout
    private lateinit var systemNotification: LinearLayout
    private lateinit var notificationsContainer: LinearLayout
    private lateinit var emptyText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_notifications)

        firestore = FirebaseFirestore.getInstance()
        sessionManager = SessionManager(this)

        initializeViews()

        // The three sample rows baked into the layout were never real --
        // hide them in favour of the dynamic list below.
        appointmentNotification.visibility = LinearLayout.GONE
        promotionNotification.visibility = LinearLayout.GONE
        systemNotification.visibility = LinearLayout.GONE

        backButton.setOnClickListener {
            finish()
        }

        loadNotifications()
    }

    private fun initializeViews() {

        backButton = findViewById(R.id.backButton)
        appointmentNotification = findViewById(R.id.appointmentNotification)
        promotionNotification = findViewById(R.id.promotionNotification)
        systemNotification = findViewById(R.id.systemNotification)
        notificationsContainer = findViewById(R.id.notificationsContainer)
        emptyText = findViewById(R.id.emptyText)
    }

    private fun loadNotifications() {

        val userId = sessionManager.getUserId()

        if (userId == null) {
            emptyText.visibility = LinearLayout.VISIBLE
            return
        }

        val isStaffOrBoss = sessionManager.getStaffRole() != null

        var query: Query = firestore.collection("notifications")

        query = if (isStaffOrBoss) {

            if (sessionManager.isBoss()) {
                query.whereEqualTo("audience", "staff")
            } else {
                val barberId = sessionManager.getStaffBarberId()
                if (barberId == null) {
                    emptyText.visibility = LinearLayout.VISIBLE
                    return
                }
                query.whereEqualTo("audience", "staff").whereEqualTo("barberId", barberId)
            }

        } else {
            query.whereEqualTo("audience", "customer").whereEqualTo("userId", userId)
        }

        query.get()
            .addOnSuccessListener { snapshot ->

                notificationsContainer.removeAllViews()

                if (snapshot.isEmpty) {
                    emptyText.visibility = LinearLayout.VISIBLE
                    return@addOnSuccessListener
                }

                emptyText.visibility = LinearLayout.GONE

                val documents = snapshot.documents.sortedByDescending {
                    it.getTimestamp("createdAt")
                }

                for (doc in documents) {
                    addNotificationRow(doc)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Could not load notifications: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun addNotificationRow(document: DocumentSnapshot) {

        val title = document.getString("title") ?: ""
        val message = document.getString("message") ?: ""
        val isRead = document.getBoolean("isRead") ?: false

        val row = LinearLayout(this)
        row.orientation = LinearLayout.VERTICAL
        row.setPadding(24, 20, 24, 20)
        row.alpha = if (isRead) 0.5f else 1f

        val titleView = TextView(this)
        titleView.text = title
        titleView.textSize = 16f
        titleView.setTextColor(getColor(android.R.color.white))

        val messageView = TextView(this)
        messageView.text = message
        messageView.textSize = 14f
        messageView.setTextColor(getColor(android.R.color.white))

        row.addView(titleView)
        row.addView(messageView)

        row.setOnClickListener {

            if (!isRead) {
                firestore.collection("notifications")
                    .document(document.id)
                    .update("isRead", true)
                    .addOnSuccessListener {
                        row.alpha = 0.5f
                    }
            }
        }

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, 0, 10)

        notificationsContainer.addView(row, params)
    }
}