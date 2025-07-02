package dev.adriele.adolescare.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dev.adriele.adolescare.adapter.SymptomsActivitiesAdapter
import dev.adriele.adolescare.database.AppDatabaseProvider
import dev.adriele.adolescare.database.repositories.implementation.CycleLogRepositoryImpl
import dev.adriele.adolescare.databinding.ActivityAddSymptomsBinding
import dev.adriele.adolescare.model.SymptomsActivitiesQ
import dev.adriele.adolescare.viewmodel.CycleLogViewModel
import dev.adriele.adolescare.viewmodel.factory.CycleLogViewModelFactory
import kotlinx.coroutines.launch

class AddSymptomsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddSymptomsBinding

    private lateinit var cycleLogViewModel: CycleLogViewModel

    private var userId: String? = null
    private var dateCycle: String? = null
    private var cycleDay: Int = 0

    private var symptomsActivitiesQ: MutableList<SymptomsActivitiesQ> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
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
    }

    private fun addSymptomsActivity() {
        symptomsActivitiesQ.clear()
        val listOfCategories = resources.getStringArray(dev.adriele.language.R.array.categories).toList()

        listOfCategories.forEach { category ->
            when(category) {
                getString(dev.adriele.language.R.string.sex_drive) -> {
                    val listOfSexDrives = resources.getStringArray(dev.adriele.language.R.array.sex_and_drive_options).toList()
                    val symptomsActivitiesQData = SymptomsActivitiesQ(
                        category = category,
                        listOfSexDrives
                    )
                    symptomsActivitiesQ.add(symptomsActivitiesQData)
                }
                getString(dev.adriele.language.R.string.mood) -> {
                    val listOfSexDrives = resources.getStringArray(dev.adriele.language.R.array.mood_options).toList()
                    val symptomsActivitiesQData = SymptomsActivitiesQ(
                        category = category,
                        listOfSexDrives
                    )
                    symptomsActivitiesQ.add(symptomsActivitiesQData)
                }
                getString(dev.adriele.language.R.string.symptoms_category) -> {
                    val listOfSymptoms = resources.getStringArray(dev.adriele.language.R.array.symptoms_list).toList()
                    val symptomsActivitiesQData = SymptomsActivitiesQ(
                        category = category,
                        listOfSymptoms
                    )
                    symptomsActivitiesQ.add(symptomsActivitiesQData)
                }
            }
        }
    }

    private fun setupSymptomsRecyclerView(preselected: Map<String, List<String>>) {
        val adapter = SymptomsActivitiesAdapter(symptomsActivitiesQ, preselected, false) { category, selected ->
            when(category) {
                getString(dev.adriele.language.R.string.sex_drive) -> {
                    updateCycleLog(sexActivity = selected)
                }
                getString(dev.adriele.language.R.string.mood) -> {
                    updateCycleLog(mood = selected)
                }
                getString(dev.adriele.language.R.string.symptoms_category) -> {
                    updateCycleLog(symptoms = selected)
                }
            }
        }

        binding.rvSymptoms.adapter = adapter
        binding.rvSymptoms.layoutManager = LinearLayoutManager(this)
    }

    private fun initializeViewModel() {
        val cycleLogDao = AppDatabaseProvider.getDatabase(this).cycleLogDao()
        val cycleDao = AppDatabaseProvider.getDatabase(this).cycleDao()
        val cycleRepository = CycleLogRepositoryImpl(cycleLogDao, cycleDao)
        val cycleViewModelFactory = CycleLogViewModelFactory(cycleRepository)
        cycleLogViewModel = ViewModelProvider(this, cycleViewModelFactory)[CycleLogViewModel::class]
    }

    private fun extractIntentExtras() {
        binding.tvDayCycle.text = intent.getStringExtra("cycle")
        userId = intent.getStringExtra("userId")
        dateCycle = intent.getStringExtra("dateCycle")
        cycleDay = intent.getIntExtra("cycleDay", 0)
    }

    private fun updateCycleLog(
        sexActivity: List<String>? = null,
        mood: List<String>? = null,
        symptoms: List<String>? = null
    ) {
        userId?.let { uid ->
            dateCycle?.let { cycleDate ->
                lifecycleScope.launch {
                    cycleLogViewModel.getLogByDate(uid, cycleDate).observe(this@AddSymptomsActivity) { cycleLogs ->
                        // Keep existing values if not being updated
                        val updatedSexActivity = sexActivity ?: cycleLogs?.sexActivity
                        val updatedMood = mood ?: cycleLogs?.mood
                        val updatedSymptom = symptoms ?: cycleLogs?.symptoms

                        cycleLogViewModel.updateListsByUserIdAndDate(
                            userId = uid,
                            date = cycleDate,
                            dayCycle = cycleDay,
                            sexActivity = updatedSexActivity,
                            mood = updatedMood,
                            symptoms = updatedSymptom
                        )
                    }
                }
            }
        }
    }

    private fun loadAndPrefillCycleData() {
        userId?.let { uid ->
            dateCycle?.let { cycleDate ->
                lifecycleScope.launch {
                    cycleLogViewModel.getLogByDate(uid, cycleDate).observe(this@AddSymptomsActivity) { logCycle ->
                        logCycle?.let {
                            cycleDay = it.cycleDay
                            val selectedMap = mutableMapOf<String, List<String>>()
                            selectedMap[getString(dev.adriele.language.R.string.sex_drive)] = it.sexActivity ?: emptyList()
                            selectedMap[getString(dev.adriele.language.R.string.mood)] = it.mood ?: emptyList()
                            selectedMap[getString(dev.adriele.language.R.string.symptoms_category)] = it.symptoms ?: emptyList()

                            setupSymptomsRecyclerView(selectedMap)
                        }
                    }
                }
            }
        }
    }

}