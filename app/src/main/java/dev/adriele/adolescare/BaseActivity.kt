package dev.adriele.adolescare

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dev.adriele.language.LanguageManager
import dev.adriele.language.LocaleHelper
import dev.adriele.themes.ThemeManager

open class BaseActivity : AppCompatActivity() {
    override fun attachBaseContext(newBase: Context) {
        val savedLang = LanguageManager.getSavedLanguage(newBase)
        val updatedContext = savedLang?.let {
            LocaleHelper.setLocale(newBase, it)
        } ?: newBase

        super.attachBaseContext(updatedContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply theme early
        ThemeManager.applyTheme(ThemeManager.getSavedTheme(this))
        super.onCreate(savedInstanceState)
    }
}