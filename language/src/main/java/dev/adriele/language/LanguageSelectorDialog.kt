package dev.adriele.language

import android.app.AlertDialog
import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class LanguageSelectorDialog(private val context: Context) {

    interface LanguageSelectionCallback {
        fun onLanguageSelected(languageCode: String)
    }

    fun show(callback: LanguageSelectionCallback, cancellable: Boolean) {
        val languages = arrayOf(
            context.getString(R.string.tagalog),
            context.getString(R.string.english)
        )

        MaterialAlertDialogBuilder(context)
            .setTitle(context.getString(R.string.select_language))
            .setCancelable(cancellable)
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