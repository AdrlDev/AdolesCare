package dev.adriele.adolescare.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.platform.MaterialSharedAxis
import dev.adriele.adolescare.BaseActivity
import dev.adriele.adolescare.database.AppDatabaseProvider
import dev.adriele.adolescare.database.repositories.implementation.UserRepositoryImpl
import dev.adriele.adolescare.databinding.ActivitySettingsBinding
import dev.adriele.adolescare.dialogs.MyLoadingDialog
import dev.adriele.adolescare.helpers.Utility
import dev.adriele.adolescare.helpers.Utility.SecurityUtils
import dev.adriele.adolescare.viewmodel.UserViewModel
import dev.adriele.adolescare.viewmodel.factory.UserViewModelFactory
import dev.adriele.language.LanguageManager
import dev.adriele.language.LanguageSelectorDialog
import dev.adriele.language.R
import dev.adriele.themes.ThemeSelectorDialog

class SettingsActivity : BaseActivity(), Utility.IUtility {
    private lateinit var binding: ActivitySettingsBinding

    private lateinit var loadingDialog: MyLoadingDialog

    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedAxis = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        window.enterTransition = sharedAxis
        window.exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViewModel()
        setUpTopBar()
        setUpButtonsClicked()
        observer()
    }

    private fun observer() {
        userViewModel.updatePasswordStatus.observe(this) { success ->
            loadingDialog.dismiss()
            if (success) {
                Snackbar.make(binding.main, getString(R.string.password_updated_successfully),
                    Snackbar.LENGTH_SHORT).show()
            } else {
                Snackbar.make(binding.main, getString(R.string.failed_to_update_password),
                    Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun initViewModel() {
        loadingDialog = MyLoadingDialog(this)

        val userDao = AppDatabaseProvider.getDatabase(this).userDao()
        val userRepositoryImpl = UserRepositoryImpl(userDao)

        // Create the ViewModel using the factory
        val factory = UserViewModelFactory(userRepositoryImpl)
        userViewModel = ViewModelProvider(this, factory)[UserViewModel::class.java]
    }

    private fun setUpButtonsClicked() {
        val clickListener = onClickListener()
        binding.btnLanguage.setOnClickListener(clickListener)
        binding.btnTheme.setOnClickListener(clickListener)
        binding.btnTerm.setOnClickListener(clickListener)
        binding.btnReset.setOnClickListener(clickListener)
    }

    private fun setUpTopBar() {
        setSupportActionBar(binding.topAppBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.topAppBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun onClickListener(): View.OnClickListener {
        return View.OnClickListener { view ->
            when (view) {
                binding.btnLanguage -> {
                    LanguageSelectorDialog(this).show(object : LanguageSelectorDialog.LanguageSelectionCallback {
                        override fun onLanguageSelected(languageCode: String) {
                            LanguageManager.saveLanguage(this@SettingsActivity, languageCode)
                            LanguageManager.markLanguageChanged(this@SettingsActivity)

                            val resultIntent = Intent()
                            resultIntent.putExtra("LANGUAGE_CHANGED", true)
                            setResult(RESULT_OK, resultIntent)
                            recreate()
                        }
                    }, true)
                }
                binding.btnTheme -> {
                    ThemeSelectorDialog().showThemeSelectorDialog(this, object : ThemeSelectorDialog.ThemeSelectionCallback {
                        override fun onThemeSelected(selectedTheme: String) {
                            recreate() // restart activity to apply theme
                        }
                    })
                }
                binding.btnTerm -> {
                    Utility.showDialog(
                        context = this@SettingsActivity,
                        title = getString(R.string.term_privacy),
                        supportingText = getString(R.string.terms_privacy_policy_text),
                        callback = this
                    )
                }
                binding.btnReset -> {
                    Utility.showChangePasswordDialog(this) { username, newPassword ->
                        loadingDialog.show(getString(R.string.updating_password_please_wait))
                        userViewModel.updatePasswordByUsername(username, SecurityUtils.hashPasswordBcrypt(newPassword))
                    }
                }
            }
        }
    }

    override fun onAccept(accepted: Boolean) {

    }
}