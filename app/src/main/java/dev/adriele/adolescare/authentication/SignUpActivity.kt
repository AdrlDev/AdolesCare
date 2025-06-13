package dev.adriele.adolescare.authentication

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import dev.adriele.adolescare.R
import dev.adriele.adolescare.Utility
import dev.adriele.adolescare.Utility.SecurityUtils
import dev.adriele.adolescare.Utility.TermsPrivacyClickListener
import dev.adriele.adolescare.database.AppDatabaseProvider
import dev.adriele.adolescare.database.dao.UserDao
import dev.adriele.adolescare.database.entities.User
import dev.adriele.adolescare.database.repositories.implementation.UserRepositoryImpl
import dev.adriele.adolescare.databinding.ActivitySignUpBinding
import dev.adriele.adolescare.dialogs.MyLoadingDialog
import dev.adriele.adolescare.viewmodel.UserViewModel
import dev.adriele.adolescare.viewmodel.factory.UserViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignUpActivity : AppCompatActivity(), TermsPrivacyClickListener, Utility.LoginHereClickListener, Utility.OnDatePickedCallback {
    private lateinit var binding: ActivitySignUpBinding

    private lateinit var userRepositoryImpl: UserRepositoryImpl
    private lateinit var userDao: UserDao

    private lateinit var userViewModel: UserViewModel

    private lateinit var loadingDialog: MyLoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        init()
        afterInit()
    }

    private fun init() {
        loadingDialog = MyLoadingDialog(this)

        Utility.setupTermOfUseText(binding.tvTermOfUse, this)
        Utility.setupAlreadyHaveAccountText(binding.tvAlreadyHaveAccount, this)
        Utility.setupDatePicker(binding.etBirthday, this, this)

        userDao = AppDatabaseProvider.getDatabase(this).userDao()
        userRepositoryImpl = UserRepositoryImpl(userDao)

        // Create the ViewModel using the factory
        val factory = UserViewModelFactory(userRepositoryImpl)
        userViewModel = ViewModelProvider(this, factory)[UserViewModel::class.java]
    }

    private fun afterInit() {
        // Observe insert status
        userViewModel.insertStatus.observe(this) { (success, uid) ->
            if (success) {
                loadingDialog.dismiss()
                startActivity(Intent(this, CompleteSetupActivity::class.java).putExtra("userId", uid))
                finish()
            } else {
                loadingDialog.dismiss()
                Snackbar.make(binding.root, "Failed to insert user", Snackbar.LENGTH_SHORT).show()
            }
        }

        binding.btnSignup.setOnClickListener {
            loadingDialog.show("Saving, please wait...")

            val fName = binding.etFirstName.text
            val lName = binding.etLastName.text
            val mi = binding.etMi.text
            val birthday = binding.etBirthday.text
            val age = binding.etAge.text
            val username = binding.etUsername.text
            val password = binding.etPassword.text
            val confirmPassword = binding.etConfirmPassword.text
            val uid = Utility.generateUID()
            val hashedPassword = SecurityUtils.hashPasswordBcrypt(password.toString())

            if(fName.isNullOrEmpty()) {
                binding.llFName.error = "Please enter your first name."
                binding.etFirstName.requestFocus()
                loadingDialog.dismiss()
                return@setOnClickListener
            } else if(lName.isNullOrEmpty()) {
                binding.llLName.error = "Please enter your last name."
                binding.etLastName.requestFocus()
                loadingDialog.dismiss()
                return@setOnClickListener
            } else if(birthday.isNullOrEmpty()) {
                binding.llBDay.error = "Please enter your birthday."
                binding.etBirthday.requestFocus()
                loadingDialog.dismiss()
                return@setOnClickListener
            } else if(username.isNullOrEmpty()) {
                binding.llUsername.error = "Please enter your username."
                binding.etUsername.requestFocus()
                loadingDialog.dismiss()
                return@setOnClickListener
            } else if(password.isNullOrEmpty()) {
                binding.llPassword.error = "Please enter your password."
                binding.etPassword.requestFocus()
                loadingDialog.dismiss()
                return@setOnClickListener
            } else if(password.toString() != confirmPassword.toString()) {
                binding.llConfirmPassword.error = "Password not match. Try again."
                binding.etConfirmPassword.requestFocus()
                loadingDialog.dismiss()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val isTaken = userViewModel.isUsernameTaken(username.toString())
                if (isTaken) {
                    withContext(Dispatchers.Main) {
                        loadingDialog.dismiss()
                        binding.llUsername.error = "Username already exists. Please choose another."
                        binding.etUsername.requestFocus()
                    }
                } else {
                    val userData = User(
                        userId = uid,
                        username = username.toString(),
                        password = hashedPassword,
                        firstName = fName.toString(),
                        lastName = lName.toString(),
                        middleInitial = mi.toString(),
                        birthday = birthday.toString(),
                        age = age.toString().toInt()
                    )

                    userViewModel.insertUser(userData)
                }
            }
        }
    }

    override fun onTermsOfUseClicked() {

    }

    override fun onPrivacyPolicyClicked() {

    }

    override fun onLoginClicked() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun onDatePicked(formattedDate: String, computedResult: String) {
        binding.etBirthday.setText(formattedDate)
        binding.etAge.setText(computedResult)
    }
}