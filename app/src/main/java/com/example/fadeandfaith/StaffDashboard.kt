package com.example.fadeandfaith

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class StaffDashboard : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var sessionManager: SessionManager

    private lateinit var staffNameText: TextView
    private lateinit var appointmentsText: TextView

    private lateinit var appointmentsButton: LinearLayout
    private lateinit var availabilityButton: LinearLayout
    private lateinit var pricesButton: LinearLayout
    private lateinit var settingsButton: LinearLayout
    private lateinit var logoutButton: LinearLayout

    private var notificationListener: ListenerRegistration? = null
    private var isFirstSnapshot = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_staff_dasboard)

        requestNotificationPermissionIfNeeded()

        firestore = FirebaseFirestore.getInstance()
        sessionManager = SessionManager(this)

        initializeViews()
        loadDashboard()
        setupButtons()
    }

    override fun onStart() {
        super.onStart()
        startListeningForNotifications()
    }

    override fun onStop() {
        super.onStop()
        notificationListener?.remove()
        notificationListener = null
    }

    private fun startListeningForNotifications() {

        isFirstSnapshot = true

        var query: Query = firestore.collection("notifications")
            .whereEqualTo("audience", "staff")
            .whereEqualTo("isRead", false)

        if (!sessionManager.isBoss()) {

            val barberId = sessionManager.getStaffBarberId() ?: return

            query = query.whereEqualTo("barberId", barberId)
        }

        notificationListener = query.addSnapshotListener { snapshot, error ->

            if (error != null || snapshot == null) {
                return@addSnapshotListener
            }

            // The first snapshot is just "here's everything that currently
            // matches" -- skip it, or every unread notification from the
            // past would pop up again every time this screen opens. Only
            // react to genuinely new ones added after that.
            if (isFirstSnapshot) {
                isFirstSnapshot = false
                return@addSnapshotListener
            }

            for (change in snapshot.documentChanges) {

                if (change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {

                    val title = change.document.getString("title") ?: "Fade & Faith"
                    val message = change.document.getString("message") ?: ""

                    showLocalNotification(title, message)
                }
            }
        }
    }

    private fun showLocalNotification(title: String, message: String) {

        val channelId = "bookings_channel"

        val manager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(channelId, "Bookings", NotificationManager.IMPORTANCE_HIGH)
            )
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun requestNotificationPermissionIfNeeded() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
    }

    private fun initializeViews() {

        staffNameText = findViewById(R.id.staffNameText)
        appointmentsText = findViewById(R.id.appointmentsText)

        appointmentsButton = findViewById(R.id.appointmentsButton)
        availabilityButton = findViewById(R.id.availabilityButton)
        pricesButton = findViewById(R.id.pricesButton)
        settingsButton = findViewById(R.id.settingsButton)
        logoutButton = findViewById(R.id.logoutButton)
    }

    private fun loadDashboard() {

        val userId = sessionManager.getUserId()

        if (userId == null) {
            staffNameText.text = "Staff"
            appointmentsText.text = "0"
            return
        }

        firestore.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                staffNameText.text = document.getString("name") ?: "Staff"
            }

        var bookingsQuery: Query = firestore.collection("bookings")

        if (!sessionManager.isBoss()) {

            val barberId = sessionManager.getStaffBarberId()

            if (barberId == null) {
                appointmentsText.text = "0"
                return
            }

            bookingsQuery = bookingsQuery.whereEqualTo("barberId", barberId)
        }

        bookingsQuery.get()
            .addOnSuccessListener { snapshot ->

                val count = snapshot.documents.count { doc ->
                    val status = (doc.getString("status") ?: "").uppercase()
                    status == "CONFIRMED" || status == "PENDING"
                }

                appointmentsText.text = count.toString()
            }
            .addOnFailureListener {
                appointmentsText.text = "0"
            }
    }

    private fun setupButtons() {

        appointmentsButton.setOnClickListener {
            startActivity(Intent(this, StaffAppointments::class.java))
        }

        availabilityButton.setOnClickListener {
            startActivity(Intent(this, StaffAvailability::class.java))
        }

        pricesButton.setOnClickListener {
            startActivity(Intent(this, StaffPrices::class.java))
        }

        settingsButton.setOnClickListener {
            startActivity(Intent(this, Settings::class.java))
        }

        logoutButton.setOnClickListener {
            SessionManager(this).logout()
            startActivity(Intent(this, Login::class.java))
            finishAffinity()
        }
    }
}