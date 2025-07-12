package dev.adriele.adolescare.authentication

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
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
import dev.adriele.adolescare.ui.DashboardActivity
import dev.adriele.adolescare.helpers.Utility
import dev.adriele.adolescare.authentication.adapter.PagerAdapter
import dev.adriele.adolescare.authentication.contracts.FragmentDataListener
import dev.adriele.adolescare.authentication.fragments.femaleHistory.FLastPeriodNoMsgFragment
import dev.adriele.adolescare.authentication.fragments.femaleHistory.FP2NoMessageFragment
import dev.adriele.adolescare.authentication.fragments.femaleHistory.FP3NoMessageFragment
import dev.adriele.adolescare.authentication.fragments.femaleHistory.FPeriodNoFragment
import dev.adriele.adolescare.authentication.fragments.femaleHistory.ResultFragment
import dev.adriele.adolescare.authentication.fragments.femaleHistory.FPeriodYes1Fragment
import dev.adriele.adolescare.authentication.fragments.femaleHistory.FPeriodYes2Fragment
import dev.adriele.adolescare.authentication.fragments.femaleHistory.FPeriodYes3Fragment
import dev.adriele.adolescare.authentication.fragments.femaleHistory.FemaleMenstrualHistory
import dev.adriele.adolescare.authentication.fragments.femaleHistory.FirstPeriodReportedFragment
import dev.adriele.adolescare.authentication.fragments.femaleHistory.LPLSelectDaysFragment
import dev.adriele.adolescare.authentication.fragments.femaleHistory.SelectLPSDateFragment
import dev.adriele.adolescare.authentication.fragments.femaleHistory.SelectNumberOfWeeksFragment
import dev.adriele.adolescare.authentication.fragments.maleHistory.MQ1Fragment
import dev.adriele.adolescare.authentication.fragments.maleHistory.MQ1TrackFragment
import dev.adriele.adolescare.authentication.fragments.maleHistory.MaleQuestions
import dev.adriele.adolescare.database.AppDatabaseProvider
import dev.adriele.adolescare.database.entities.MenstrualHistoryEntity
import dev.adriele.adolescare.database.repositories.implementation.MenstrualHistoryRepositoryImpl
import dev.adriele.adolescare.databinding.ActivityMenstrualHistoryQuestionBinding
import dev.adriele.adolescare.dialogs.MyLoadingDialog
import dev.adriele.adolescare.viewmodel.MenstrualHistoryViewModel
import dev.adriele.adolescare.viewmodel.factory.MenstrualHistoryViewModelFactory

