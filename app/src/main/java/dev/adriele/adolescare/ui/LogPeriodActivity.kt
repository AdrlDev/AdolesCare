package dev.adriele.adolescare.ui

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.applandeo.materialcalendarview.CalendarDay
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.gson.Gson
import dev.adriele.adolescare.R
import dev.adriele.adolescare.api.request.InsightsRequest
import dev.adriele.adolescare.api.response.InsightsResponse
import dev.adriele.adolescare.contracts.IChatBot
import dev.adriele.adolescare.database.AppDatabaseProvider
import dev.adriele.adolescare.database.entities.CycleLogEntity
import dev.adriele.adolescare.database.entities.MenstrualCycle
import dev.adriele.adolescare.database.entities.MenstrualHistoryEntity
import dev.adriele.adolescare.database.repositories.implementation.ChatBotRepositoryImpl
import dev.adriele.adolescare.database.repositories.implementation.CycleLogRepositoryImpl
import dev.adriele.adolescare.database.repositories.implementation.MenstrualHistoryRepositoryImpl
import dev.adriele.adolescare.databinding.ActivityLogPeriodBinding
import dev.adriele.adolescare.helpers.Utility
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

class LogPeriodActivity : AppCompatActivity(), IChatBot.Insight {
    private lateinit var binding: ActivityLogPeriodBinding

    private lateinit var menstrualHistoryViewModel: MenstrualHistoryViewModel
    private lateinit var cycleLogViewModel: CycleLogViewModel
    private lateinit var chatBotViewModel: ChatBotViewModel

    private var sexDrives: MutableList<String> = mutableListOf()
    private var moods: MutableList<String> = mutableListOf()

    private var userId: String? = null
    private var dateFormatted: String? = null
    private var cycleDay: Int? = null

    private var isExpanded = false

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

    private fun populateChipGroup(chipGroup: ChipGroup, items: List<String>) {
        chipGroup.removeAllViews() // Clear existing chips

        Log.e("CHIP_ITEMS", Gson().toJson(items))

        for (item in items) {
            val chip = Chip(this).apply {
                text = item
                isChecked = true
                isCheckable = false
                isChipIconVisible = true
                setChipIconResource(R.drawable.round_check_20)

                setChipBackgroundColorResource(R.color.buttonColor) // optional
                setTextColor(resources.getColor(R.color.buttonTextColor, null)) // optional

                // ✅ Radius & Stroke
                chipCornerRadius = resources.getDimension(R.dimen.margin_medium)
                chipStrokeColor = getColorStateList(R.color.buttonColor)
            }
            chipGroup.addView(chip)
        }
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
        cycleDay = (totalDays - 1) % cycleLength + 1

        dateFormatted = SimpleDateFormat("MMM d", Locale.getDefault()).format(today.time)
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

        cycleLogViewModel.getLogByDate(userId!!, dateFormatted ?: Utility.getCurrentCycleDate()).observe(this) { existingLog ->
            sexDrives.clear()
            moods.clear()

            if (existingLog == null) {
                val cycleLog = CycleLogEntity(
                    userId = userId!!,
                    cycleDay = cycleDay ?: 0,
                    date = dateFormatted ?: Utility.getCurrentCycleDate(),
                    symptoms = null,
                    sexActivity = null,
                    pregnancyTestResult = null,
                    notes = remark
                )
                saveCycleDays(cycleLog)
            } else {
                val hasSexDrive = !existingLog.sexActivity.isNullOrEmpty()
                val hasMood = !existingLog.mood.isNullOrEmpty()

                sexDrives.addAll(existingLog.sexActivity ?: emptyList())
                moods.addAll(existingLog.mood ?: emptyList())

                if (hasSexDrive) {
                    populateChipGroup(binding.cgSexDrive, existingLog.sexActivity)
                }

                if (hasMood) {
                    populateChipGroup(binding.cgMood, existingLog.mood)
                }

                val showLL = hasSexDrive || hasMood

                binding.lblSexDrive.visibility = if(hasSexDrive) View.VISIBLE else View.GONE
                binding.lblMood.visibility = if(hasMood) View.VISIBLE else View.GONE

                // 👇 Show or hide the layout depending on data
                binding.llActivities.visibility =
                    if (showLL) View.VISIBLE else View.GONE

                getInsights(sexDrives, moods, showLL)
            }
            hideShimmer()
        }
    }

