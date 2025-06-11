package dev.adriele.adolescare.authentication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.snackbar.Snackbar
import dev.adriele.adolescare.DashboardActivity
import dev.adriele.adolescare.Utility
import dev.adriele.adolescare.authentication.adapter.PagerAdapter
import dev.adriele.adolescare.authentication.contracts.FragmentDataListener
import dev.adriele.adolescare.authentication.fragments.femaleHistoryFragment.FLastPeriodNoMsgFragment
import dev.adriele.adolescare.authentication.fragments.femaleHistoryFragment.FPeriodNoFragment
import dev.adriele.adolescare.authentication.fragments.femaleHistoryFragment.FPeriodNoLastFragment
import dev.adriele.adolescare.authentication.fragments.femaleHistoryFragment.FPeriodYes1Fragment
import dev.adriele.adolescare.authentication.fragments.femaleHistoryFragment.FemaleMenstrualHistory
import dev.adriele.adolescare.authentication.fragments.femaleHistoryFragment.FirstPeriodReportedFragment
import dev.adriele.adolescare.database.AppDatabaseProvider
import dev.adriele.adolescare.database.entities.MenstrualHistoryEntity
import dev.adriele.adolescare.database.repositories.implementation.MenstrualHistoryRepositoryImpl
import dev.adriele.adolescare.databinding.ActivityMenstrualHistoryQuestionBinding
import dev.adriele.adolescare.dialogs.MyLoadingDialog
import dev.adriele.adolescare.viewmodel.MenstrualHistoryViewModel
import dev.adriele.adolescare.viewmodel.factory.MenstrualHistoryViewModelFactory

class MenstrualHistoryQuestionActivity : AppCompatActivity(), FragmentDataListener {
    private lateinit var binding: ActivityMenstrualHistoryQuestionBinding

    private lateinit var vp: ViewPager2
    private lateinit var stepper: LinearLayout
    private lateinit var btnNext: Button

    private lateinit var pagerAdapter: PagerAdapter
    private lateinit var fragments: List<Fragment>

    private val collectedData = mutableMapOf<String, Any>()

    private lateinit var menstrualHistoryRepositoryImpl: MenstrualHistoryRepositoryImpl
    private lateinit var menstrualHistoryViewModel: MenstrualHistoryViewModel

    private lateinit var loadingDialog: MyLoadingDialog

    private var userId: String? = null
    private var userSex: String? = null
    private var hasMenstrualPeriod: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMenstrualHistoryQuestionBinding.inflate(layoutInflater)
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

        intent.getStringExtra("userSex")?.let {
            userSex = it
        }

        loadingDialog = MyLoadingDialog(this)

        //default fragment
        fragments = listOf(
            FirstPeriodReportedFragment()
        )

        pagerAdapter = PagerAdapter(this, fragments)
        vp.adapter = pagerAdapter

        stepper.visibility = View.GONE
        btnNext.visibility = View.GONE
        binding.tvStep.visibility = View.GONE

        // Initialize stepper indicators
        Utility.setupStepper(pagerAdapter.itemCount, resources, this, stepper)

