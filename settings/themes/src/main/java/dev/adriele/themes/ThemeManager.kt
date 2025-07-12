package dev.adriele.themes

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit

object ThemeManager {
    const val MODE_LIGHT = "light"
    const val MODE_DARK = "dark"
    const val MODE_SYSTEM = "system"
    private const val PREF_NAME = "theme_pref"
    private const val KEY_THEME = "selected_theme"

    fun applyTheme(mode: String) {
        when (mode) {
            MODE_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            MODE_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            MODE_SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    fun saveTheme(context: Context, mode: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(KEY_THEME, mode) }
        applyTheme(mode)
    }

    fun getSavedTheme(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_THEME, MODE_SYSTEM) ?: MODE_SYSTEM
    }
}