package dev.adriele.adolescare.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.applandeo.materialcalendarview.CalendarDay
import dev.adriele.adolescare.ui.LogPeriodYearlyActivity
import dev.adriele.adolescare.R
import dev.adriele.adolescare.database.AppDatabaseProvider
import dev.adriele.adolescare.database.entities.CycleLogEntity
import dev.adriele.adolescare.database.entities.MenstrualCycle
import dev.adriele.adolescare.database.entities.MenstrualHistoryEntity
import dev.adriele.adolescare.database.repositories.implementation.ChatBotRepositoryImpl
import dev.adriele.adolescare.database.repositories.implementation.CycleLogRepositoryImpl
import dev.adriele.adolescare.database.repositories.implementation.MenstrualHistoryRepositoryImpl
import dev.adriele.adolescare.databinding.ActivityLogPeriodBinding
import dev.adriele.adolescare.viewmodel.ChatBotViewModel
import dev.adriele.adolescare.viewmodel.CycleLogViewModel
import dev.adriele.adolescare.viewmodel.MenstrualHistoryViewModel
import dev.adriele.adolescare.viewmodel.factory.ChatBotViewModelFactory
import dev.adriele.adolescare.viewmodel.factory.CycleLogViewModelFactory
import dev.adriele.adolescare.viewmodel.factory.MenstrualHistoryViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class LogPeriodActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLogPeriodBinding

    private lateinit var menstrualHistoryViewModel: MenstrualHistoryViewModel
    private lateinit var cycleLogViewModel: CycleLogViewModel
    private lateinit var chatBotViewModel: ChatBotViewModel

    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityLogPeriodBinding.inflate(layoutInflater)

        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        userId = intent.getStringExtra("userId")

        initializeViewModel()
        init()
        handleButtons()
    }

    private fun initializeViewModel() {
        val menstrualHistoryDao = AppDatabaseProvider.getDatabase(this).menstrualHistoryDao()
        val menstrualHistoryRepo = MenstrualHistoryRepositoryImpl(menstrualHistoryDao)
        val menstrualHistoryViewModelFactory =
            MenstrualHistoryViewModelFactory(menstrualHistoryRepo)
        menstrualHistoryViewModel = ViewModelProvider(
            this,
            menstrualHistoryViewModelFactory
        )[MenstrualHistoryViewModel::class]

        val cycleLogDao = AppDatabaseProvider.getDatabase(this).cycleLogDao()
        val cycleDao = AppDatabaseProvider.getDatabase(this).cycleDao()
        val cycleRepository = CycleLogRepositoryImpl(cycleLogDao, cycleDao)
        val cycleViewModelFactory = CycleLogViewModelFactory(cycleRepository)
        cycleLogViewModel = ViewModelProvider(this, cycleViewModelFactory)[CycleLogViewModel::class]

        val chatBotDao = AppDatabaseProvider.getDatabase(this).conversationDao()
        val chatRepo = ChatBotRepositoryImpl(chatBotDao)
        val chatBotViewModelFactory = ChatBotViewModelFactory(chatRepo, userId!!)
        chatBotViewModel = ViewModelProvider(this, chatBotViewModelFactory)[ChatBotViewModel::class]
    }

    private fun init() {
        binding.shimmerLayout.startShimmer()

        // Observe menstrual history
        menstrualHistoryViewModel.getMensHistory(userId!!)

        menstrualHistoryViewModel.mensHistory.observe(this) { history ->
            if (history != null) {
                val lmp = history.lastPeriodStart
                val periodDays = history.periodDurationDays
                val cycleInterval = history.cycleIntervalWeeks

                lifecycleScope.launch(Dispatchers.IO) {
                    cycleLogViewModel.insertCycle(
                        MenstrualCycle(
                            userId = userId!!,
                            lastPeriodStart = lmp!!,
                            periodDurationDays = periodDays!!,
                            cycleLengthWeeks = cycleInterval!!
                        )
                    )

                    val sdf = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                    val calendar = Calendar.getInstance().apply {
                        time = sdf.parse(lmp)!!
                    }

                    val calendarDays = mutableListOf<CalendarDay>()

                    repeat(periodDays) {
                        val date = calendar.clone() as Calendar

                        val periodDay = CalendarDay(date).apply {
                            imageDrawable = ContextCompat.getDrawable(this@LogPeriodActivity, R.drawable.menstruation_combined)
                            labelColor = R.color.buttonColor
                        }
                        calendarDays.add(periodDay)
                        calendar.add(Calendar.DATE, 1)
                    }

                    val today = Calendar.getInstance()
                    val todayDay = CalendarDay(today).apply {
                        backgroundDrawable = ContextCompat.getDrawable(this@LogPeriodActivity, dev.adriele.calendarview.R.drawable.bg_today_circle)
                        labelColor = R.color.requiredColorHelper
                    }

                    // Prevent duplicates
                    if (calendarDays.none { it.calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                                it.calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) }) {
                        calendarDays.add(todayDay)
                    }

                    withContext(Dispatchers.Main) {
                        // Always check if view is attached
                        binding.calendarView.setDate(calendarDays.first().calendar)
                        binding.calendarView.setCalendarDays(calendarDays)

                        calculateLMP(history)
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun calculateLMP(history: MenstrualHistoryEntity) {
        val lastPeriodStart = history.lastPeriodStart
        val cycleIntervalWeeks = history.cycleIntervalWeeks ?: 4 // Default 28 days
        val cycleLength = cycleIntervalWeeks * 7

        val lastPeriod = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).parse(lastPeriodStart!!)
        val today = Calendar.getInstance()
        val calendarLMP = Calendar.getInstance().apply { time = lastPeriod!! }

        val diffInMillis = today.timeInMillis - calendarLMP.timeInMillis
        val totalDays = (diffInMillis / (1000 * 60 * 60 * 24)).toInt() + 1

        val currentCycle = (totalDays - 1) / cycleLength + 1
        val cycleDay = (totalDays - 1) % cycleLength + 1

        val dateFormatted = SimpleDateFormat("MMM d", Locale.getDefault()).format(today.time)
        binding.tvDateCycle.text = "$dateFormatted - Cycle $currentCycle Day $cycleDay"

        // Ovulation window calculation based on dynamic cycle length
        val ovulationDay = cycleLength - 14
        val fertileStartDay = ovulationDay - 5
        val fertileEndDay = ovulationDay + 1

        val remark = when (cycleDay) {
            in 1..7 -> "Low chance of getting pregnant"
            in 8 until fertileStartDay -> "Chance increasing"
            in fertileStartDay..fertileEndDay -> "High chance of getting pregnant"
            in (fertileEndDay + 1)..cycleLength -> "Fertility declining"
            else -> "Cycle day out of range"
        }

        binding.tvRemarks.text = remark

        cycleLogViewModel.getLogByDate(userId!!, dateFormatted).observe(this) { existingLog ->
            if (existingLog == null) {
                val cycleLog = CycleLogEntity(
                    userId = userId!!,
                    cycleDay = cycleDay,
                    date = dateFormatted,
                    symptoms = null,
                    sexActivity = null,
                    pregnancyTestResult = null,
                    notes = remark
                )
                saveCycleDays(cycleLog)
            }
            hideShimmer()
        }
    }

    private fun saveCycleDays(entity: CycleLogEntity) {
        cycleLogViewModel.insertCycleLog(entity)
    }

    private fun hideShimmer() {
        binding.shimmerLayout.stopShimmer()
        binding.shimmerLayout.visibility = View.GONE
        binding.llCycleDay.visibility = View.VISIBLE
    }

    private fun handleButtons() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnYear.setOnClickListener {
            startActivity(
                Intent(this, LogPeriodYearlyActivity::class.java)
                .putExtra("userId", userId))
            finish()
        }
    }
}