package dev.adriele.language

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.core.content.edit

object LanguageManager {
    private const val PREFS_NAME = "app_prefs"
    private const val PREF_LANGUAGE = "app_language"

    fun getSavedLanguage(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(PREF_LANGUAGE, null)
    }

    fun saveLanguage(context: Context, languageCode: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit() { putString(PREF_LANGUAGE, languageCode) }
    }

    fun markLanguageChanged(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putBoolean("LANGUAGE_CHANGED", true) }
    }

    fun consumeLanguageChanged(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val changed = prefs.getBoolean("LANGUAGE_CHANGED", false)
        if (changed) prefs.edit { putBoolean("LANGUAGE_CHANGED", false) }
        return changed
    }
}
