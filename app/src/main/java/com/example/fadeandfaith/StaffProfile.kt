package com.example.fadeandfaith

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class StaffProfile : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var sessionManager: SessionManager

    private lateinit var backButton: ImageView
    private lateinit var photoImage: ImageView
    private lateinit var choosePhotoButton: MaterialButton
    private lateinit var descriptionInput: EditText
    private lateinit var saveButton: MaterialButton

    private var barberId = ""
    private var newPhoto: String? = null

    private val photoPicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->

            if (uri == null) return@registerForActivityResult

            val encoded = ImageStore.uriToBase64(this, uri)

            if (encoded == null) {
                Toast.makeText(this, "That photo could not be used.", Toast.LENGTH_LONG).show()
                return@registerForActivityResult
            }

            newPhoto = encoded
            photoImage.setImageBitmap(ImageStore.base64ToBitmap(encoded))
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_staff_profile)

        firestore = FirebaseFirestore.getInstance()
        sessionManager = SessionManager(this)

        backButton = findViewById(R.id.backButton)
        photoImage = findViewById(R.id.photoImage)
        choosePhotoButton = findViewById(R.id.choosePhotoButton)
        descriptionInput = findViewById(R.id.descriptionInput)
        saveButton = findViewById(R.id.saveButton)

        barberId = sessionManager.getStaffBarberId() ?: ""

        if (barberId.isEmpty()) {
            Toast.makeText(
                this,
                "This account is not linked to a barber profile.",
                Toast.LENGTH_LONG
            ).show()
            finish()
            return
        }

        backButton.setOnClickListener { finish() }
        choosePhotoButton.setOnClickListener { photoPicker.launch("image/*") }
        saveButton.setOnClickListener { saveProfile() }

        loadProfile()
    }

    private fun loadProfile() {

        firestore.collection("barbers").document(barberId).get()
            .addOnSuccessListener { document ->

                descriptionInput.setText(document.getString("description") ?: "")

                val bitmap = ImageStore.base64ToBitmap(document.getString("photoBase64"))

                if (bitmap != null) {
                    photoImage.setImageBitmap(bitmap)
                }
            }
    }

    private fun saveProfile() {

        val updates = hashMapOf<String, Any>(
            "description" to descriptionInput.text.toString().trim()
        )

        newPhoto?.let { updates["photoBase64"] = it }

        saveButton.isEnabled = false

        firestore.collection("barbers").document(barberId)
            .set(updates, SetOptions.merge())
            .addOnSuccessListener {
                saveButton.isEnabled = true
                Toast.makeText(this, "Profile saved.", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                saveButton.isEnabled = true
                Toast.makeText(this, "Could not save: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}