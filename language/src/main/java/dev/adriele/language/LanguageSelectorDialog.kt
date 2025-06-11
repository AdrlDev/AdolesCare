package dev.adriele.language

import android.app.AlertDialog
import android.content.Context

class LanguageSelectorDialog(private val context: Context) {

    interface LanguageSelectionCallback {
        fun onLanguageSelected(languageCode: String)
    }

    fun show(callback: LanguageSelectionCallback) {
        val languages = arrayOf(
            context.getString(R.string.tagalog),
            context.getString(R.string.english)
        )

        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.select_language))
            .setCancelable(false)
            .setItems(languages) { _, which ->
                val selectedLanguage = when (which) {
                    0 -> "tl"
                    1 -> "en"
                    else -> "en"
                }
                callback.onLanguageSelected(selectedLanguage)
            }
            .show()
    }
}