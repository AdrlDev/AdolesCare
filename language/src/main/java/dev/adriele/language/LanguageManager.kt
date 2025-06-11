package dev.adriele.language

import android.content.Context
import androidx.core.content.edit

object LanguageManager {
    private const val PREFS_NAME = "app_prefs"
    private const val PREF_LANGUAGE = "app_language"
    const val DEFAULT_LANGUAGE = "en"

    fun getSavedLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(PREF_LANGUAGE, null) ?: DEFAULT_LANGUAGE
    }

    fun saveLanguage(context: Context, languageCode: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit() { putString(PREF_LANGUAGE, languageCode) }
    }
}
