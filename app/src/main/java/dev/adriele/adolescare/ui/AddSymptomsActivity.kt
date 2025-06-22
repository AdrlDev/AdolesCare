package dev.adriele.adolescare.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.gson.Gson
import dev.adriele.adolescare.R
import dev.adriele.adolescare.database.AppDatabaseProvider
import dev.adriele.adolescare.database.repositories.implementation.CycleLogRepositoryImpl
import dev.adriele.adolescare.databinding.ActivityAddSymptomsBinding
import dev.adriele.adolescare.viewmodel.CycleLogViewModel
import dev.adriele.adolescare.viewmodel.factory.CycleLogViewModelFactory
import kotlinx.coroutines.launch

class AddSymptomsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddSymptomsBinding

    private lateinit var cycleLogViewModel: CycleLogViewModel

    private var sexDriveList: MutableList<String> = mutableListOf()
    private var moodList: MutableList<String> = mutableListOf()

    private var userId: String? = null
    private var dateCycle: String? = null
    private var cycleDay: Int = 0

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

        extractIntentExtras()
        initializeViewModel()
        setupChipGroups()
        loadAndPrefillCycleData()
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

    private fun setupChipGroups() {
        setupChipGroup(
            chipGroup = binding.chipGroupSexDrive,
            selectedList = sexDriveList,
            onUpdate = { updatedList ->
                updateCycleLog(sexActivity = updatedList)
                Log.d("SELECTED_CHIP_SEX", Gson().toJson(updatedList))
            }
        )

        setupChipGroup(
            chipGroup = binding.chipGroupMood,
            selectedList = moodList,
            onUpdate = { updatedList ->
                updateCycleLog(mood = updatedList)
                Log.d("SELECTED_CHIP_MOOD", Gson().toJson(updatedList))
            }
        )
    }

    private fun setupChipGroup(
        chipGroup: ChipGroup,
        selectedList: MutableList<String>,
        onUpdate: (List<String>) -> Unit
    ) {
        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            selectedList.clear()
            for (i in 0 until group.childCount) {
                val chip = group.getChildAt(i) as Chip
                if (chip.id in checkedIds) {
                    selectedList.add(chip.text.toString())
                    chip.setChipIconResource(R.drawable.round_check_20)
                    chip.isChipIconVisible = true
                } else {
                    chip.chipIcon = null
                }
            }
            onUpdate(selectedList)
        }
    }

    private fun updateCycleLog(
        sexActivity: List<String>? = null,
        mood: List<String>? = null
    ) {
        if (userId != null && dateCycle != null) {
            lifecycleScope.launch {
                cycleLogViewModel.getLogByDate(userId!!, dateCycle!!).observe(this@AddSymptomsActivity) { cycleLogs ->
                    // Keep existing values if not being updated
                    val updatedSexActivity = sexActivity ?: cycleLogs?.sexActivity
                    val updatedMood = mood ?: cycleLogs?.mood

                    cycleLogViewModel.updateListsByUserIdAndDate(
                        userId = userId!!,
                        date = dateCycle!!,
                        dayCycle = cycleDay,
                        sexActivity = updatedSexActivity,
                        mood = updatedMood
                    )
                }
            }
        }
    }

    private fun loadAndPrefillCycleData() {
        if (userId != null && dateCycle != null) {
            lifecycleScope.launch {
                cycleLogViewModel.getLogByDate(userId!!, dateCycle!!).observe(this@AddSymptomsActivity) { logCycle ->
                    logCycle.let {
                        cycleDay = it?.cycleDay!! // ensure it's up-to-date
                        preselectChips(binding.chipGroupSexDrive, it.sexActivity, sexDriveList)
                        preselectChips(binding.chipGroupMood, it.mood, moodList)
                    }
                }
            }
        }
    }

    private fun preselectChips(
        chipGroup: ChipGroup,
        values: List<String>?,
        selectedList: MutableList<String>
    ) {
        selectedList.clear()
        if (!values.isNullOrEmpty()) {
            for (i in 0 until chipGroup.childCount) {
                val chip = chipGroup.getChildAt(i) as Chip
                if (values.contains(chip.text.toString())) {
                    chip.isChecked = true
                    chip.setChipIconResource(R.drawable.round_check_20)
                    chip.isChipIconVisible = true
                    selectedList.add(chip.text.toString())
                } else {
                    chip.isChecked = false
                    chip.chipIcon = null
                }
            }
        }
    }

}