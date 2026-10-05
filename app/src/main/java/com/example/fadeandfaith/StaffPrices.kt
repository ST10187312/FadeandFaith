package com.example.fadeandfaith

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class StaffPrices : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var sessionManager: SessionManager

    private lateinit var haircutPriceInput: EditText
    private lateinit var shavePriceInput: EditText
    private lateinit var facialPriceInput: EditText
    private lateinit var premiumPriceInput: EditText
    private lateinit var pendingRequestsContainer: LinearLayout

    // Must match the document names used in Services.kt / Booking.kt.
    private val serviceNames = mutableMapOf(
        "haircut_beard" to "Haircut & Beard",
        "shave_haircut" to "Shave & Haircut",
        "facial_shave" to "Facial & Shave",
        "premium_experience" to "Premium Experience"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_staff_prices)

        firestore = FirebaseFirestore.getInstance()
        sessionManager = SessionManager(this)

        haircutPriceInput = findViewById(R.id.haircutPriceInput)
        shavePriceInput = findViewById(R.id.shavePriceInput)
        facialPriceInput = findViewById(R.id.facialPriceInput)
        premiumPriceInput = findViewById(R.id.premiumPriceInput)
        pendingRequestsContainer = findViewById(R.id.pendingRequestsContainer)

        findViewById<Button>(R.id.backButton).setOnClickListener {
            finish()
        }

        loadCurrentPrices()

        if (sessionManager.isBoss()) {
            loadPendingRequests()
        }

        findViewById<Button>(R.id.savePricesButton).setOnClickListener {
            if (sessionManager.isBoss()) {
                saveDirectly()
            } else {
                submitPriceRequests()
            }
        }
    }

    private fun loadCurrentPrices() {

        for (serviceId in serviceNames.keys) {

            firestore.collection("services")
                .document(serviceId)
                .get()
                .addOnSuccessListener { document ->

                    val name = document.getString("name")
                    if (name != null) {
                        serviceNames[serviceId] = name
                    }

                    val price = document.getDouble("price") ?: return@addOnSuccessListener
                    val priceText = String.format("%.2f", price)

                    when (serviceId) {
                        "haircut_beard" -> haircutPriceInput.setText(priceText)
                        "shave_haircut" -> shavePriceInput.setText(priceText)
                        "facial_shave" -> facialPriceInput.setText(priceText)
                        "premium_experience" -> premiumPriceInput.setText(priceText)
                    }
                }
        }
    }

    private fun collectInputPrices(): Map<String, Double>? {

        val inputs = mapOf(
            "haircut_beard" to haircutPriceInput.text.toString(),
            "shave_haircut" to shavePriceInput.text.toString(),
            "facial_shave" to facialPriceInput.text.toString(),
            "premium_experience" to premiumPriceInput.text.toString()
        )

        val parsedPrices = mutableMapOf<String, Double>()

        for ((serviceId, value) in inputs) {

            val price = value.toDoubleOrNull()

            if (price == null || price < 0) {
                Toast.makeText(this, "Please enter valid prices", Toast.LENGTH_SHORT).show()
                return null
            }

            parsedPrices[serviceId] = price
        }

        return parsedPrices
    }

    /**
     * Boss only: updates service prices immediately, no approval needed.
     */
    private fun saveDirectly() {

        val parsedPrices = collectInputPrices() ?: return

        var completedCount = 0
        var hadFailure = false
        val totalCount = parsedPrices.size

        for ((serviceId, price) in parsedPrices) {

            firestore.collection("services")
                .document(serviceId)
                .update("price", price)
                .addOnCompleteListener { task ->

                    completedCount++
                    if (!task.isSuccessful) hadFailure = true

                    if (completedCount == totalCount) {
                        Toast.makeText(
                            this,
                            if (hadFailure) "Some prices could not be updated."
                            else "Service prices updated",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    /**
     * Regular staff: instead of changing anything, this creates a request
     * for the boss to approve.
     */
    private fun submitPriceRequests() {

        val parsedPrices = collectInputPrices() ?: return
        val userId = sessionManager.getUserId()

        if (userId == null) {
            Toast.makeText(this, "Please log in again.", Toast.LENGTH_LONG).show()
            return
        }

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { userDoc ->

                val staffName = userDoc.getString("name") ?: "Staff member"

                var completedCount = 0
                val totalCount = parsedPrices.size

                for ((serviceId, price) in parsedPrices) {

                    val requestData = hashMapOf(
                        "serviceId" to serviceId,
                        "serviceName" to (serviceNames[serviceId] ?: serviceId),
                        "requestedPrice" to price,
                        "requestedByUid" to userId,
                        "requestedByName" to staffName,
                        "status" to "pending",
                        "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                    )

                    firestore.collection("priceRequests")
                        .add(requestData)
                        .addOnCompleteListener {

                            completedCount++

                            if (completedCount == totalCount) {
                                Toast.makeText(
                                    this,
                                    "Price changes sent to the boss for approval.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Could not submit request: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    /**
     * Boss only: shows every pending request with Approve/Deny buttons.
     */
    private fun loadPendingRequests() {

        pendingRequestsContainer.removeAllViews()

        firestore.collection("priceRequests")
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { snapshot ->

                if (snapshot.isEmpty) {
                    return@addOnSuccessListener
                }

                val header = TextView(this)
                header.text = "Pending price requests"
                header.textSize = 16f
                header.setPadding(0, 30, 0, 10)
                pendingRequestsContainer.addView(header)

                for (doc in snapshot.documents) {
                    addRequestCard(doc)
                }
            }
    }

    private fun addRequestCard(document: DocumentSnapshot) {

        val requestId = document.id
        val serviceName = document.getString("serviceName") ?: ""
        val requestedPrice = document.getDouble("requestedPrice") ?: 0.0
        val requestedByName = document.getString("requestedByName") ?: "Staff member"

        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.setPadding(20, 20, 20, 20)

        val details = TextView(this)
        details.text =
            "$serviceName -> R${String.format("%.2f", requestedPrice)}\n" +
                    "Requested by $requestedByName"
        details.textSize = 14f

        card.addView(details)

        val buttonRow = LinearLayout(this)
        buttonRow.orientation = LinearLayout.HORIZONTAL

        val approveButton = Button(this)
        approveButton.text = "Approve"

        val denyButton = Button(this)
        denyButton.text = "Deny"

        buttonRow.addView(approveButton)
        buttonRow.addView(denyButton)
        card.addView(buttonRow)

        approveButton.setOnClickListener {
            approveRequest(requestId, document.getString("serviceId") ?: "", requestedPrice)
        }

        denyButton.setOnClickListener {
            denyRequest(requestId)
        }

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, 0, 16)

        pendingRequestsContainer.addView(card, params)
    }

    private fun approveRequest(requestId: String, serviceId: String, newPrice: Double) {

        firestore.collection("services").document(serviceId)
            .update("price", newPrice)
            .addOnSuccessListener {

                firestore.collection("priceRequests").document(requestId)
                    .update("status", "approved")
                    .addOnSuccessListener {
                        Toast.makeText(this, "Price approved and updated.", Toast.LENGTH_SHORT).show()
                        loadCurrentPrices()
                        loadPendingRequests()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Could not approve: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun denyRequest(requestId: String) {

        firestore.collection("priceRequests").document(requestId)
            .update("status", "denied")
            .addOnSuccessListener {
                Toast.makeText(this, "Request denied.", Toast.LENGTH_SHORT).show()
                loadPendingRequests()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Could not deny: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}