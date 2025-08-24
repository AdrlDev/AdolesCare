package dev.adriele.adolescare.authentication

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.platform.MaterialSharedAxis
import dev.adriele.adolescare.BaseActivity
import dev.adriele.adolescare.ui.DashboardActivity
import dev.adriele.adolescare.helpers.Utility
import dev.adriele.adolescare.helpers.Utility.PreferenceManager
import dev.adriele.adolescare.helpers.Utility.SecurityUtils
import dev.adriele.adolescare.database.AppDatabaseProvider
import dev.adriele.adolescare.database.dao.UserDao
import dev.adriele.adolescare.database.repositories.implementation.ArchiveRepositoryImpl
import dev.adriele.adolescare.database.repositories.implementation.ReminderRepositoryImpl
import dev.adriele.adolescare.database.repositories.implementation.UserRepositoryImpl
import dev.adriele.adolescare.databinding.ActivityLoginBinding
import dev.adriele.adolescare.dialogs.MyLoadingDialog
import dev.adriele.adolescare.viewmodel.ArchiveViewModel
import dev.adriele.adolescare.viewmodel.ReminderViewModel
import dev.adriele.adolescare.viewmodel.UserViewModel
import dev.adriele.adolescare.viewmodel.factory.ArchiveViewModelFactory
import dev.adriele.adolescare.viewmodel.factory.ReminderViewModelFactory
import dev.adriele.adolescare.viewmodel.factory.UserViewModelFactory
import dev.adriele.language.R

class LoginActivity : BaseActivity(), Utility.SignUpHereClickListener {
    private lateinit var binding: ActivityLoginBinding

    private lateinit var userRepositoryImpl: UserRepositoryImpl
    private lateinit var userDao: UserDao
    private lateinit var userViewModel: UserViewModel
    private lateinit var reminderViewModel: ReminderViewModel
    private lateinit var archiveViewModel: ArchiveViewModel

    private lateinit var loadingDialog: MyLoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedAxis = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        window.enterTransition = sharedAxis
        window.exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        init()
        initializeViewModel()
        afterInit()
    }

    private fun initializeViewModel() {
        userDao = AppDatabaseProvider.getDatabase(this).userDao()
        userRepositoryImpl = UserRepositoryImpl(userDao)

        // Create the ViewModel using the factory
        val factory = UserViewModelFactory(userRepositoryImpl)
        userViewModel = ViewModelProvider(this, factory)[UserViewModel::class.java]

        val reminderDao = AppDatabaseProvider.getDatabase(this).reminderDao()
        val reminderRepository = ReminderRepositoryImpl(reminderDao)
        val reminderFactory = ReminderViewModelFactory(reminderRepository)
        reminderViewModel = ViewModelProvider(this, reminderFactory)[ReminderViewModel::class]

        val archiveDao = AppDatabaseProvider.getDatabase(this).archiveDao()
        val archiveRepository = ArchiveRepositoryImpl(archiveDao)
        val archiveViewModelFactory = ArchiveViewModelFactory(archiveRepository)
        archiveViewModel = ViewModelProvider(this, archiveViewModelFactory)[ArchiveViewModel::class.java]
    }

    private fun afterInit() {
        userViewModel.user.observe(this) { user ->
            if (user == null) {
                loadingDialog.dismiss()
                Snackbar.make(binding.root, getString(R.string.login_failed_user_not_found), Snackbar.LENGTH_LONG).show()
                return@observe
            }

            if (binding.etPassword.text.toString().isNotEmpty()) {
                if (SecurityUtils.checkPassword(binding.etPassword.text.toString(), user.password)) {
                    // ✅ Schedule Worker AFTER successful login
                    reminderViewModel.scheduleDailyDelayedPeriodWorker(this,user.userId)
                    reminderViewModel.scheduleDailyPredictedDatesWorker(this, user.userId)
                    archiveViewModel.scheduleReminderArchiverWorker(this)

                    Handler(Looper.getMainLooper()).postDelayed({
                        loadingDialog.dismiss()
                        val intent = Intent(this, DashboardActivity::class.java).apply {
                            putExtra("userId", user.userId)
                            putExtra("userName", user.username)
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        }
                        startActivity(intent)
                    }, 1500)
                } else {
                    loadingDialog.dismiss()
                    Snackbar.make(binding.root, getString(R.string.login_failed_incorrect_password), Snackbar.LENGTH_LONG).show()
                }
            }
        }

        userViewModel.updatePasswordStatus.observe(this) { status ->
            if(status) {
                loadingDialog.dismiss()
                Snackbar.make(binding.root, getString(R.string.password_updated_successfully), Snackbar.LENGTH_LONG).show()
            } else {
                loadingDialog.dismiss()
                Snackbar.make(binding.root, getString(R.string.failed_to_update_password), Snackbar.LENGTH_LONG).show()
            }
        }

        binding.btnLogin.setOnClickListener {
            loadingDialog.show(getString(R.string.logging_in_please_wait))

            val username = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()

            if(username.isEmpty()) {
                binding.llUsername.error = getString(R.string.please_enter_username)
                binding.etUsername.requestFocus()
                loadingDialog.dismiss()
                return@setOnClickListener
            } else if(password.isEmpty()) {
                binding.llPassword.error = getString(R.string.please_enter_password)
                binding.etPassword.requestFocus()
                loadingDialog.dismiss()
                return@setOnClickListener
            } else {
                userViewModel.getUserByUsername(username)
            }
        }

        binding.cbRemember.setOnCheckedChangeListener { p0, p1 ->
            val username = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()

            if (p1 && username.isNotEmpty() && password.isNotEmpty()) {
                PreferenceManager.saveLoginInfo(this@LoginActivity, username, password, true)
            } else {
                PreferenceManager.clearLoginInfo(this@LoginActivity)
            }
        }

        binding.tvForget.setOnClickListener {
            Utility.showChangePasswordDialog(this) { username, newPassword ->
                loadingDialog.show(getString(R.string.updating_password_please_wait))
                userViewModel.updatePasswordByUsername(username, SecurityUtils.hashPasswordBcrypt(newPassword))
            }
        }
    }

    private fun init() {
        Utility.setupDonatHaveAccountText(this,binding.tvDoNotHaveAccount, this)

        loadingDialog = MyLoadingDialog(this)

        val (savedUsername, savedPassword, isRemembered) = PreferenceManager.getSavedLoginInfo(this)

        if (isRemembered) {
            binding.etUsername.setText(savedUsername)
            binding.etPassword.setText(savedPassword)
            binding.cbRemember.isChecked = true
        }
    }

    override fun onSignUpClicked() {
        val bundle = ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
        val intent = Intent(this, SignUpActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        startActivity(intent, bundle)
    }
}