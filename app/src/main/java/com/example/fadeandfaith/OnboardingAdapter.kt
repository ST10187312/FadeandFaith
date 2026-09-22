package com.example.fadeandfaith

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class OnboardingAdapter :
    RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {

    private val backgrounds = arrayOf(
        Color.rgb(8, 8, 8),
        Color.rgb(12, 12, 12),
        Color.rgb(7, 7, 7)
    )

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OnboardingViewHolder {

        val frame = FrameLayout(parent.context)

        frame.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        return OnboardingViewHolder(frame)
    }

    override fun onBindViewHolder(
        holder: OnboardingViewHolder,
        position: Int
    ) {

        holder.container.setBackgroundColor(
            backgrounds[position]
        )

        holder.logo.text = "F&F"

        holder.logo.setTextColor(
            Color.rgb(212, 175, 55)
        )
    }

    override fun getItemCount(): Int = 3

    class OnboardingViewHolder(
        val container: FrameLayout
    ) : RecyclerView.ViewHolder(container) {

        val logo = TextView(container.context)

        init {

            val logoSize = 110

            logo.layoutParams = FrameLayout.LayoutParams(
                dp(logoSize),
                dp(logoSize),
                Gravity.CENTER
            )

            logo.gravity = Gravity.CENTER

            logo.textSize = 30f

            logo.setTypeface(
                null,
                android.graphics.Typeface.BOLD
            )

            val background = GradientDrawable()

            background.shape =
                GradientDrawable.OVAL

            background.setColor(
                Color.rgb(18, 18, 18)
            )

            background.setStroke(
                dp(1),
                Color.rgb(212, 175, 55)
            )

            logo.background = background

            container.addView(logo)
        }

        private fun dp(value: Int): Int {

            return (
                    value *
                            container.resources
                                .displayMetrics
                                .density
                    ).toInt()
        }
    }
}