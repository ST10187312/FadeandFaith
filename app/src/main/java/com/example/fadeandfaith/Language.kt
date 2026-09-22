package com.example.fadeandfaith

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class Language : AppCompatActivity() {

    private lateinit var backButton: ImageView

    private lateinit var englishOption: LinearLayout
    private lateinit var isizuluOption: LinearLayout
    private lateinit var afrikaansOption: LinearLayout
    private lateinit var isixhosaOption: LinearLayout
    private lateinit var sepediOption: LinearLayout

    private lateinit var englishIndicator: ImageView
    private lateinit var isizuluIndicator: ImageView
    private lateinit var afrikaansIndicator: ImageView
    private lateinit var isixhosaIndicator: ImageView
    private lateinit var sepediIndicator: ImageView

    private lateinit var selectedLanguageText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_language)

        initializeViews()
        setupLanguageOptions()
        setupClickListeners()
    }

    private fun initializeViews() {

        backButton = findViewById(R.id.backButton)

        englishOption = findViewById(R.id.englishOption)
        isizuluOption = findViewById(R.id.isizuluOption)
        afrikaansOption = findViewById(R.id.afrikaansOption)
        isixhosaOption = findViewById(R.id.isixhosaOption)
        sepediOption = findViewById(R.id.sepediOption)

        englishIndicator = findViewById(R.id.englishIndicator)
        isizuluIndicator = findViewById(R.id.isizuluIndicator)
        afrikaansIndicator = findViewById(R.id.afrikaansIndicator)
        isixhosaIndicator = findViewById(R.id.isixhosaIndicator)
        sepediIndicator = findViewById(R.id.sepediIndicator)

        selectedLanguageText = findViewById(R.id.selectedLanguageText)
    }

    private fun setupLanguageOptions() {

        selectLanguage("English")

        englishOption.setOnClickListener {
            selectLanguage("English")
        }

        isizuluOption.setOnClickListener {
            selectLanguage("isiZulu")
        }

        afrikaansOption.setOnClickListener {
            selectLanguage("Afrikaans")
        }

        isixhosaOption.setOnClickListener {
            selectLanguage("isiXhosa")
        }

        sepediOption.setOnClickListener {
            selectLanguage("Sepedi")
        }
    }

    private fun selectLanguage(language: String) {

        selectedLanguageText.text = language

        englishIndicator.alpha =
            if (language == "English") 1f else 0.25f

        isizuluIndicator.alpha =
            if (language == "isiZulu") 1f else 0.25f

        afrikaansIndicator.alpha =
            if (language == "Afrikaans") 1f else 0.25f

        isixhosaIndicator.alpha =
            if (language == "isiXhosa") 1f else 0.25f

        sepediIndicator.alpha =
            if (language == "Sepedi") 1f else 0.25f

        if (language != "English") {
            Toast.makeText(
                this,
                "$language selected. Full translation will be connected later.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setupClickListeners() {

        backButton.setOnClickListener {
            finish()
        }
    }
}