        // Change button text on the last page
        vp.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                Utility.updateStepper(position, pagerAdapter.itemCount, stepper, binding.tvStep, resources)
                btnNext.text = if (position == pagerAdapter.itemCount - 1) "Finish" else "Next"
            }
        })

        val menstrualDao = AppDatabaseProvider.getDatabase(this).menstrualHistoryDao()
        menstrualHistoryRepositoryImpl = MenstrualHistoryRepositoryImpl(menstrualDao)

        val factory = MenstrualHistoryViewModelFactory(menstrualHistoryRepositoryImpl)
        menstrualHistoryViewModel = ViewModelProvider(this, factory)[MenstrualHistoryViewModel::class.java]
    }

    private fun afterInit() {
        menstrualHistoryViewModel.insertStatus.observe(this) { (success, hasPeriod) ->
            if(success) {
                loadingDialog.dismiss()
                if(!hasPeriod) {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                }
            } else {
                loadingDialog.dismiss()
                Snackbar.make(binding.main, "Failed to save menstrual history...", Snackbar.LENGTH_LONG).show()
            }
        }

        btnNext.setOnClickListener {
            val currentItem = vp.currentItem
            if (currentItem < pagerAdapter.itemCount - 1) {
                // Move to the next page
                vp.currentItem = currentItem + 1
            } else {
                loadingDialog.show("Saving, please wait...")

                hasMenstrualPeriod?.let { hasPeriod ->
                    if(!hasPeriod) {
                        val mensHistoryData = MenstrualHistoryEntity(
                            userId = userId!!,
                            firstPeriodReported = false,
                            lastPeriodStart = null,
                            periodDurationDays = 0,
                            cycleIntervalWeeks = 0
                        )

                        menstrualHistoryViewModel.insertMenstrualHistory(mensHistoryData)
                    } else {
                        val mensHistoryData = MenstrualHistoryEntity(
                            userId = userId!!,
                            firstPeriodReported = true,
                            lastPeriodStart = null,
                            periodDurationDays = 0,
                            cycleIntervalWeeks = 0
                        )

                        menstrualHistoryViewModel.insertMenstrualHistory(mensHistoryData)
                    }
                }
            }
        }

    }

    override fun onDataCollected(data: Map<String, Any>) {
        collectedData.putAll(data)
        hasMenstrualPeriod = collectedData[FemaleMenstrualHistory.FIRST_PERIOD.name] as? Boolean
        hasMenstrualPeriod?.let {
            updateFragments(it)
        }

        val lastPeriodStarted = collectedData[FemaleMenstrualHistory.LAST_PERIOD_STARTED.name] as? Boolean
        lastPeriodStarted?.let {
            updateLastPeriodStartedFragments(it)
        }

        val lastPeriodStartedChange = collectedData[FemaleMenstrualHistory.CHANGE_LAST_PERIOD_STARTED.name] as? Boolean
        lastPeriodStartedChange?.let {
            updateLastPeriodStartedChangeFragments(it)
        }
    }

    private fun updateFragments(hasPeriod: Boolean) {
        stepper.visibility = View.VISIBLE
        btnNext.visibility = View.VISIBLE
        binding.tvStep.visibility = View.VISIBLE

        fragments = if (hasPeriod == true) {
            stepper.visibility = View.GONE
            btnNext.visibility = View.GONE
            binding.tvStep.visibility = View.GONE

            listOf(
                FPeriodYes1Fragment()
            )
        } else {
            listOf(
                FPeriodNoFragment(),
                FPeriodNoLastFragment.newInstance(hasPeriod = false)
            )
        }

        pagerAdapter = PagerAdapter(this, fragments)
        vp.adapter = pagerAdapter

        // Reinitialize stepper indicators
        stepper.removeAllViews()
        Utility.setupStepper(pagerAdapter.itemCount, resources, this, stepper)

        // Reset the ViewPager to the first page
        vp.currentItem = 0
    }

    private fun updateLastPeriodStartedFragments(remember: Boolean) {
        stepper.visibility = View.VISIBLE
        btnNext.visibility = View.VISIBLE
        binding.tvStep.visibility = View.VISIBLE

        fragments = if (remember == true) {
            listOf(

            )
        } else {
            listOf(
                FLastPeriodNoMsgFragment()
            )
        }

        pagerAdapter = PagerAdapter(this, fragments)
        vp.adapter = pagerAdapter

        // Reinitialize stepper indicators
        stepper.removeAllViews()
        Utility.setupStepper(pagerAdapter.itemCount, resources, this, stepper)

        // Reset the ViewPager to the first page
        vp.currentItem = 0
    }

    private fun updateLastPeriodStartedChangeFragments(change: Boolean) {
        stepper.visibility = View.VISIBLE
        btnNext.visibility = View.VISIBLE
        binding.tvStep.visibility = View.VISIBLE

        fragments = if (change == true) {
            listOf(

            )
        } else {
            listOf(

            )
        }

        pagerAdapter = PagerAdapter(this, fragments)
        vp.adapter = pagerAdapter

        // Reinitialize stepper indicators
        stepper.removeAllViews()
        Utility.setupStepper(pagerAdapter.itemCount, resources, this, stepper)

        // Reset the ViewPager to the first page
        vp.currentItem = 0
    }
}