    private fun getInsights(
        sexDrives: MutableList<String>,
        moods: MutableList<String>,
        isNeedInsight: Boolean
    ) {
        if(isNeedInsight) {
            binding.tvLblInsight.visibility = View.VISIBLE
            binding.cardInsight.visibility = View.GONE
            binding.shimmerInsight.visibility = View.VISIBLE
            binding.shimmerInsight.startShimmer()
            chatBotViewModel.getInsights(
                insightsRequest = InsightsRequest(
                    sexDrives = sexDrives,
                    moods = moods
                ),
                this@LogPeriodActivity
            )
        } else {
            binding.shimmerInsight.visibility = View.GONE
            binding.cardInsight.visibility = View.GONE
            binding.tvLblInsight.visibility = View.GONE
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

        binding.btnAdd.setOnClickListener {
            startActivity(
                Intent(this, AddSymptomsActivity::class.java)
                    .putExtra("userId", userId)
                    .putExtra("cycle", binding.tvDateCycle.text)
                    .putExtra("cycleDay", cycleDay)
                    .putExtra("dateCycle", dateFormatted))
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupInsightToggle() {
        binding.tvInsight.post {
            // Temporarily allow all lines to measure true line count
            binding.tvInsight.maxLines = Integer.MAX_VALUE
            binding.tvInsight.ellipsize = null

            binding.tvInsight.measure(
                View.MeasureSpec.makeMeasureSpec(binding.tvInsight.width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.UNSPECIFIED
            )

            val fullLineCount = binding.tvInsight.lineCount

            // Restore collapsed state
            binding.tvInsight.maxLines = 10
            binding.tvInsight.ellipsize = TextUtils.TruncateAt.END

            if (fullLineCount <= 10) {
                binding.tvReadMore.visibility = View.GONE
            } else {
                binding.tvReadMore.visibility = View.VISIBLE
            }
        }

        binding.tvReadMore.setOnClickListener {
            val startHeight = binding.tvInsight.height

            if (!isExpanded) {
                // Expand
                binding.tvInsight.maxLines = Int.MAX_VALUE
                binding.tvInsight.ellipsize = null

                binding.tvInsight.measure(
                    View.MeasureSpec.makeMeasureSpec(binding.tvInsight.width, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.UNSPECIFIED
                )
                val endHeight = binding.tvInsight.measuredHeight

                animateTextViewHeight(binding.tvInsight, startHeight, endHeight)

                binding.tvReadMore.text = "Show less..."
            } else {
                // Collapse
                binding.tvInsight.maxLines = 10
                binding.tvInsight.ellipsize = TextUtils.TruncateAt.END

                binding.tvInsight.measure(
                    View.MeasureSpec.makeMeasureSpec(binding.tvInsight.width, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.UNSPECIFIED
                )
                val endHeight = binding.tvInsight.measuredHeight

                animateTextViewHeight(binding.tvInsight, startHeight, endHeight)

                binding.tvReadMore.text = "Read more..."
            }

            isExpanded = !isExpanded
        }
    }

    private fun animateTextViewHeight(view: TextView, from: Int, to: Int) {
        val animator = ValueAnimator.ofInt(from, to)
        animator.duration = 300
        animator.addUpdateListener { animation ->
            val value = animation.animatedValue as Int
            view.layoutParams.height = value
            view.requestLayout()
        }
        animator.start()
    }

    override fun onResult(result: InsightsResponse) {
        Log.e("INSIGHT_RESULT", Gson().toJson(result))
        binding.tvInsight.text = result.insights

        setupInsightToggle()

        binding.shimmerInsight.stopShimmer()
        binding.cardInsight.visibility = View.VISIBLE
        binding.shimmerInsight.visibility = View.GONE
    }
}