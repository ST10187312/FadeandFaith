package com.example.fadeandfaith

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

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

    private var selectedPaymentMethod = "Cash"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_payment_method)

        initializeViews()
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
    }

    private fun setupPaymentOptions() {

        selectPaymentMethod("Cash")

        cardOption.setOnClickListener {
            selectPaymentMethod("Card")
        }

        googlePayOption.setOnClickListener {
            selectPaymentMethod("Google Pay")
        }

        applePayOption.setOnClickListener {
            selectPaymentMethod("Apple Pay")
        }

        cashOption.setOnClickListener {
            selectPaymentMethod("Cash")
        }
    }

    private fun selectPaymentMethod(method: String) {

        selectedPaymentMethod = method

        cardIndicator.alpha =
            if (method == "Card") 1f else 0.25f

        googlePayIndicator.alpha =
            if (method == "Google Pay") 1f else 0.25f

        applePayIndicator.alpha =
            if (method == "Apple Pay") 1f else 0.25f

        cashIndicator.alpha =
            if (method == "Cash") 1f else 0.25f
    }

    private fun setupClickListeners() {

        backButton.setOnClickListener {
            finish()
        }

        addCardButton.setOnClickListener {

            Toast.makeText(
                this,
                "Card setup will be connected to the payment service.",
                Toast.LENGTH_SHORT
            ).show()
        }

        saveButton.setOnClickListener {

            Toast.makeText(
                this,
                "$selectedPaymentMethod selected.",
                Toast.LENGTH_SHORT
            ).show()

            finish()
        }
    }
}