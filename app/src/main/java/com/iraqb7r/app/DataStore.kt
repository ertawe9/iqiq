package com.iraqb7r.app

import android.content.Context
import android.content.SharedPreferences

object DataStore {
    private const val PREFS = "iraq_b7r_prefs"
    private const val KEY_CARTS = "carts"
    private const val KEY_ARCHIVE = "archive"
    private const val KEY_NEXT_ID = "next_id"
    private const val KEY_THEME_LIGHT = "theme_light"
    private const val KEY_ACCENT = "accent" // gold | red | blue | green | purple
    private const val KEY_WIDGET_ENABLED = "widget_enabled"

    private fun prefs(ctx: Context): SharedPreferences =
        ctx.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun getCarts(ctx: Context): MutableList<CartItem> =
        CartItem.listFromJsonArray(prefs(ctx).getString(KEY_CARTS, null))

    fun saveCarts(ctx: Context, list: List<CartItem>) {
        prefs(ctx).edit().putString(KEY_CARTS, CartItem.listToJsonArray(list)).apply()
    }

    fun getArchive(ctx: Context): MutableList<CartItem> =
        CartItem.listFromJsonArray(prefs(ctx).getString(KEY_ARCHIVE, null))

    fun saveArchive(ctx: Context, list: List<CartItem>) {
        prefs(ctx).edit().putString(KEY_ARCHIVE, CartItem.listToJsonArray(list)).apply()
    }

    fun nextId(ctx: Context): Int {
        val p = prefs(ctx)
        val id = p.getInt(KEY_NEXT_ID, 1)
        p.edit().putInt(KEY_NEXT_ID, id + 1).apply()
        return id
    }

    fun isLightTheme(ctx: Context): Boolean = prefs(ctx).getBoolean(KEY_THEME_LIGHT, false)
    fun setLightTheme(ctx: Context, value: Boolean) {
        prefs(ctx).edit().putBoolean(KEY_THEME_LIGHT, value).apply()
    }

    fun getAccent(ctx: Context): String = prefs(ctx).getString(KEY_ACCENT, "gold") ?: "gold"
    fun setAccent(ctx: Context, value: String) {
        prefs(ctx).edit().putString(KEY_ACCENT, value).apply()
    }

    fun isWidgetEnabled(ctx: Context): Boolean = prefs(ctx).getBoolean(KEY_WIDGET_ENABLED, false)
    fun setWidgetEnabled(ctx: Context, value: Boolean) {
        prefs(ctx).edit().putBoolean(KEY_WIDGET_ENABLED, value).apply()
    }

    fun resetAll(ctx: Context) {
        prefs(ctx).edit().clear().apply()
    }
}
