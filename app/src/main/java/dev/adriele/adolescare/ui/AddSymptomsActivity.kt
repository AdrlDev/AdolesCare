package dev.adriele.adolescare.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.transition.platform.MaterialSharedAxis
import dev.adriele.adolescare.BaseActivity
import dev.adriele.adolescare.adapter.SymptomsActivitiesAdapter
import dev.adriele.adolescare.database.AppDatabaseProvider
import dev.adriele.adolescare.database.repositories.implementation.CycleLogRepositoryImpl
import dev.adriele.adolescare.database.repositories.implementation.ReminderRepositoryImpl
import dev.adriele.adolescare.databinding.ActivityAddSymptomsBinding
import dev.adriele.adolescare.helpers.Utility
import dev.adriele.adolescare.helpers.enums.SymptomCategory
import dev.adriele.adolescare.model.SymptomsActivitiesQ
import dev.adriele.adolescare.viewmodel.CycleLogViewModel
import dev.adriele.adolescare.viewmodel.ReminderViewModel
import dev.adriele.adolescare.viewmodel.factory.CycleLogViewModelFactory
import dev.adriele.adolescare.viewmodel.factory.ReminderViewModelFactory
import dev.adriele.language.R
import kotlinx.coroutines.launch

class AddSymptomsActivity : BaseActivity() {
    private lateinit var binding: ActivityAddSymptomsBinding
    private lateinit var cycleLogViewModel: CycleLogViewModel
    private lateinit var reminderViewModel: ReminderViewModel
    private var userId: String? = null
    private var dateCycle: String? = null
    private var cycleDay: Int = 0

    private var symptomsActivitiesQ: MutableList<SymptomsActivitiesQ> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedAxis = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        window.enterTransition = sharedAxis
        window.exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddSymptomsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        addSymptomsActivity()

        extractIntentExtras()
        initializeViewModel()
        loadAndPrefillCycleData()

