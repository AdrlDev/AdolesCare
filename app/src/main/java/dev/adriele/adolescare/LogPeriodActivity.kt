package dev.adriele.adolescare

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.applandeo.materialcalendarview.EventDay
import dev.adriele.adolescare.database.AppDatabaseProvider
import dev.adriele.adolescare.database.entities.CycleLogEntity
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
        val menstrualHistoryViewModelFactory = MenstrualHistoryViewModelFactory(menstrualHistoryRepo)
        menstrualHistoryViewModel = ViewModelProvider(this, menstrualHistoryViewModelFactory)[MenstrualHistoryViewModel::class]

        val cycleLogDao = AppDatabaseProvider.getDatabase(this).cycleLogDao()
        val cycleRepository = CycleLogRepositoryImpl(cycleLogDao)
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

                val sdf = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                val calendar = Calendar.getInstance()
                calendar.time = sdf.parse(lmp!!)!!

                val periodEvents = mutableListOf<EventDay>()
                val highlightDates = mutableListOf<Calendar>()

                repeat(periodDays!!) {
                    val date = calendar.clone() as Calendar
                    highlightDates.add(date)
                    // 🩸 Use combined icon drawable
                    periodEvents.add(EventDay(date, R.drawable.menstruation_combined))

                    calendar.add(Calendar.DATE, 1)
                }

                // Scroll to LMP
                binding.calendarView.setDate(periodEvents.first().calendar)

                // Set the events
                binding.calendarView.setEvents(periodEvents)
                binding.calendarView.setHighlightedDays(highlightDates)

                calculateLMP(history)
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
        cycleLogViewModel.insertCycle(entity)
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
            startActivity(Intent(this, LogPeriodYearlyActivity::class.java)
                .putExtra("userId", userId))
            finish()
        }
    }
}