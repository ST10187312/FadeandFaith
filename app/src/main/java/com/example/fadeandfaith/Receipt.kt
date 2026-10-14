package com.example.fadeandfaith

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

class Receipt : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore

    private lateinit var serviceText: TextView
    private lateinit var barberText: TextView
    private lateinit var dateText: TextView
    private lateinit var timeText: TextView
    private lateinit var priceText: TextView
    private lateinit var referenceText: TextView
    private lateinit var qrImage: ImageView

    // This is an ImageView in the layout (not a Button). Declaring it as a
    // Button was what made the receipt screen crash on open.
    private lateinit var backButton: ImageView
    private lateinit var doneButton: Button

    private var bookingId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receipt)

        firestore = FirebaseFirestore.getInstance()

        serviceText = findViewById(R.id.serviceText)
        barberText = findViewById(R.id.barberText)
        dateText = findViewById(R.id.dateText)
        timeText = findViewById(R.id.timeText)
        priceText = findViewById(R.id.priceText)
        referenceText = findViewById(R.id.receiptNumberText)
        qrImage = findViewById(R.id.qrImage)

        backButton = findViewById(R.id.backButton)
        doneButton = findViewById(R.id.paymentButton)

        bookingId = intent.getStringExtra("BOOKING_ID") ?: ""

        if (bookingId.isEmpty()) {
            Toast.makeText(
                this,
                "Booking information could not be found.",
                Toast.LENGTH_LONG
            ).show()
            finish()
            return
        }

        loadReceipt()

        backButton.setOnClickListener {
            finish()
        }

        doneButton.setOnClickListener {
            val intent = Intent(this, MyBookings::class.java)
            intent.putExtra("BOOKING_ID", bookingId)
            startActivity(intent)
            finish()
        }
    }

    private fun loadReceipt() {

        firestore.collection("bookings")
            .document(bookingId)
            .get()
            .addOnSuccessListener { document ->

                if (!document.exists()) {
                    Toast.makeText(
                        this,
                        "Booking could not be found.",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                    return@addOnSuccessListener
                }

                val customerName = document.getString("customerName") ?: ""
                val serviceName = document.getString("serviceName") ?: ""
                val barberName = document.getString("barberName") ?: ""
                val bookingDate = document.getString("bookingDate") ?: ""
                val bookingTime = document.getString("bookingTime") ?: ""
                val price = document.getDouble("price") ?: 0.0
                val status = document.getString("status") ?: "pending"

                val reference = bookingId.take(8).uppercase()
                val priceString = String.format("R%.2f", price)
                val statusString = status.replaceFirstChar { it.uppercase() }

                serviceText.text = serviceName
                barberText.text = barberName
                dateText.text = bookingDate
                timeText.text = bookingTime
                priceText.text = priceString

                referenceText.text =
                    if (status.equals("confirmed", ignoreCase = true)) {
                        "Ref: $reference"
                    } else {
                        "Status: $statusString"
                    }

                // The QR code holds the whole receipt as plain text, so any
                // phone camera or QR scanner can read it without internet.
                val receiptText = buildString {
                    appendLine("FADE & FAITH - DIGITAL RECEIPT")
                    appendLine("Ref: $reference")
                    if (customerName.isNotEmpty()) appendLine("Customer: $customerName")
                    appendLine("Service: $serviceName")
                    appendLine("Barber: $barberName")
                    appendLine("Date: $bookingDate")
                    appendLine("Time: $bookingTime")
                    appendLine("Total: $priceString")
                    append("Status: $statusString")
                }

                try {
                    qrImage.setImageBitmap(generateQrBitmap(receiptText, 600))
                } catch (e: Exception) {
                    Toast.makeText(
                        this,
                        "QR code could not be created.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Booking could not be loaded: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
    }

    private fun generateQrBitmap(content: String, size: Int): Bitmap {

        val hints = mapOf(
            EncodeHintType.MARGIN to 1,
            EncodeHintType.CHARACTER_SET to "UTF-8"
        )

        val matrix = QRCodeWriter().encode(
            content,
            BarcodeFormat.QR_CODE,
            size,
            size,
            hints
        )

        val pixels = IntArray(size * size)

        for (y in 0 until size) {
            for (x in 0 until size) {
                pixels[y * size + x] =
                    if (matrix[x, y]) Color.BLACK else Color.WHITE
            }
        }

        return Bitmap.createBitmap(pixels, size, size, Bitmap.Config.ARGB_8888)
    }
}