package com.example.fadeandfaith

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class Home : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var sessionManager: SessionManager

    private lateinit var notificationButton: ImageView
    private lateinit var profileButton: ImageView
    private lateinit var seeServicesButton: TextView
    private lateinit var seeBarbersButton: TextView

    private lateinit var navHome: LinearLayout
    private lateinit var navServices: LinearLayout
    private lateinit var navBooking: LinearLayout
    private lateinit var navProfile: LinearLayout

    private var notificationListener: ListenerRegistration? = null
    private var isFirstSnapshot = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_home)

        requestNotificationPermissionIfNeeded()

        firestore = FirebaseFirestore.getInstance()
        sessionManager = SessionManager(this)

        initializeViews()
        setupClickListeners()
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

        val userId = sessionManager.getUserId() ?: return

        isFirstSnapshot = true

        notificationListener = firestore.collection("notifications")
            .whereEqualTo("audience", "customer")
            .whereEqualTo("userId", userId)
            .whereEqualTo("isRead", false)
            .addSnapshotListener { snapshot, error ->

                if (error != null || snapshot == null) {
                    return@addSnapshotListener
                }

                if (isFirstSnapshot) {
                    isFirstSnapshot = false
                    return@addSnapshotListener
                }

                for (change in snapshot.documentChanges) {

                    if (change.type == DocumentChange.Type.ADDED) {

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

    private fun initializeViews() {

        notificationButton = findViewById(R.id.notificationButton)
        profileButton = findViewById(R.id.profileButton)
        seeServicesButton = findViewById(R.id.seeServicesButton)
        seeBarbersButton = findViewById(R.id.seeBarbersButton)

        navHome = findViewById(R.id.navHome)
        navServices = findViewById(R.id.navServices)
        navBooking = findViewById(R.id.navBooking)
        navProfile = findViewById(R.id.navProfile)
    }

    private fun setupClickListeners() {

        notificationButton.setOnClickListener {
            startActivity(Intent(this, Notifications::class.java))
        }

        profileButton.setOnClickListener {
            startActivity(Intent(this, Profile::class.java))
        }

        seeServicesButton.setOnClickListener {
            startActivity(Intent(this, Services::class.java))
        }

        seeBarbersButton.setOnClickListener {
            startActivity(Intent(this, Barbers::class.java))
        }

        navHome.setOnClickListener {
            // Already on Home
        }

        navServices.setOnClickListener {
            startActivity(Intent(this, Services::class.java))
        }

        navBooking.setOnClickListener {
            startActivity(Intent(this, Booking::class.java))
        }

        navProfile.setOnClickListener {
            startActivity(Intent(this, Profile::class.java))
        }
    }
}