package com.iraqb7r.app

import android.content.Context
import androidx.core.content.ContextCompat

object AccentTheme {
    fun colorFor(ctx: Context, accent: String): Int {
        val res = when (accent) {
            "red" -> R.color.accent_red
            "blue" -> R.color.accent_blue
            "green" -> R.color.accent_green
            "purple" -> R.color.accent_purple
            else -> R.color.accent_gold
        }
        return ContextCompat.getColor(ctx, res)
    }

    fun darkColorFor(ctx: Context, accent: String): Int {
        val res = when (accent) {
            "red" -> R.color.accent_red_dark
            "blue" -> R.color.accent_blue_dark
            "green" -> R.color.accent_green_dark
            "purple" -> R.color.accent_purple_dark
            else -> R.color.accent_gold_dark
        }
        return ContextCompat.getColor(ctx, res)
    }
}
