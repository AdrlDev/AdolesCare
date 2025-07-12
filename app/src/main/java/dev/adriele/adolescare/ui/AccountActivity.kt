package dev.adriele.adolescare.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.transition.platform.MaterialSharedAxis
import dev.adriele.adolescare.database.AppDatabaseProvider
import dev.adriele.adolescare.database.repositories.implementation.UserRepositoryImpl
import dev.adriele.adolescare.databinding.ActivityAccountBinding
import dev.adriele.adolescare.dialogs.MyLoadingDialog
import dev.adriele.adolescare.viewmodel.UserViewModel
import dev.adriele.adolescare.viewmodel.factory.UserViewModelFactory

class AccountActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAccountBinding

    private lateinit var loadingDialog: MyLoadingDialog
    private lateinit var userViewModel: UserViewModel

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

        setUpTopBar()
        initViewModel()
        getUserData()
        observer()
    }

    private fun observer() {
        userViewModel.user.observe(this) { user ->
            user?.let { it ->
                val fName = "${it.firstName.replaceFirstChar { fName -> fName.uppercase() }} " +
                        "${it.middleInitial?.replaceFirstChar { mName -> mName.uppercase() }} " +
                        "${it.lastName.replaceFirstChar { lName -> lName.uppercase()}} "
                val userName = it.username
                val sex = it.sex
                val bDay = it.birthday
                val age = it.age

                val isMale = sex == "Male"

                binding.tvFullName.text = fName
                binding.etUsername.setText(userName)
                binding.chipMale.isChecked = isMale
                binding.chipFemale.isChecked = !isMale
                binding.etBirthday.setText(bDay)
                binding.etAge.setText(age.toString())
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
    }

    private fun setUpTopBar() {
        setSupportActionBar(binding.topAppBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.topAppBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}