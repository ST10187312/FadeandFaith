package com.example.fadeandfaith

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity

class Splash : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash)

        animateIn()

        Handler(Looper.getMainLooper()).postDelayed({

            startActivity(
                Intent(this, Onboarding::class.java)
            )

            finish()

        }, 3000)
    }

    /**
     * Logo grows in first, then the name, the gold line and the tagline
     * fade up one after another.
     */
    private fun animateIn() {

        val logo = findViewById<View>(R.id.logoText)
        val brand = findViewById<View>(R.id.brandName)
        val line = findViewById<View>(R.id.goldLine)
        val tagline = findViewById<View>(R.id.tagline)

        logo.alpha = 0f
        logo.scaleX = 0.6f
        logo.scaleY = 0.6f

        logo.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(700)
            .setInterpolator(DecelerateInterpolator())
            .start()

        line.alpha = 0f
        line.scaleX = 0f

        line.animate()
            .alpha(1f)
            .scaleX(1f)
            .setStartDelay(1100)
            .setDuration(500)
            .start()

        fadeUp(brand, 600)
        fadeUp(tagline, 1300)
    }

    private fun fadeUp(view: View, delay: Long) {

        view.alpha = 0f
        view.translationY = 30f

        view.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(delay)
            .setDuration(600)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }
}