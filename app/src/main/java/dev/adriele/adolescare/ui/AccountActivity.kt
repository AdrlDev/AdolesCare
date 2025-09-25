package dev.adriele.adolescare.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.platform.MaterialSharedAxis
import dev.adriele.adolescare.R
import dev.adriele.adolescare.authentication.LoginActivity
import dev.adriele.adolescare.database.AppDatabaseProvider
import dev.adriele.adolescare.database.repositories.implementation.UserRepositoryImpl
import dev.adriele.adolescare.databinding.ActivityAccountBinding
import dev.adriele.adolescare.dialogs.MyLoadingDialog
import dev.adriele.adolescare.helpers.Utility
import dev.adriele.adolescare.viewmodel.UserViewModel
import dev.adriele.adolescare.viewmodel.factory.UserViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.toString

class AccountActivity : AppCompatActivity(), Utility.OnDatePickedCallback {
    private lateinit var binding: ActivityAccountBinding

    private lateinit var loadingDialog: MyLoadingDialog
    private lateinit var userViewModel: UserViewModel
    private lateinit var loading: MyLoadingDialog

    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedAxis = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        window.enterTransition = sharedAxis
        window.exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loading = MyLoadingDialog(this)
        disableInputs()
        setUpTopBar()
        initViewModel()
        getUserData()
        observer()
    }

    private fun observer() {
        userViewModel.user.observe(this) { user ->
            user?.let { it ->
                val fName = "${it.firstName.replaceFirstChar { fName -> fName.uppercase() }} " +
                        "${it.middleInitial?.replaceFirstChar { mName -> mName.uppercase() }}. " +
                        "${it.lastName.replaceFirstChar { lName -> lName.uppercase()}} "
                val userName = it.username
                val sex = it.sex
                val bDay = it.birthday
                val age = it.age

                val isMale = sex == "Male"

                binding.tvFullName.text = fName
                binding.etUsername.setText(userName)
                binding.chipFemale.isChecked = !isMale
                binding.etBirthday.setText(bDay)
                binding.etAge.setText(age.toString())
            }
        }

        userViewModel.updateState.observe(this) { state ->
            if(state) {
                loadingDialog.dismiss()
                getUserData()
                disableInputs()
            } else {
                loadingDialog.dismiss()
                Snackbar.make(binding.root, "Something went wrong!", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun getUserData() {
        userId?.let { uid ->
            userViewModel.getUserByUID(uid = uid)
        }
    }

    private fun initViewModel() {
        loadingDialog = MyLoadingDialog(this)

        userId = intent.getStringExtra("userId")

        val userDao = AppDatabaseProvider.getDatabase(this).userDao()
        val userRepositoryImpl = UserRepositoryImpl(userDao)

        // Create the ViewModel using the factory
        val factory = UserViewModelFactory(userRepositoryImpl)
        userViewModel = ViewModelProvider(this, factory)[UserViewModel::class.java]

        Utility.setupDatePicker(this, binding.etBirthday, this, this)
    }

    private fun setUpTopBar() {
        binding.topAppBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_edit -> {
                    enableInputs()
                    true
                }
                R.id.action_logout -> {
                    logout()
                    true
                }
                else -> false
            }
        }

        binding.btnCancel.setOnClickListener {
            disableInputs()
            getUserData()
        }

        binding.btnUpdate.setOnClickListener {
            loadingDialog.apply {
                show("Updating please wait...")
            }

            val userName = binding.etUsername.text.toString()
            val bDay = binding.etBirthday.text.toString()
            val age = binding.etAge.text.toString()

            lifecycleScope.launch {
                val isTaken = userViewModel.isUsernameTaken(userName)
                if (isTaken) {
                    withContext(Dispatchers.Main) {
                        loadingDialog.dismiss()
                        binding.llFName.error = getString(dev.adriele.language.R.string.username_already_exists)
                        binding.etUsername.requestFocus()
                    }
                } else {
                    userViewModel.updateUser(userName, bDay, age.toInt(), userId ?: "")
                }
            }
        }
    }

    private fun disableInputs() {
        binding.etUsername.isEnabled = false
        binding.etBirthday.isEnabled = false
        binding.etAge.isEnabled = false

        binding.llFName.error = null

        binding.llBtn.visibility = View.GONE
    }

    private fun enableInputs() {
        binding.etUsername.isEnabled = true
        binding.etBirthday.isEnabled = true
        binding.etAge.isEnabled = true

        binding.etUsername.requestFocus()

        binding.llBtn.visibility = View.VISIBLE
    }

    private fun logout() {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(dev.adriele.language.R.string.menu_logout))
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton(getString(dev.adriele.language.R.string.yes)) { _, _ ->
                loading.show("Please wait...")

                Handler(Looper.getMainLooper()).postDelayed({
                    loading.dismiss()
                    val intent = Intent(this, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                    startActivity(intent)
                }, 1500)
            }
            .setNegativeButton(getString(dev.adriele.language.R.string.cancel), null)
            .show()
    }

    override fun onDatePicked(formattedDate: String, computedResult: String) {
        binding.etBirthday.setText(formattedDate)
        binding.etAge.setText(computedResult)
    }
}