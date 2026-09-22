package com.example.fadeandfaith

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class Privacy : AppCompatActivity() {

    private lateinit var backButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_privacy)

        backButton = findViewById(R.id.backButton)

        backButton.setOnClickListener {
            finish()
        }
    }
}