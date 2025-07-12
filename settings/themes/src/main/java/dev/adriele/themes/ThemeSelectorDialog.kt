package dev.adriele.themes

import android.app.AlertDialog
import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ThemeSelectorDialog {

    interface ThemeSelectionCallback {
        fun onThemeSelected(selectedTheme: String)
    }

    fun showThemeSelectorDialog(context: Context, themeSelectionCallback: ThemeSelectionCallback) {
        val items = arrayOf("Off", "On", "System Default")
        val modes = arrayOf(ThemeManager.MODE_LIGHT, ThemeManager.MODE_DARK, ThemeManager.MODE_SYSTEM)

        MaterialAlertDialogBuilder(context)
            .setTitle("Select Theme")
            .setItems(items) { _, which ->
                ThemeManager.saveTheme(context, modes[which]) // Save to SharedPreferences
                themeSelectionCallback.onThemeSelected(modes[which])
            }
            .show()
    }
}