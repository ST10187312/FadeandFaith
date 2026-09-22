package com.example.fadeandfaith

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class StaffPrices : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private var barberId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_staff_prices)

        db = DatabaseHelper(this)

        findViewById<Button>(R.id.backButton).setOnClickListener {
            finish()
        }

        loadStaffBarber()

        findViewById<Button>(R.id.savePricesButton).setOnClickListener {
            savePrices()
        }
    }

    private fun loadStaffBarber() {

        val session = SessionManager(this)
        val staffUserId = session.getUserId()

        barberId = db.getBarberIdForStaff(staffUserId)

        if (barberId == null) {
            Toast.makeText(
                this,
                "Staff profile is not linked to a barber",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun savePrices() {

        val id = barberId

        if (id == null) {
            Toast.makeText(
                this,
                "Barber profile not found",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val haircut =
            findViewById<EditText>(R.id.haircutPriceInput)
                .text.toString()

        val shave =
            findViewById<EditText>(R.id.shavePriceInput)
                .text.toString()

        val facial =
            findViewById<EditText>(R.id.facialPriceInput)
                .text.toString()

        val premium =
            findViewById<EditText>(R.id.premiumPriceInput)
                .text.toString()

        val prices = listOf(
            haircut to 1L,
            shave to 2L,
            facial to 3L,
            premium to 4L
        )

        for ((value, serviceId) in prices) {

            val price = value.toDoubleOrNull()

            if (price == null || price < 0) {
                Toast.makeText(
                    this,
                    "Please enter valid prices",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            db.updateServicePrice(
                id,
                serviceId,
                price
            )
        }

        Toast.makeText(
            this,
            "Service prices updated",
            Toast.LENGTH_SHORT
        ).show()
    }
}