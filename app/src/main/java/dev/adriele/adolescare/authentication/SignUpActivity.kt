package dev.adriele.adolescare.authentication

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.platform.MaterialSharedAxis
import dev.adriele.adolescare.BaseActivity
import dev.adriele.adolescare.R
import dev.adriele.adolescare.helpers.Utility
import dev.adriele.adolescare.helpers.Utility.SecurityUtils
import dev.adriele.adolescare.helpers.Utility.TermsPrivacyClickListener
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

class SignUpActivity : BaseActivity(), TermsPrivacyClickListener, Utility.LoginHereClickListener, Utility.OnDatePickedCallback, Utility.IUtility {
    private lateinit var binding: ActivitySignUpBinding

    private lateinit var userRepositoryImpl: UserRepositoryImpl
    private lateinit var userDao: UserDao

    private lateinit var userViewModel: UserViewModel

    private lateinit var loadingDialog: MyLoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedAxis = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        window.enterTransition = sharedAxis
        window.exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)

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

        Utility.setupTermOfUseText(this, binding.tvTermOfUse, this)
        Utility.setupAlreadyHaveAccountText(this,binding.tvAlreadyHaveAccount, this)
        Utility.setupDatePicker(this, binding.etBirthday, this, this)

        userDao = AppDatabaseProvider.getDatabase(this).userDao()
        userRepositoryImpl = UserRepositoryImpl(userDao)

        // Create the ViewModel using the factory
        val factory = UserViewModelFactory(userRepositoryImpl)
        userViewModel = ViewModelProvider(this, factory)[UserViewModel::class.java]

        binding.btnSignup.isEnabled = false
    }

    private fun afterInit() {
        // Observe insert status
        userViewModel.insertStatus.observe(this) { (success, uid) ->
            if (success) {
                loadingDialog.dismiss()
                val bundle = ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
                startActivity(Intent(this, CompleteSetupActivity::class.java).putExtra("userId", uid), bundle)
                finish()
            } else {
                loadingDialog.dismiss()
                Snackbar.make(binding.root, getString(dev.adriele.language.R.string.failed_to_insert_user), Snackbar.LENGTH_SHORT).show()
            }
        }

        binding.btnSignup.setOnClickListener {
            loadingDialog.show(getString(dev.adriele.language.R.string.saving_please_wait))

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
                binding.llFName.error = getString(dev.adriele.language.R.string.please_enter_first_name)
                binding.etFirstName.requestFocus()
                loadingDialog.dismiss()
                return@setOnClickListener
            } else if(lName.isNullOrEmpty()) {
                binding.llLName.error = getString(dev.adriele.language.R.string.please_enter_last_name)
                binding.etLastName.requestFocus()
                loadingDialog.dismiss()
                return@setOnClickListener
            } else if(birthday.isNullOrEmpty()) {
                binding.llBDay.error = getString(dev.adriele.language.R.string.please_enter_birthday)
                binding.etBirthday.requestFocus()
                loadingDialog.dismiss()
                return@setOnClickListener
            } else if(username.isNullOrEmpty()) {
                binding.llUsername.error = getString(dev.adriele.language.R.string.please_enter_username)
                binding.etUsername.requestFocus()
                loadingDialog.dismiss()
                return@setOnClickListener
            } else if(password.isNullOrEmpty()) {
                binding.llPassword.error = getString(dev.adriele.language.R.string.please_enter_password)
                binding.etPassword.requestFocus()
                loadingDialog.dismiss()
                return@setOnClickListener
            } else if(password.toString() != confirmPassword.toString()) {
                binding.llConfirmPassword.error = getString(dev.adriele.language.R.string.password_not_match)
                binding.etConfirmPassword.requestFocus()
                loadingDialog.dismiss()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val isTaken = userViewModel.isUsernameTaken(username.toString())
                if (isTaken) {
                    withContext(Dispatchers.Main) {
                        loadingDialog.dismiss()
                        binding.llUsername.error = getString(dev.adriele.language.R.string.username_already_exists)
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
        Utility.showDialog(
            context = this@SignUpActivity,
            title = getString(dev.adriele.language.R.string.term_privacy),
            supportingText = getString(dev.adriele.language.R.string.terms_privacy_policy_text),
            this
        )
    }

    override fun onPrivacyPolicyClicked() {
        Utility.showDialog(
            context = this@SignUpActivity,
            title = getString(dev.adriele.language.R.string.term_privacy),
            supportingText = getString(dev.adriele.language.R.string.terms_privacy_policy_text),
            callback = this
        )
    }

    override fun onLoginClicked() {
        val bundle = ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
        val intent = Intent(this, LoginActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        startActivity(intent, bundle)
    }

    override fun onDatePicked(formattedDate: String, computedResult: String) {
        binding.etBirthday.setText(formattedDate)
        binding.etAge.setText(computedResult)
    }

    override fun onAccept(accepted: Boolean) {
        binding.btnSignup.isEnabled = accepted
    }
}