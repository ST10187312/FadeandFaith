package com.example.fadeandfaith

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Customers browse hairstyles the barbershop has done, with the barber,
 * location, how long it took and the price.
 */
class Gallery : AppCompatActivity() {

    private lateinit var galleryContainer: LinearLayout
    private lateinit var emptyText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_gallery)

        galleryContainer = findViewById(R.id.galleryContainer)
        emptyText = findViewById(R.id.emptyText)

        findViewById<ImageView>(R.id.backButton).setOnClickListener { finish() }

        loadGallery()
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun loadGallery() {

        FirebaseFirestore.getInstance()
            .collection("hairstyles")
            .get()
            .addOnSuccessListener { snapshot ->

                galleryContainer.removeAllViews()

                if (snapshot.isEmpty) {
                    emptyText.visibility = View.VISIBLE
                    return@addOnSuccessListener
                }

                emptyText.visibility = View.GONE

                val docs = snapshot.documents.sortedByDescending {
                    it.getTimestamp("createdAt")?.seconds ?: 0L
                }

                for (doc in docs.take(40)) {

                    val card = LinearLayout(this).apply {
                        orientation = LinearLayout.VERTICAL
                        setBackgroundResource(R.drawable.cardoutline)
                        setPadding(dp(14), dp(14), dp(14), dp(18))
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply { topMargin = dp(18) }
                    }

                    card.addView(ImageView(this).apply {
                        scaleType = ImageView.ScaleType.CENTER_CROP
                        setImageBitmap(ImageStore.base64ToBitmap(doc.getString("photoBase64")))
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            dp(260)
                        )
                    })

                    card.addView(TextView(this).apply {
                        text = doc.getString("title") ?: ""
                        setTextColor(0xFFE8C45E.toInt())
                        textSize = 20f
                        typeface = Typeface.create("serif", Typeface.BOLD)
                        setPadding(0, dp(14), 0, dp(4))
                    })

                    card.addView(TextView(this).apply {
                        text = "By " + (doc.getString("barberName") ?: "")
                        setTextColor(0xFFFFFFFF.toInt())
                        textSize = 14f
                    })

                    val price = doc.getDouble("price") ?: 0.0
                    val minutes = doc.getLong("durationMinutes") ?: 0L

                    card.addView(TextView(this).apply {
                        text = "Location: " + (doc.getString("location") ?: "") +
                                "\nTime taken: $minutes min" +
                                "\nPrice: R" + String.format("%.0f", price)
                        setTextColor(0xFF9A9A9A.toInt())
                        textSize = 13f
                        setLineSpacing(dp(3).toFloat(), 1f)
                        setPadding(0, dp(8), 0, 0)
                    })

                    galleryContainer.addView(card)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Could not load gallery: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}