class MenstrualHistoryQuestionActivity : BaseActivity(), FragmentDataListener {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedAxis = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        window.enterTransition = sharedAxis
        window.exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)

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
        initViewModel()
        setupViewPager()
        setupButton()
        observeViewModel()
    }

    private fun initViews() {
        vp = binding.viewpager
        stepper = binding.stepperContainer
        btnNext = binding.btnNext

        loadingDialog = MyLoadingDialog(this)

        userId = intent.getStringExtra("userId")
        userSex = intent.getStringExtra("userSex")
    }

    private fun initViewModel() {
        val dao = AppDatabaseProvider.getDatabase(this).menstrualHistoryDao()
        val repo = MenstrualHistoryRepositoryImpl(dao)
        val factory = MenstrualHistoryViewModelFactory(repo)
        menstrualHistoryViewModel = ViewModelProvider(this, factory)[MenstrualHistoryViewModel::class.java]
    }

    private fun setupViewPager() {
        pagerAdapter = PagerAdapter(this)
        vp.adapter = pagerAdapter

        val fragments = when(userSex) {
            getString(dev.adriele.language.R.string.female) -> listOf(FirstPeriodReportedFragment())
            else -> listOf(MQ1Fragment())
        }

        pagerAdapter.updateFragments(fragments)

        updateStepperUI(1, false)

        vp.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                Utility.updateStepper(position, pagerAdapter.itemCount, stepper, binding.tvStep, resources)
                btnNext.text = if (position == pagerAdapter.itemCount - 1) "Finish" else "Next"
            }
        })
    }

    private fun observeViewModel() {
        menstrualHistoryViewModel.insertStatus.observe(this) { (success, _) ->
            loadingDialog.dismiss()
            if (success) {
                val bundle = ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
                startActivity(Intent(this, DashboardActivity::class.java)
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    .putExtra("userId", userId),
                    bundle
                )
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
                saveData()
            }
        }
    }

    override fun onDataCollected(data: Map<String, Any>) {
        collectedData.putAll(data)

        val pages = when(userSex) {
            getString(dev.adriele.language.R.string.female) -> observeFemaleQ(collectedData).first
            else -> observeMaleQ(collectedData).first
        }
        val isFinish = when(userSex) {
            getString(dev.adriele.language.R.string.female) -> observeFemaleQ(collectedData).second
            else -> observeMaleQ(collectedData).second
        }

        setFragments(pages, isFinish)
    }

    private fun observeFemaleQ(map: MutableMap<String, Any>): Pair<MutableList<Fragment>, Boolean> {
        val pages = mutableListOf<Fragment>()

        val hasMenstrualPeriod = map[FemaleMenstrualHistory.FIRST_PERIOD.name] as? Boolean
        val lastPeriodStarted = map[FemaleMenstrualHistory.LAST_PERIOD_STARTED.name] as? Boolean
        val lastPeriodStartedDate = map[FemaleMenstrualHistory.LAST_PERIOD_STARTED_DATE.name] as? String
        val lastPeriodStartedChange = map[FemaleMenstrualHistory.CHANGE_LAST_PERIOD_STARTED.name] as? Boolean
        val lastPeriodLasted = map[FemaleMenstrualHistory.LAST_PERIOD_LASTED.name] as? Boolean
        val lastPeriodLastedNo = map[FemaleMenstrualHistory.LAST_PERIOD_LASTED_NO.name] as? Boolean
        val lastPeriodLastedDays = map[FemaleMenstrualHistory.LAST_PERIOD_LASTED_DAYS.name] as? Int
        val numberOfWeeks = map[FemaleMenstrualHistory.NUMBER_OF_WEEKS.name] as? Boolean
        val numberOfWeeksNo = map[FemaleMenstrualHistory.NUMBER_OF_WEEKS_NO.name] as? Boolean
        val numberOfWeeksSelected = map[FemaleMenstrualHistory.NUMBER_OF_WEEKS_SELECTED.name] as? Int

        Log.d("FlowDebug", "Collected: $map")

        var isFinish = false

        if (hasMenstrualPeriod == false && !map.containsKey(FemaleMenstrualHistory.LAST_PERIOD_STARTED.name)) {
            pages.clear()
            pages.addAll(
                listOf(FPeriodNoFragment(), ResultFragment.newInstance(false, lastPeriodStartedDate ?: "N/A", lastPeriodLastedDays ?: 0, numberOfWeeksSelected ?: 0))
            )
        }

        if (hasMenstrualPeriod == true && !map.containsKey(FemaleMenstrualHistory.LAST_PERIOD_STARTED.name)) {
            pages.clear()
            pages.add(FPeriodYes1Fragment())
        }

        if (lastPeriodStarted != null
            && !map.containsKey(FemaleMenstrualHistory.CHANGE_LAST_PERIOD_STARTED.name)) {
            pages.clear()
            pages.add(
                if (lastPeriodStarted) SelectLPSDateFragment()
                else FLastPeriodNoMsgFragment()
            )
        }

        if (lastPeriodStartedChange != null && !map.containsKey(FemaleMenstrualHistory.LAST_PERIOD_STARTED_DATE.name)) {
            pages.clear()
            pages.add(
                if (lastPeriodStartedChange) SelectLPSDateFragment()
                else FPeriodYes2Fragment()
            )
        }

        if (lastPeriodStartedDate != null && !map.containsKey(FemaleMenstrualHistory.LAST_PERIOD_LASTED.name)) {
            pages.clear()
            pages.add(FPeriodYes2Fragment())
        }

        if (lastPeriodLasted != null && lastPeriodLasted && !map.containsKey(FemaleMenstrualHistory.LAST_PERIOD_LASTED_DAYS.name)) {
            pages.clear()
            pages.add(LPLSelectDaysFragment())
        }

        if (lastPeriodLasted != null && !lastPeriodLasted && !map.containsKey(FemaleMenstrualHistory.LAST_PERIOD_LASTED_NO.name)) {
            //if user select no in last period lasted
            pages.clear()
            pages.add(FP2NoMessageFragment())
        }

        if(lastPeriodLastedNo != null && lastPeriodLasted != null) {
            pages.clear()
            if(lastPeriodLastedNo) {
                pages.add(LPLSelectDaysFragment())
            } else {
                pages.add(FPeriodYes3Fragment())
            }
        }

        if(lastPeriodLastedDays != null) {
            pages.clear()
            pages.add(FPeriodYes3Fragment())
        }

        if(numberOfWeeks != null && numberOfWeeks && !map.containsKey(FemaleMenstrualHistory.NUMBER_OF_WEEKS_SELECTED.name)) {
            pages.clear()
            pages.add(SelectNumberOfWeeksFragment())
        }

        if(numberOfWeeks != null && !numberOfWeeks && !map.containsKey(FemaleMenstrualHistory.NUMBER_OF_WEEKS_NO.name)) {
            pages.clear()
            pages.add(FP3NoMessageFragment())
        }

        if(numberOfWeeksNo != null && numberOfWeeks != null) {
            pages.clear()
            if(numberOfWeeksNo) {
                pages.add(SelectNumberOfWeeksFragment())
            } else {
                if(numberOfWeeksSelected != null && lastPeriodLastedDays != null && lastPeriodStartedDate != null) {
                    isFinish = true
                    pages.clear()
                    pages.add(ResultFragment.newInstance(
                        hasPeriod = hasMenstrualPeriod == true,
                        lastPeriodStarted = lastPeriodStartedDate,
                        numberOfDays = lastPeriodLastedDays,
                        numberOfWeeks = numberOfWeeksSelected
                    ))
                }
            }
        }

        if(numberOfWeeksSelected != null && lastPeriodLastedDays != null && lastPeriodStartedDate != null) {
            isFinish = true
            pages.clear()
            pages.add(ResultFragment.newInstance(
                hasPeriod = hasMenstrualPeriod == true,
                lastPeriodStarted = lastPeriodStartedDate,
                numberOfDays = lastPeriodLastedDays,
                numberOfWeeks = numberOfWeeksSelected
            ))
        }

        if (pages.isEmpty()) {
            Log.w("FlowDebug", "No fragment condition matched")
        }

        return Pair(pages, isFinish)
    }

    private fun observeMaleQ(map: MutableMap<String, Any>): Pair<MutableList<Fragment>, Boolean> {
        val pages = mutableListOf<Fragment>()

        val maleQ1 = map["male_q1"] as? Int
        val maleQ1Track = map["male_q1_track"] as? Int

        Log.d("FlowDebug", "Collected: $map")

        var isFinish = false

        if (maleQ1 == MaleQuestions.TRACK_PARTNERS_MENS.value && maleQ1 != MaleQuestions.WANT_TO_LEARN_ABOUT_PREGNANCY.value) {
            pages.clear()
            pages.add(MQ1TrackFragment())
        }

        if (maleQ1Track == MaleQuestions.CONFIRM.value) {
            pages.clear()
            pages.add(FirstPeriodReportedFragment())
            userSex = getString(dev.adriele.language.R.string.female)
        }

        if (maleQ1 != MaleQuestions.TRACK_PARTNERS_MENS.value && maleQ1 == MaleQuestions.WANT_TO_LEARN_ABOUT_PREGNANCY.value) {
            pages.clear()
            isFinish = true
        }

        if (pages.isEmpty()) {
            Log.w("FlowDebug", "No fragment condition matched")
        }

        return Pair(pages, isFinish)
    }

    private fun setFragments(pages: List<Fragment>, isFinish: Boolean) {
        pagerAdapter.updateFragments(pages)
        vp.currentItem = 0

        val stepCount = if (isFinish) 1 else pagerAdapter.itemCount
        updateStepperUI(stepCount, isFinish)
        btnNext.text = if (stepCount == 1) "Finish" else "Next"

        Utility.setupStepper(stepCount, resources, this, stepper)
    }

    private fun updateStepperUI(stepCount: Int, isFinish: Boolean) {
        val shouldShow = stepCount > 1 || isFinish
        stepper.visibility = if (shouldShow) View.VISIBLE else View.GONE
        binding.tvStep.visibility = if (shouldShow) View.VISIBLE else View.GONE
        btnNext.visibility = if (shouldShow) View.VISIBLE else View.GONE

        Utility.updateStepper(0, stepCount, stepper, binding.tvStep, resources)
    }

    private fun saveData() {
        val lpsDate = collectedData[FemaleMenstrualHistory.LAST_PERIOD_STARTED_DATE.name] as? String
        val lplDays = collectedData[FemaleMenstrualHistory.LAST_PERIOD_LASTED_DAYS.name] as? Int
        val lplWeeks = collectedData[FemaleMenstrualHistory.NUMBER_OF_WEEKS_SELECTED.name] as? Int

        val data = MenstrualHistoryEntity(
            userId = userId ?: return,
            firstPeriodReported = collectedData[FemaleMenstrualHistory.FIRST_PERIOD.name] as? Boolean == true,
            lastPeriodStart = lpsDate,
            periodDurationDays = lplDays,
            cycleIntervalWeeks = lplWeeks
        )

        loadingDialog.show("Saving, please wait...")
        menstrualHistoryViewModel.insertMenstrualHistory(data)
    }
}