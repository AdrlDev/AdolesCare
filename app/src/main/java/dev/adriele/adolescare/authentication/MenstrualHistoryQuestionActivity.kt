package dev.adriele.adolescare.authentication

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
import dev.adriele.adolescare.Utility
import dev.adriele.adolescare.authentication.adapter.PagerAdapter
import dev.adriele.adolescare.authentication.contracts.FragmentDataListener
import dev.adriele.adolescare.authentication.fragments.femaleHistoryFragment.FirstPeriodReportedFragment
import dev.adriele.adolescare.database.AppDatabaseProvider
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
        btnNext.setOnClickListener {
            val currentItem = vp.currentItem
            if (currentItem < pagerAdapter.itemCount - 1) {
                // Move to the next page
                vp.currentItem = currentItem + 1
            } else {

            }
        }

    }

    override fun onDataCollected(data: Map<String, Any>) {
        collectedData.putAll(data)
        val firstPeriodReported = collectedData["firstPeriodReported"] as? Boolean
        firstPeriodReported?.let {
            updateFragments(firstPeriodReported)
        }
    }

    private fun updateFragments(hasPeriod: Boolean) {
        stepper.visibility = View.VISIBLE
        btnNext.visibility = View.VISIBLE
        binding.tvStep.visibility = View.VISIBLE

        fragments = if (hasPeriod == true) {
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