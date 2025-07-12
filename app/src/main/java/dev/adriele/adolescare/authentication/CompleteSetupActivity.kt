package dev.adriele.adolescare.authentication

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.platform.MaterialFadeThrough
import com.google.android.material.transition.platform.MaterialSharedAxis
import dev.adriele.adolescare.BaseActivity
import dev.adriele.adolescare.R
import dev.adriele.adolescare.helpers.Utility
import dev.adriele.adolescare.authentication.adapter.PagerAdapter
import dev.adriele.adolescare.authentication.contracts.FragmentDataListener
import dev.adriele.adolescare.authentication.fragments.SelectBarangayFragment
import dev.adriele.adolescare.authentication.fragments.SelectSexFragment
import dev.adriele.adolescare.database.AppDatabaseProvider
import dev.adriele.adolescare.database.repositories.implementation.UserRepositoryImpl
import dev.adriele.adolescare.databinding.ActivityCompleteSetupBinding
import dev.adriele.adolescare.dialogs.MyLoadingDialog
import dev.adriele.adolescare.viewmodel.UserViewModel
import dev.adriele.adolescare.viewmodel.factory.UserViewModelFactory

class CompleteSetupActivity : BaseActivity(), FragmentDataListener {
    private lateinit var binding: ActivityCompleteSetupBinding

    private lateinit var vp: ViewPager2
    private lateinit var stepper: LinearLayout
    private lateinit var btnNext: Button

    private lateinit var pagerAdapter: PagerAdapter
    private val fragments = mutableListOf<Fragment>()

    private val collectedData = mutableMapOf<String, Any>()

    private lateinit var userRepositoryImpl: UserRepositoryImpl
    private lateinit var userViewModel: UserViewModel

    private lateinit var loadingDialog: MyLoadingDialog

    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedAxis = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        window.enterTransition = sharedAxis
        window.exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCompleteSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        init()
        afterInit()
    }

    private fun initViews() {
        vp = binding.viewpager
        stepper = binding.stepperContainer
        btnNext = binding.btnNext
    }

    private fun init() {
        intent.getStringExtra("userId")?.let {
            userId = it
        }

        loadingDialog = MyLoadingDialog(this)

        //default fragment

        fragments.addAll(listOf(
            SelectBarangayFragment(),
            SelectSexFragment()
        ))

        pagerAdapter = PagerAdapter(this)
        vp.adapter = pagerAdapter

        pagerAdapter.updateFragments(fragments) // <-- Load fragments first

        // Now that fragments are added, initialize stepper
        Utility.setupStepper(pagerAdapter.itemCount, resources, this, stepper)

        vp.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                Utility.updateStepper(position, pagerAdapter.itemCount, stepper, binding.tvStep, resources)
                btnNext.text = if (position == pagerAdapter.itemCount - 1) "Finish" else "Next"
            }
        })

        val db = AppDatabaseProvider.getDatabase(this).userDao()
        userRepositoryImpl = UserRepositoryImpl(db)
        // Create the ViewModel using the factory
        val factory = UserViewModelFactory(userRepositoryImpl)
        userViewModel = ViewModelProvider(this, factory)[UserViewModel::class.java]
    }

    private fun afterInit() {
        userViewModel.updateSexBarangay.observe(this) { (success, selectedSex) ->
            if(success) {
                loadingDialog.dismiss()
                val bundle = ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
                startActivity(
                    Intent(this, MenstrualHistoryQuestionActivity::class.java)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        .putExtra("userSex", selectedSex)
                        .putExtra("userId", userId),
                    bundle
                )
            } else {
                loadingDialog.dismiss()
                Snackbar.make(binding.root, "Failed to update user", Snackbar.LENGTH_SHORT).show()
            }
        }

        btnNext.setOnClickListener {
            val currentItem = vp.currentItem
            if (currentItem < pagerAdapter.itemCount - 1) {
                // Move to the next page
                vp.currentItem = currentItem + 1
            } else {
                val selectedBarangay = collectedData["barangay"] as? String
                val selectedSex = collectedData["sex"] as? String

                if (!selectedBarangay.isNullOrEmpty() && !selectedSex.isNullOrEmpty()) {
                    loadingDialog.show("Saving, please wait...")
                    userViewModel.updateSexAndBarangay(selectedSex, selectedBarangay, userId!!)
                } else {
                    loadingDialog.dismiss()
                    Snackbar.make(binding.root, "Please select barangay and sex", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDataCollected(data: Map<String, Any>) {
        collectedData.putAll(data)
    }
}