package com.example.fadeandfaith

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2

class Onboarding : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2

    private lateinit var indicator1: View
    private lateinit var indicator2: View
    private lateinit var indicator3: View

    private lateinit var title: TextView
    private lateinit var description: TextView
    private lateinit var nextButton: Button
    private lateinit var skipButton: TextView

    private val titles = arrayOf(
        "Your Style.\nYour Standard.",
        "Find Your\nPerfect Barber.",
        "Book. Relax.\nLook Fresh."
    )

    private val descriptions = arrayOf(
        "Experience premium barbering designed around your style, confidence and convenience.",
        "Discover talented barbers, explore their work and choose the professional that's right for you.",
        "Choose your service, select your time and secure your appointment in just a few taps."
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_onboarding)

        initializeViews()
        setupViewPager()
        setupButtons()
        updateOnboardingContent(0)
    }

    private fun initializeViews() {

        viewPager = findViewById(R.id.onboardingViewPager)

        indicator1 = findViewById(R.id.indicator1)
        indicator2 = findViewById(R.id.indicator2)
        indicator3 = findViewById(R.id.indicator3)

        title = findViewById(R.id.onboardingTitle)
        description = findViewById(R.id.onboardingDescription)

        nextButton = findViewById(R.id.nextButton)
        skipButton = findViewById(R.id.skipButton)
    }

    private fun setupViewPager() {

        viewPager.adapter = OnboardingAdapter()

        viewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {

                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)

                    updateOnboardingContent(position)
                }
            }
        )
    }

    private fun setupButtons() {

        nextButton.setOnClickListener {

            val currentPage = viewPager.currentItem

            if (currentPage < 2) {

                viewPager.currentItem = currentPage + 1

            } else {

                openLogin()
            }
        }

        skipButton.setOnClickListener {

            openLogin()
        }
    }

    private fun updateOnboardingContent(position: Int) {

        title.text = titles[position]
        description.text = descriptions[position]

        indicator1.alpha = if (position == 0) 1f else 0.3f
        indicator2.alpha = if (position == 1) 1f else 0.3f
        indicator3.alpha = if (position == 2) 1f else 0.3f

        nextButton.text =
            if (position == 2) {
                "GET STARTED"
            } else {
                "NEXT"
            }
    }

    private fun openLogin() {

        startActivity(
            Intent(
                this,
                Login::class.java
            )
        )

        finish()
    }
}