        setSupportActionBar(binding.topAppBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.topAppBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun addSymptomsActivity() {
        symptomsActivitiesQ.clear()
        SymptomCategory.entries.forEach { category ->
            val options = category.options.map { getString(it.resId) }

            val symptomsActivitiesQData = SymptomsActivitiesQ(
                category = category.name,
                choices = options
            )

            symptomsActivitiesQ.add(symptomsActivitiesQData)
        }
    }

    private fun setupSymptomsRecyclerView(preselected: Map<String, List<String>>) {
        Log.d("SYMPTOMS_SIZE", "symptomsActivitiesQ size: ${symptomsActivitiesQ.size}")

        val adapter = SymptomsActivitiesAdapter(symptomsActivitiesQ, preselected, false) { category, selected ->
            when(category) {
                SymptomCategory.SEX_DRIVE.name -> {
                    updateCycleLog(sexActivity = selected)
                }
                SymptomCategory.MOOD.name -> {
                    updateCycleLog(mood = selected)
                }
                SymptomCategory.SYMPTOMS.name -> {
                    updateCycleLog(symptoms = selected)
                }
                SymptomCategory.VAGINAL_DISCHARGE.name -> {
                    updateCycleLog(vaginalDischarge = selected)
                }
                SymptomCategory.DIGESTION_AND_STOOL.name -> {
                    updateCycleLog(digestionStool = selected)
                }
                SymptomCategory.PREGNANCY_TEST.name -> {
                    updateCycleLog(pregnancyTest = selected)
                }
                SymptomCategory.PHYSICAL_ACTIVITY.name -> {
                    updateCycleLog(physicalActivity = selected)
                }
            }
        }

        val layoutManager = object : LinearLayoutManager(this) {
            override fun onMeasure(
                recycler: RecyclerView.Recycler,
                state: RecyclerView.State,
                widthSpec: Int,
                heightSpec: Int
            ) {
                var totalHeight = 0
                val width = View.MeasureSpec.getSize(widthSpec)

                if (itemCount > 0) {
                    for (i in 0 until itemCount) {
                        try {
                            val view = recycler.getViewForPosition(i)
                            val layoutParams = view.layoutParams as RecyclerView.LayoutParams

                            val childWidthSpec = ViewGroup.getChildMeasureSpec(
                                widthSpec,
                                paddingLeft + paddingRight,
                                layoutParams.width
                            )
                            val childHeightSpec = ViewGroup.getChildMeasureSpec(
                                heightSpec,
                                paddingTop + paddingBottom,
                                layoutParams.height
                            )

                            view.measure(childWidthSpec, childHeightSpec)
                            totalHeight += view.measuredHeight + layoutParams.topMargin + layoutParams.bottomMargin
                        } catch (_: IndexOutOfBoundsException) {
                            // Adapter data not ready yet
                            break
                        }
                    }
                }

                setMeasuredDimension(width, totalHeight)
            }
        }
        binding.rvSymptoms.isNestedScrollingEnabled = false
        binding.rvSymptoms.adapter = adapter
        binding.rvSymptoms.layoutManager = layoutManager
    }

    private fun initializeViewModel() {
        val cycleLogDao = AppDatabaseProvider.getDatabase(this).cycleLogDao()
        val cycleDao = AppDatabaseProvider.getDatabase(this).cycleDao()
        val cycleRepository = CycleLogRepositoryImpl(cycleLogDao, cycleDao)
        val cycleViewModelFactory = CycleLogViewModelFactory(cycleRepository)
        cycleLogViewModel = ViewModelProvider(this, cycleViewModelFactory)[CycleLogViewModel::class]

        val reminderDao = AppDatabaseProvider.getDatabase(this).reminderDao()
        val reminderRepo = ReminderRepositoryImpl(reminderDao)
        val reminderFactory = ReminderViewModelFactory(reminderRepo)
        reminderViewModel = ViewModelProvider(this, reminderFactory)[ReminderViewModel::class]
    }

    @SuppressLint("SetTextI18n")
    private fun extractIntentExtras() {
        binding.tvDayCycle.text = intent.getStringExtra("cycle")
        userId = intent.getStringExtra("userId")
        dateCycle = intent.getStringExtra("dateCycle")
        cycleDay = intent.getIntExtra("cycleDay", 0)

        val now = Utility.getCurrentCycleDate()
        binding.topAppBar.title = getString(R.string.today) + "($now)"
    }

    private fun updateCycleLog(
        sexActivity: List<String>? = null,
        mood: List<String>? = null,
        symptoms: List<String>? = null,
        vaginalDischarge: List<String>? = null,
        digestionStool: List<String>? = null,
        pregnancyTest: List<String>? = null,
        physicalActivity: List<String>? = null
    ) {
        userId?.let { uid ->
            dateCycle?.let { cycleDate ->
                lifecycleScope.launch {
                    val existingLog = cycleLogViewModel.getLogByDateNow(uid, cycleDate)

                    val updatedSexActivity = sexActivity ?: existingLog?.sexActivity
                    val updatedMood = mood ?: existingLog?.mood
                    val updatedSymptom = symptoms ?: existingLog?.symptoms
                    val vaginalDischarge = vaginalDischarge ?: existingLog?.vaginalDischarge
                    val digestionStool = digestionStool ?: existingLog?.digestionAndStool
                    val pregnancyTest = pregnancyTest ?: existingLog?.pregnancyTestResult
                    val physicalActivity = physicalActivity ?: existingLog?.physicalActivity

                    cycleLogViewModel.updateListsByUserIdAndDate(
                        userId = uid,
                        date = cycleDate,
                        dayCycle = cycleDay,
                        sexActivity = updatedSexActivity,
                        mood = updatedMood,
                        symptoms = updatedSymptom,
                        vaginalDischarge = vaginalDischarge,
                        digestionAndStool = digestionStool,
                        pregnancyTestResult = pregnancyTest,
                        physicalActivity = physicalActivity
                    )
                }
            }
        }
    }

    private fun loadAndPrefillCycleData() {
        lifecycleScope.launch {
            userId?.let { uid ->
                dateCycle?.let { cycleDate ->
                    val logCycle = cycleLogViewModel.getLogByDate(uid, cycleDate)
                    logCycle?.let {
                        cycleDay = it.cycleDay
                        val selectedMap = mutableMapOf<String, List<String>>()
                        selectedMap[SymptomCategory.SEX_DRIVE.name] = it.sexActivity ?: emptyList()
                        selectedMap[SymptomCategory.MOOD.name] = it.mood ?: emptyList()
                        selectedMap[SymptomCategory.SYMPTOMS.name] = it.symptoms ?: emptyList()
                        selectedMap[SymptomCategory.VAGINAL_DISCHARGE.name] = it.vaginalDischarge ?: emptyList()
                        selectedMap[SymptomCategory.DIGESTION_AND_STOOL.name] = it.digestionAndStool ?: emptyList()
                        selectedMap[SymptomCategory.PREGNANCY_TEST.name] = it.pregnancyTestResult ?: emptyList()
                        selectedMap[SymptomCategory.PHYSICAL_ACTIVITY.name] = it.physicalActivity ?: emptyList()

                        setupSymptomsRecyclerView(selectedMap)
                    }
                }
            }
        }
    }

}