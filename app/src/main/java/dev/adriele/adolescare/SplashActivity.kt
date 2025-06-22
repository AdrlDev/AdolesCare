package dev.adriele.adolescare

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import dev.adriele.adolescare.authentication.LoginActivity
import dev.adriele.adolescare.authentication.SignUpActivity
import dev.adriele.adolescare.databinding.ActivitySplashBinding
import dev.adriele.language.LanguageManager
import dev.adriele.language.LanguageSelectorDialog
import dev.adriele.language.LocaleHelper

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding

    private var savedLang : String? = null

    private val handler = Handler(Looper.getMainLooper())
    private var progressStatus = 0

    private val requestStorageAccessLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // Check if permission is granted after returning from settings
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
            Environment.isExternalStorageManager()
        ) {
            showLanguageDialog()
        }
    }

    private val requestLegacyPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showLanguageDialog()
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val savedLang = LanguageManager.getSavedLanguage(newBase)
        val updatedContext = LocaleHelper.setLocale(newBase, savedLang)
        super.attachBaseContext(updatedContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        savedLang = LanguageManager.getSavedLanguage(this)

        simulateLoading()
    }

    private fun showLanguageDialog() {
        // Show language selection only if language not yet chosen
        if (savedLang == null) {
            LanguageSelectorDialog(this).show(object : LanguageSelectorDialog.LanguageSelectionCallback {
                override fun onLanguageSelected(languageCode: String) {
                    LanguageManager.saveLanguage(this@SplashActivity, languageCode)
                    // Restart activity to apply the new language
                    recreate()
                    hideProgress()
                }
            })
        }
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                showLanguageDialog()
            } else {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                requestStorageAccessLauncher.launch(intent)
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
            ) {
                showLanguageDialog()
            } else {
                requestLegacyPermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    private fun simulateLoading() {
        progressStatus = 0
        binding.progressBar.progress = progressStatus

        handler.post(object : Runnable {
            override fun run() {
                if (progressStatus < 100) {
                    progressStatus += 2
                    binding.progressBar.progress = progressStatus
                    handler.postDelayed(this, 30)
                } else {
                    onLoadingFinished()
                }
            }
        })
    }

    private fun onLoadingFinished() {
        // Continue your splash logic here, e.g., request permission or show language dialog
        handler.postDelayed({
            requestStoragePermission()
            hideProgress()
        }, 500) // Delay ensures ActivityResultLauncher is ready
    }

    private fun hideProgress() {
        binding.progressBar.visibility = View.GONE
        binding.buttonsContainer.visibility = View.VISIBLE

        binding.btnSignup.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        binding.btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}