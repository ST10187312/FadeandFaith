package com.example.fadeandfaith

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

/**
 * Customers choose how they will pay when they arrive. Payment happens at
 * the barbershop, so the choice is saved and attached to each booking.
 */
class PaymentMethodActivity : AppCompatActivity() {

    private lateinit var backButton: ImageView

    private lateinit var cardOption: LinearLayout
    private lateinit var googlePayOption: LinearLayout
    private lateinit var applePayOption: LinearLayout
    private lateinit var cashOption: LinearLayout

    private lateinit var cardIndicator: ImageView
    private lateinit var googlePayIndicator: ImageView
    private lateinit var applePayIndicator: ImageView
    private lateinit var cashIndicator: ImageView

    private lateinit var addCardButton: MaterialButton
    private lateinit var saveButton: MaterialButton

    private lateinit var savedCardText: TextView

    private val cashLabel = "Cash at the barbershop"
    private val cardLabel = "Card at the barbershop"

    private var selectedPaymentMethod = "Cash at the barbershop"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_payment_method)

        initializeViews()

        selectedPaymentMethod = getSharedPreferences("ff_prefs", MODE_PRIVATE)
            .getString("payment_method", cashLabel) ?: cashLabel

        setupPaymentOptions()
        setupClickListeners()
    }

    private fun initializeViews() {

        backButton = findViewById(R.id.backButton)

        cardOption = findViewById(R.id.cardOption)
        googlePayOption = findViewById(R.id.googlePayOption)
        applePayOption = findViewById(R.id.applePayOption)
        cashOption = findViewById(R.id.cashOption)

        cardIndicator = findViewById(R.id.cardIndicator)
        googlePayIndicator = findViewById(R.id.googlePayIndicator)
        applePayIndicator = findViewById(R.id.applePayIndicator)
        cashIndicator = findViewById(R.id.cashIndicator)

        addCardButton = findViewById(R.id.addCardButton)
        saveButton = findViewById(R.id.saveButton)

        savedCardText = findViewById(R.id.savedCardText)

        // Cards are paid at the shop, so there is nothing to add here, and
        // online wallets are not offered yet.
        addCardButton.visibility = View.GONE
        googlePayOption.visibility = View.GONE
        applePayOption.visibility = View.GONE

        savedCardText.text = "Pay by card when you arrive"
    }

    private fun setupPaymentOptions() {

        selectPaymentMethod(selectedPaymentMethod)

        cardOption.setOnClickListener {
            selectPaymentMethod(cardLabel)
        }

        cashOption.setOnClickListener {
            selectPaymentMethod(cashLabel)
        }
    }

    private fun selectPaymentMethod(method: String) {

        selectedPaymentMethod = method

        cardIndicator.alpha = if (method == cardLabel) 1f else 0.25f
        cashIndicator.alpha = if (method == cashLabel) 1f else 0.25f
        googlePayIndicator.alpha = 0.25f
        applePayIndicator.alpha = 0.25f
    }

    private fun setupClickListeners() {

        backButton.setOnClickListener {
            finish()
        }

        saveButton.setOnClickListener {

            getSharedPreferences("ff_prefs", MODE_PRIVATE)
                .edit()
                .putString("payment_method", selectedPaymentMethod)
                .apply()

            Toast.makeText(
                this,
                "$selectedPaymentMethod selected.",
                Toast.LENGTH_SHORT
            ).show()

            finish()
        }
    }
}