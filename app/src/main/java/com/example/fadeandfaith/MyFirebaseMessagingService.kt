package com.example.fadeandfaith

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.notification?.title ?: "Fade & Faith"
        val body = message.notification?.body ?: ""

        showNotification(title, body)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        val sessionManager = SessionManager(this)
        val userId = sessionManager.getUserId() ?: return

        if (!sessionManager.isLoggedIn()) {
            return
        }

        val role = sessionManager.getStaffRole()

        val isStaffOrBoss =
            role.equals("STAFF", ignoreCase = true) || role.equals("BOSS", ignoreCase = true)

        if (!isStaffOrBoss) {
            return
        }

        FirebaseFirestore.getInstance()
            .collection("staff_tokens")
            .document(userId)
            .set(mapOf("token" to token))
    }

    private fun showNotification(title: String, body: String) {

        val channelId = "bookings_channel"

        val manager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    channelId,
                    "Bookings",
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
