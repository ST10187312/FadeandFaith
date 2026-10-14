package com.example.fadeandfaith

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Staff upload a photo of a finished hairstyle, with where it was done,
 * how long it took and the price. Customers see these in the Gallery.
 */
class StaffHairstyles : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var sessionManager: SessionManager

    private lateinit var photoImage: ImageView
    private lateinit var choosePhotoButton: MaterialButton
    private lateinit var titleInput: EditText
    private lateinit var locationInput: EditText
    private lateinit var durationInput: EditText
    private lateinit var priceInput: EditText
    private lateinit var saveButton: MaterialButton
    private lateinit var myStylesContainer: LinearLayout

    private var photo: String? = null

    private val photoPicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->

            if (uri == null) return@registerForActivityResult

            val encoded = ImageStore.uriToBase64(this, uri)

            if (encoded == null) {
                Toast.makeText(this, "That photo could not be used.", Toast.LENGTH_LONG).show()
                return@registerForActivityResult
            }

            photo = encoded
            photoImage.setImageBitmap(ImageStore.base64ToBitmap(encoded))
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_staff_hairstyles)

        firestore = FirebaseFirestore.getInstance()
        sessionManager = SessionManager(this)

        photoImage = findViewById(R.id.photoImage)
        choosePhotoButton = findViewById(R.id.choosePhotoButton)
        titleInput = findViewById(R.id.titleInput)
        locationInput = findViewById(R.id.locationInput)
        durationInput = findViewById(R.id.durationInput)
        priceInput = findViewById(R.id.priceInput)
        saveButton = findViewById(R.id.saveButton)
        myStylesContainer = findViewById(R.id.myStylesContainer)

        findViewById<ImageView>(R.id.backButton).setOnClickListener { finish() }
        choosePhotoButton.setOnClickListener { photoPicker.launch("image/*") }
        saveButton.setOnClickListener { saveHairstyle() }

        loadMyStyles()
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun saveHairstyle() {

        val uid = sessionManager.getUserId()

        if (uid == null) {
            Toast.makeText(this, "Please log in again.", Toast.LENGTH_LONG).show()
            return
        }

        val title = titleInput.text.toString().trim()
        val location = locationInput.text.toString().trim()
        val duration = durationInput.text.toString().trim().toLongOrNull()
        val price = priceInput.text.toString().trim().toDoubleOrNull()

        if (photo == null) {
            Toast.makeText(this, "Please choose a photo.", Toast.LENGTH_SHORT).show()
            return
        }

        if (title.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Please add a name and a location.", Toast.LENGTH_SHORT).show()
            return
        }

        if (duration == null || duration <= 0 || price == null || price < 0) {
            Toast.makeText(this, "Please enter a valid time and price.", Toast.LENGTH_SHORT).show()
            return
        }

        saveButton.isEnabled = false

        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { userDoc ->

                val barberId = sessionManager.getStaffBarberId() ?: ""
                val barberName =
                    if (barberId.isNotEmpty()) barberId
                    else userDoc.getString("name") ?: "Fade & Faith"

                val data = hashMapOf(
                    "ownerUid" to uid,
                    "barberId" to barberId,
                    "barberName" to barberName,
                    "title" to title,
                    "location" to location,
                    "durationMinutes" to duration,
                    "price" to price,
                    "photoBase64" to photo,
                    "createdAt" to FieldValue.serverTimestamp()
                )

                firestore.collection("hairstyles").add(data)
                    .addOnSuccessListener {
                        saveButton.isEnabled = true
                        Toast.makeText(this, "Hairstyle added to the gallery.", Toast.LENGTH_SHORT).show()

                        photo = null
                        photoImage.setImageDrawable(null)
                        titleInput.setText("")
                        locationInput.setText("")
                        durationInput.setText("")
                        priceInput.setText("")

                        loadMyStyles()
                    }
                    .addOnFailureListener { e ->
                        saveButton.isEnabled = true
                        Toast.makeText(this, "Could not save: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { e ->
                saveButton.isEnabled = true
                Toast.makeText(this, "Could not save: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun loadMyStyles() {

        val uid = sessionManager.getUserId() ?: return

        firestore.collection("hairstyles")
            .whereEqualTo("ownerUid", uid)
            .get()
            .addOnSuccessListener { snapshot ->

                myStylesContainer.removeAllViews()

                val docs = snapshot.documents.sortedByDescending {
                    it.getTimestamp("createdAt")?.seconds ?: 0L
                }

                for (doc in docs) {

                    val row = LinearLayout(this).apply {
                        orientation = LinearLayout.HORIZONTAL
                        gravity = Gravity.CENTER_VERTICAL
                        setBackgroundResource(R.drawable.cardoutline)
                        setPadding(dp(12), dp(12), dp(12), dp(12))
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply { topMargin = dp(10) }
                    }

                    row.addView(ImageView(this).apply {
                        scaleType = ImageView.ScaleType.CENTER_CROP
                        setImageBitmap(ImageStore.base64ToBitmap(doc.getString("photoBase64")))
                        layoutParams = LinearLayout.LayoutParams(dp(64), dp(64))
                    })

                    row.addView(TextView(this).apply {
                        text = (doc.getString("title") ?: "") +
                                "\nR" + String.format("%.0f", doc.getDouble("price") ?: 0.0)
                        setTextColor(0xFFFFFFFF.toInt())
                        textSize = 14f
                        setPadding(dp(14), 0, dp(8), 0)
                        layoutParams = LinearLayout.LayoutParams(
                            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                        )
                    })

                    row.addView(TextView(this).apply {
                        text = "REMOVE"
                        setTextColor(0xFFB85C5C.toInt())
                        textSize = 11f
                        setTypeface(typeface, Typeface.BOLD)
                        setPadding(dp(10), dp(10), dp(10), dp(10))
                        setOnClickListener {
                            firestore.collection("hairstyles").document(doc.id).delete()
                                .addOnSuccessListener { loadMyStyles() }
                        }
                    })

                    myStylesContainer.addView(row)
                }
            }
    }
}