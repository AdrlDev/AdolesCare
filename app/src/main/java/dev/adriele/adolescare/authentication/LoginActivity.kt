package dev.adriele.adolescare.authentication

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.CompoundButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import dev.adriele.adolescare.DashboardActivity
import dev.adriele.adolescare.Utility
import dev.adriele.adolescare.Utility.PreferenceManager
import dev.adriele.adolescare.Utility.SecurityUtils
import dev.adriele.adolescare.database.AppDatabaseProvider
import dev.adriele.adolescare.database.dao.UserDao
import dev.adriele.adolescare.database.repositories.implementation.UserRepositoryImpl
import dev.adriele.adolescare.databinding.ActivityLoginBinding
import dev.adriele.adolescare.viewmodel.UserViewModel
import dev.adriele.adolescare.viewmodel.factory.UserViewModelFactory

class LoginActivity : AppCompatActivity(), Utility.SignUpHereClickListener {
    private lateinit var binding: ActivityLoginBinding

    private lateinit var userRepositoryImpl: UserRepositoryImpl
    private lateinit var userDao: UserDao

    private lateinit var userViewModel: UserViewModel

    private lateinit var loadingDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
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
        afterInit()
    }

    private fun afterInit() {
        userViewModel.user.observe(this) { user ->
            if (user == null) {
                loadingDialog.dismiss()
                Snackbar.make(binding.root, "Login failed, user not found.", Snackbar.LENGTH_LONG).show()
                return@observe
            }

            if (binding.etPassword.text.toString().isNotEmpty()) {
                if (SecurityUtils.checkPassword(binding.etPassword.text.toString(), user.password)) {
                    loadingDialog.dismiss()
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                } else {
                    loadingDialog.dismiss()
                    Snackbar.make(binding.root, "Login failed, incorrect password.", Snackbar.LENGTH_LONG).show()
                }
            }
        }

        userViewModel.updatePasswordStatus.observe(this) { status ->
            if(status) {
                loadingDialog.dismiss()
                Snackbar.make(binding.root, "password updated successfully", Snackbar.LENGTH_LONG).show()
            } else {
                loadingDialog.dismiss()
                Snackbar.make(binding.root, "Failed to update password", Snackbar.LENGTH_LONG).show()
            }
        }

        binding.btnLogin.setOnClickListener {
            loadingDialog = Utility.showLoadingDialog(this, "Logging In, please wait...")

            val username = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()

            if(username.isEmpty()) {
                binding.llUsername.error = "Please enter username."
                binding.etUsername.requestFocus()
                loadingDialog.dismiss()
                return@setOnClickListener
            } else if(password.isEmpty()) {
                binding.llPassword.error = "Please enter password."
                binding.etPassword.requestFocus()
                loadingDialog.dismiss()
                return@setOnClickListener
            } else {
                userViewModel.getUserByUsername(username)
            }
        }

        binding.cbRemember.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
                val username = binding.etUsername.text.toString()
                val password = binding.etPassword.text.toString()

                if(p1 && username.isNotEmpty() && password.isNotEmpty()) {
                    PreferenceManager.saveLoginInfo(this@LoginActivity, username, password, true)
                } else {
                    PreferenceManager.clearLoginInfo(this@LoginActivity)
                }
            }
        })

        binding.tvForget.setOnClickListener {
            Utility.showChangePasswordDialog(this) { username, newPassword ->
                loadingDialog = Utility.showLoadingDialog(this, "Updating password, please wait...")
                userViewModel.updatePasswordByUsername(username, newPassword)
            }
        }
    }

    private fun init() {
        Utility.setupDonatHaveAccountText(binding.tvDoNotHaveAccount, this)

        userDao = AppDatabaseProvider.getDatabase(this).userDao()
        userRepositoryImpl = UserRepositoryImpl(userDao)

        // Create the ViewModel using the factory
        val factory = UserViewModelFactory(userRepositoryImpl)
        userViewModel = ViewModelProvider(this, factory)[UserViewModel::class.java]

        val (savedUsername, savedPassword, isRemembered) = PreferenceManager.getSavedLoginInfo(this)

        if (isRemembered) {
            binding.etUsername.setText(savedUsername)
            binding.etPassword.setText(savedPassword)
            binding.cbRemember.isChecked = true
        }
    }

    override fun onSignUpClicked() {
        startActivity(Intent(this, SignUpActivity::class.java))
        finish()
    }
}