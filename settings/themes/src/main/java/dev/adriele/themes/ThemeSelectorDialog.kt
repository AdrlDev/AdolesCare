package dev.adriele.themes

import android.app.AlertDialog
import android.content.Context

class ThemeSelectorDialog {

    interface ThemeSelectionCallback {
        fun onThemeSelected(selectedTheme: String)
    }

    fun showThemeSelectorDialog(context: Context, themeSelectionCallback: ThemeSelectionCallback) {
        val items = arrayOf("Light", "Dark", "System Default")
        val modes = arrayOf(ThemeManager.MODE_LIGHT, ThemeManager.MODE_DARK, ThemeManager.MODE_SYSTEM)

        AlertDialog.Builder(context)
            .setTitle("Select Theme")
            .setItems(items) { _, which ->
                themeSelectionCallback.onThemeSelected(modes[which])
            }
            .show()
    }
}