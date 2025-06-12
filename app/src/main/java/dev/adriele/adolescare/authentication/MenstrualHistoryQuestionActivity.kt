package dev.adriele.adolescare.authentication

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import dev.adriele.adolescare.authentication.fragments.femaleHistoryFragment.FPeriodYes2Fragment
import dev.adriele.adolescare.authentication.fragments.femaleHistoryFragment.FemaleMenstrualHistory
import dev.adriele.adolescare.authentication.fragments.femaleHistoryFragment.FirstPeriodReportedFragment
import dev.adriele.adolescare.authentication.fragments.femaleHistoryFragment.SelectLPSDateFragment
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

    private val collectedData = mutableMapOf<String, Any>()

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
        userId = intent.getStringExtra("userId")
        userSex = intent.getStringExtra("userSex")

        loadingDialog = MyLoadingDialog(this)

        val menstrualDao = AppDatabaseProvider.getDatabase(this).menstrualHistoryDao()
        val menstrualHistoryRepositoryImpl = MenstrualHistoryRepositoryImpl(menstrualDao)

        val factory = MenstrualHistoryViewModelFactory(menstrualHistoryRepositoryImpl)
        menstrualHistoryViewModel = ViewModelProvider(this, factory)[MenstrualHistoryViewModel::class.java]
    }

    private fun afterInit() {
        setupViewPager()
        observeViewModel()
        setupButton()
    }

    private fun setupViewPager() {
        pagerAdapter = PagerAdapter(this)
        vp.adapter = pagerAdapter

        vp.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                Utility.updateStepper(position, pagerAdapter.itemCount, stepper, binding.tvStep, resources)
                btnNext.text = if (position == pagerAdapter.itemCount - 1) "Finish" else "Next"
            }
        })

        Utility.setupStepper(pagerAdapter.itemCount, resources, this, stepper)
        binding.tvStep.visibility = View.GONE
        stepper.visibility = View.GONE
        btnNext.visibility = View.GONE

        pagerAdapter.updateFragments(listOf(FirstPeriodReportedFragment()))
    }

    private fun observeViewModel() {
        menstrualHistoryViewModel.insertStatus.observe(this) { (success, hasPeriod) ->
            loadingDialog.dismiss()
            if (success) {
                if (!hasPeriod) {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                }
            } else {
                Snackbar.make(binding.main, "Failed to save menstrual history...", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun setupButton() {
        btnNext.setOnClickListener {
            val current = vp.currentItem
            if (current < pagerAdapter.itemCount - 1) {
                vp.currentItem = current + 1
            } else {
                val lpsDate = collectedData[FemaleMenstrualHistory.LAST_PERIOD_STARTED_DATE.name] as? String
                val data = MenstrualHistoryEntity(
                    userId = userId ?: return@setOnClickListener,
                    firstPeriodReported = hasMenstrualPeriod == true,
                    lastPeriodStart = lpsDate,
                    periodDurationDays = 0,
                    cycleIntervalWeeks = 0
                )
                loadingDialog.show("Saving, please wait...")
                menstrualHistoryViewModel.insertMenstrualHistory(data)
            }
        }
    }

    override fun onDataCollected(data: Map<String, Any>) {
        collectedData.putAll(data)
        val pages = mutableListOf<Fragment>()

        val hasMenstrualPeriod = collectedData[FemaleMenstrualHistory.FIRST_PERIOD.name] as? Boolean
        val lastPeriodStarted = collectedData[FemaleMenstrualHistory.LAST_PERIOD_STARTED.name] as? Boolean
        val nextClicked = collectedData[FemaleMenstrualHistory.LAST_PERIOD_STARTED_DATE_NEXT.name] as? Boolean
        val lastPeriodStartedChange = collectedData[FemaleMenstrualHistory.CHANGE_LAST_PERIOD_STARTED.name] as? Boolean

        Log.d("FlowDebug", "Collected: $collectedData")

        if (hasMenstrualPeriod == false && !collectedData.containsKey(FemaleMenstrualHistory.LAST_PERIOD_STARTED.name)) {
            pages.clear()
            pages.addAll(
                listOf(FPeriodNoFragment(), FPeriodNoLastFragment.newInstance(false))
            )
        }

        if (hasMenstrualPeriod == true && !collectedData.containsKey(FemaleMenstrualHistory.LAST_PERIOD_STARTED.name)) {
            pages.clear()
            pages.add(FPeriodYes1Fragment())
        }

        if (lastPeriodStarted != null
            && !collectedData.containsKey(FemaleMenstrualHistory.CHANGE_LAST_PERIOD_STARTED.name)) {
            pages.clear()
            pages.add(
                if (lastPeriodStarted) SelectLPSDateFragment()
                else FLastPeriodNoMsgFragment()
            )
        }

        if (lastPeriodStartedChange != null && !collectedData.containsKey(FemaleMenstrualHistory.LAST_PERIOD_STARTED_DATE_NEXT.name)) {
            pages.clear()
            pages.add(
                if (lastPeriodStartedChange) SelectLPSDateFragment()
                else FPeriodYes2Fragment()
            )
        }

        if (nextClicked != null) {
            pages.clear()
            pages.add(FPeriodYes2Fragment())
        }

        if (pages.isEmpty()) {
            Log.w("FlowDebug", "No fragment condition matched")
        }

        setFragments(pages)
    }

    private fun setFragments(pages: List<Fragment>) {
        pagerAdapter.updateFragments(pages)
        vp.currentItem = 0

        val showStepper = pages.size > 1
        stepper.visibility = if (showStepper) View.VISIBLE else View.GONE
        binding.tvStep.visibility = if (showStepper) View.VISIBLE else View.GONE
        btnNext.visibility = if (showStepper) View.VISIBLE else View.GONE

        Utility.updateStepper(0, pages.size, stepper, binding.tvStep, resources)
        btnNext.text = if (pages.size == 1) "Finish" else "Next"

        Utility.setupStepper(pages.size, resources, this, stepper)
    }

}