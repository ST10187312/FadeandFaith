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
import androidx.activity.result.contract.ActivityResultContracts
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
    private lateinit var galleryButton: LinearLayout

    private lateinit var userNameText: TextView

    private lateinit var serviceNameViews: List<TextView>
    private lateinit var servicePriceViews: List<TextView>

    private lateinit var barberInitialViews: List<TextView>
    private lateinit var barberNameViews: List<TextView>
    private lateinit var barberRoleViews: List<TextView>

    private lateinit var navHome: LinearLayout
    private lateinit var navServices: LinearLayout
    private lateinit var navBooking: LinearLayout
    private lateinit var navProfile: LinearLayout

    private var notificationListener: ListenerRegistration? = null
    private var isFirstSnapshot = true

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_home)

        firestore = FirebaseFirestore.getInstance()
        sessionManager = SessionManager(this)

        initializeViews()
        setupClickListeners()

        requestNotificationPermissionIfNeeded()
    }

    override fun onStart() {
        super.onStart()

        startListeningForNotifications()

        // Reloaded every time Home becomes visible, so a price change
        // made by the boss shows up without restarting the app.
        loadUserName()
        loadFeaturedServices()
        loadFeaturedBarbers()
    }

    override fun onStop() {
        super.onStop()
        notificationListener?.remove()
        notificationListener = null
    }

    private fun requestNotificationPermissionIfNeeded() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }
        }
    }

    private fun loadUserName() {

        val userId = sessionManager.getUserId() ?: return

        firestore.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->

                val name = document.getString("name") ?: ""

                userNameText.text = name
                    .split(" ")
                    .joinToString(" ") { word ->
                        word.replaceFirstChar { it.uppercase() }
                    }
            }
    }

    private fun loadFeaturedServices() {

        firestore.collection("services")
            .whereEqualTo("active", true)
            .get()
            .addOnSuccessListener { snapshot ->

                // Cheapest first.
                val services = snapshot.documents.sortedBy {
                    it.getDouble("price") ?: 0.0
                }

                for (i in serviceNameViews.indices) {

                    val doc = services.getOrNull(i)

                    if (doc == null) {
                        serviceNameViews[i].text = ""
                        servicePriceViews[i].text = ""
                        continue
                    }

                    serviceNameViews[i].text = doc.getString("name") ?: ""
                    servicePriceViews[i].text = formatPrice(doc.getDouble("price") ?: 0.0)
                }
            }
    }

    private fun loadFeaturedBarbers() {

        firestore.collection("barbers")
            .whereEqualTo("active", true)
            .get()
            .addOnSuccessListener { snapshot ->

                // Highest rated first.
                val barbers = snapshot.documents.sortedByDescending {
                    it.getDouble("rating") ?: 0.0
                }

                for (i in barberNameViews.indices) {

                    val doc = barbers.getOrNull(i)

                    if (doc == null) {
                        barberInitialViews[i].text = ""
                        barberNameViews[i].text = ""
                        barberRoleViews[i].text = ""
                        continue
                    }

                    val name = doc.getString("name") ?: doc.id

                    barberInitialViews[i].text = initialsFor(name)
                    barberNameViews[i].text = name
                    barberRoleViews[i].text = doc.getString("role") ?: ""
                }
            }
    }

    private fun formatPrice(price: Double): String {
        return if (price % 1.0 == 0.0) {
            "R${price.toInt()}"
        } else {
            String.format("R%.2f", price)
        }
    }

    private fun initialsFor(name: String): String {

        val parts = name.trim().split(" ").filter { it.isNotEmpty() }

        return when {
            parts.size >= 2 -> "${parts[0][0]}${parts[1][0]}".uppercase()
            parts.size == 1 -> parts[0].take(2).uppercase()
            else -> ""
        }
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
        galleryButton = findViewById(R.id.galleryButton)

        userNameText = findViewById(R.id.userName)

        serviceNameViews = listOf(
            R.id.serviceName1, R.id.serviceName2, R.id.serviceName3, R.id.serviceName4
        ).map { findViewById<TextView>(it) }

        servicePriceViews = listOf(
            R.id.servicePrice1, R.id.servicePrice2, R.id.servicePrice3, R.id.servicePrice4
        ).map { findViewById<TextView>(it) }

        barberInitialViews = listOf(
            R.id.barberInitials1, R.id.barberInitials2, R.id.barberInitials3
        ).map { findViewById<TextView>(it) }

        barberNameViews = listOf(
            R.id.barberName1, R.id.barberName2, R.id.barberName3
        ).map { findViewById<TextView>(it) }

        barberRoleViews = listOf(
            R.id.barberRole1, R.id.barberRole2, R.id.barberRole3
        ).map { findViewById<TextView>(it) }

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

        galleryButton.setOnClickListener {
            startActivity(Intent(this, Gallery::class.java))
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