package dev.adriele.adolescare.ui

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.applandeo.materialcalendarview.CalendarDay
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.platform.MaterialSharedAxis
import com.google.gson.Gson
import dev.adriele.adolescare.BaseActivity
import dev.adriele.adolescare.R
import dev.adriele.adolescare.adapter.SymptomsActivitiesAdapter
import dev.adriele.adolescare.api.request.InsightsRequest
import dev.adriele.adolescare.api.response.InsightsResponse
import dev.adriele.adolescare.api.websocket.WebSocketClient
import dev.adriele.adolescare.api.websocket.contracts.IWebSocket
import dev.adriele.adolescare.contracts.IChatBot
import dev.adriele.adolescare.contracts.ILogPeriod
import dev.adriele.adolescare.database.AppDatabaseProvider
import dev.adriele.adolescare.database.entities.CycleLogEntity
import dev.adriele.adolescare.database.entities.MenstrualCycle
import dev.adriele.adolescare.database.entities.MenstrualHistoryEntity
import dev.adriele.adolescare.database.entities.Reminder
import dev.adriele.adolescare.database.repositories.implementation.ChatBotRepositoryImpl
import dev.adriele.adolescare.database.repositories.implementation.CycleLogRepositoryImpl
import dev.adriele.adolescare.database.repositories.implementation.MenstrualHistoryRepositoryImpl
import dev.adriele.adolescare.database.repositories.implementation.ReminderRepositoryImpl
import dev.adriele.adolescare.databinding.ActivityLogPeriodBinding
import dev.adriele.adolescare.fragments.LogNewPeriodFragment
import dev.adriele.adolescare.helpers.NotificationUtils
import dev.adriele.adolescare.helpers.Utility
import dev.adriele.adolescare.helpers.Utility.animateTextViewHeight
import dev.adriele.adolescare.helpers.enums.SymptomCategory
import dev.adriele.adolescare.helpers.enums.SymptomOption
import dev.adriele.adolescare.model.SymptomsActivitiesQ
import dev.adriele.adolescare.viewmodel.ChatBotViewModel
import dev.adriele.adolescare.viewmodel.CycleLogViewModel
import dev.adriele.adolescare.viewmodel.MenstrualHistoryViewModel
import dev.adriele.adolescare.viewmodel.ReminderViewModel
import dev.adriele.adolescare.viewmodel.factory.ChatBotViewModelFactory
import dev.adriele.adolescare.viewmodel.factory.CycleLogViewModelFactory
import dev.adriele.adolescare.viewmodel.factory.MenstrualHistoryViewModelFactory
import dev.adriele.adolescare.viewmodel.factory.ReminderViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class LogPeriodActivity : BaseActivity(), IChatBot.Insight, Utility.DatePickedCallback, IWebSocket {
    private lateinit var binding: ActivityLogPeriodBinding

    private lateinit var menstrualHistoryViewModel: MenstrualHistoryViewModel
    private lateinit var cycleLogViewModel: CycleLogViewModel
    private lateinit var chatBotViewModel: ChatBotViewModel
    private lateinit var reminderViewModel: ReminderViewModel

    private var sexDrives: MutableList<String> = mutableListOf()
    private var moods: MutableList<String> = mutableListOf()
    private var symptoms: MutableList<String> = mutableListOf()
    private var vaginalDischarge: MutableList<String> = mutableListOf()
    private var digestionStool: MutableList<String> = mutableListOf()
    private var pregnancyTest: MutableList<String> = mutableListOf()
    private var physicalActivity: MutableList<String> = mutableListOf()

    private var userId: String? = null
    private var dateFormatted: String? = null
    private var selectedDate: Calendar? = null
    private var cycleDay: Int? = null

    private var isExpanded = false
    private var isShowingAll = false

    private var symptomsActivitiesQ: MutableList<SymptomsActivitiesQ> = mutableListOf()
    private var webSocketClient: WebSocketClient? = null
    private var insightRequest: InsightsRequest? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedAxis = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        window.enterTransition = sharedAxis
        window.exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityLogPeriodBinding.inflate(layoutInflater)

        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        userId = intent.getStringExtra("userId") ?: ""

        webSocketClient = WebSocketClient("insight", this)

        initializeViewModel()
        init()
        handleButtons()
        editLogPeriod()
        observer()
    }

    private fun observer() {
        menstrualHistoryViewModel.updateStatus.observe(this) { (isUpdated, message) ->
            if (isUpdated) {
                showMenstrualHistoryShimmer()
                menstrualHistoryViewModel.getMensHistory(userId ?: "")
                Snackbar.make(binding.main, message, Snackbar.LENGTH_SHORT).show()
            }
        }

        menstrualHistoryViewModel.insertStatus.observe(this) { (success, _) ->
            if (success) {
                showMenstrualHistoryShimmer()
                menstrualHistoryViewModel.getMensHistory(userId ?: "")
            } else {
                Snackbar.make(binding.main, "Failed to save menstrual history...", Snackbar.LENGTH_LONG).show()
            }
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
        val chatBotViewModelFactory = ChatBotViewModelFactory(chatRepo, userId ?: "")
        chatBotViewModel = ViewModelProvider(this, chatBotViewModelFactory)[ChatBotViewModel::class]

        val reminderDao = AppDatabaseProvider.getDatabase(this).reminderDao()
        val reminderRepo = ReminderRepositoryImpl(reminderDao)
        val reminderFactory = ReminderViewModelFactory(reminderRepo)
        reminderViewModel = ViewModelProvider(this, reminderFactory)[ReminderViewModel::class]
    }

    private fun init() {
        webSocketClient?.connect()

        cycleLogViewModel.getAllCycles(userId ?: "").observe(this@LogPeriodActivity) { cycles ->
            val calendarDays = mutableListOf<CalendarDay>() // collect all cycles here

            for (cycle in cycles) {
                val lmp = cycle.lastPeriodStart
                val periodDays = cycle.periodDurationDays

                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                val calendar = Calendar.getInstance().apply {
                    time = sdf.parse(lmp)!!
                }

                repeat(periodDays) {
                    val date = calendar.clone() as Calendar

                    val periodDay = CalendarDay(date).apply {
                        imageDrawable = ContextCompat.getDrawable(
                            this@LogPeriodActivity,
                            R.drawable.menstruation_combined
                        )
                        labelColor = R.color.buttonColor
                    }
                    calendarDays.add(periodDay)
                    calendar.add(Calendar.DATE, 1)
                }
            }

            // Highlight today
            val today = Calendar.getInstance()
            val todayDay = CalendarDay(today).apply {
                backgroundDrawable = ContextCompat.getDrawable(
                    this@LogPeriodActivity,
                    dev.adriele.calendarview.R.drawable.bg_today_circle
                )
                labelColor = R.color.requiredColorHelper
            }

            if (calendarDays.none {
                    it.calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                            it.calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
                }) {
                calendarDays.add(todayDay)
            }

            if (calendarDays.isEmpty()) {
                calendarDays.add(CalendarDay(today))
            }

            binding.shimmerLlCalendar.stopShimmer()
            binding.shimmerLlCalendar.visibility = View.GONE
            binding.calendarCard.visibility = View.VISIBLE

            val targetDate = selectedDate ?: calendarDays.last().calendar
            binding.calendarView.setDate(targetDate)
            binding.calendarView.postDelayed({
                binding.calendarView.setDate(targetDate)
            }, 1000)
            binding.calendarView.post {
                binding.calendarView.setCalendarDays(calendarDays)
            }
        }

        menstrualHistoryViewModel.mensHistory.observe(this) { history ->
            if (history != null) {
                val lmp = history.lastPeriodStart ?: Utility.getTwoWeeksAgo()
                val periodDays = history.periodDurationDays ?: 3
                val cycleInterval = history.cycleIntervalWeeks ?: 3

                lifecycleScope.launch(Dispatchers.IO) {
                    val cycleThisMonth = cycleLogViewModel.getCycleByMonth(
                        userId = userId ?: "",
                        lmp = Utility.formatDate(lmp)
                    )

                    Log.e("CYCLES", Gson().toJson(cycleThisMonth))

                    if (cycleThisMonth == null) {
                        cycleLogViewModel.insertCycle(
                            cycle = MenstrualCycle(
                                userId = userId ?: "",
                                lastPeriodStart = Utility.formatDate(lmp),
                                periodDurationDays = periodDays,
                                cycleLengthWeeks = cycleInterval,
                                createdAt = Utility.getCurrentDate()
                            )
                        )
                    } else {
                        // Found a cycle in the same month → update instead of insert
                        val updated = cycleThisMonth.copy(
                            lastPeriodStart = Utility.formatDate(lmp),
                            periodDurationDays = periodDays,
                            cycleLengthWeeks = cycleInterval
                        )
                        cycleLogViewModel.updateCycle(updated)
                    }

                    withContext(Dispatchers.Main) {
                        calculateLMP(history)
                    }
                }
            }
        }

        startGettingInsight()
    }

    @SuppressLint("SetTextI18n")
    private fun calculateLMP(history: MenstrualHistoryEntity) {
        val lastPeriodStart = history.lastPeriodStart ?: Utility.getTwoWeeksAgo()
        val cycleIntervalWeeks = history.cycleIntervalWeeks ?: 4 // Default 28 days
        val cycleLength = cycleIntervalWeeks * 7

        val lastPeriod = SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH).parse(
            lastPeriodStart
        )
        val today = Calendar.getInstance()
        val calendarLMP = Calendar.getInstance().apply {
            if (lastPeriod != null) {
                time = lastPeriod
            }
        }

        val diffInMillis = today.timeInMillis - calendarLMP.timeInMillis
        val totalDays = (diffInMillis / (1000 * 60 * 60 * 24)).toInt() + 1

        val currentCycle = (totalDays - 1) / cycleLength + 1
        cycleDay = (totalDays - 1) % cycleLength + 1

        dateFormatted = SimpleDateFormat("MMM d", Locale.ENGLISH).format(today.time)
        binding.tvDateCycle.text = "$dateFormatted - Cycle $currentCycle Day $cycleDay"

        // Ovulation window calculation based on dynamic cycle length
        val ovulationDay = cycleLength - 14
        val fertileStartDay = ovulationDay - 5
        val fertileEndDay = ovulationDay + 1

        val remark = when (cycleDay) {
            in 1..7 -> getString(dev.adriele.language.R.string.low_chance_pregnancy)
            in 8 until fertileStartDay -> getString(dev.adriele.language.R.string.chance_increasing)
            in fertileStartDay..fertileEndDay -> getString(dev.adriele.language.R.string.high_chance_pregnancy)
            in (fertileEndDay + 1)..cycleLength -> getString(dev.adriele.language.R.string.fertility_declining)
            else -> getString(dev.adriele.language.R.string.cycle_day_out_of_range)
        }

        binding.tvRemarks.text = remark
    }

    private fun getInsights(
        sexDrives: MutableList<String>,
        moods: MutableList<String>,
        symptoms: MutableList<String>,
        vaginalDischarge: MutableList<String>,
        digestionAndStool: MutableList<String>,
        pregnancyTest: MutableList<String>,
        physicalActivity: MutableList<String>,
        isNeedInsight: Boolean
    ) {
        if (isNeedInsight) {
            binding.tvLblInsight.visibility = View.VISIBLE
            binding.cardInsight.visibility = View.GONE
            binding.shimmerInsight.visibility = View.VISIBLE
            binding.shimmerInsight.startShimmer()

            val sexDrive = backToOrigLabel(sexDrives)
            val mood = backToOrigLabel(moods)
            val symptom = backToOrigLabel(symptoms)
            val vaginalDischarges = backToOrigLabel(vaginalDischarge)
            val digestionAndStools = backToOrigLabel(digestionAndStool)
            val pregnancyTests = backToOrigLabel(pregnancyTest)
            val physicalActivities = backToOrigLabel(physicalActivity)

            insightRequest = InsightsRequest(
                sexDrives = sexDrive,
                moods = mood,
                symptoms = symptom,
                vaginalDischarge = vaginalDischarges,
                digestionAndStool = digestionAndStools,
                pregnancyTest = pregnancyTests,
                physicalActivity = physicalActivities
            )

            lifecycleScope.launch {
                delay(100)
                webSocketClient?.sendMessage(
                    Gson().toJson(
                        insightRequest
                    )
                )
            }
        } else {
            binding.shimmerInsight.stopShimmer()
            binding.shimmerInsight.visibility = View.GONE
            binding.cardInsight.visibility = View.GONE
            binding.tvLblInsight.visibility = View.GONE
        }
    }

    private fun backToOrigLabel(list: MutableList<String>): MutableList<String> {
        return list.mapNotNull { optionName ->
            try {
                val option = SymptomOption.valueOf(optionName)
                getString(option.resId)
            } catch (_: Exception) {
                null
            }
        }.toMutableList()
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
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnYear.setOnClickListener {
            val intent = Intent(this, LogPeriodYearlyActivity::class.java).apply {
                putExtra("userId", userId)
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
        }

        binding.btnAdd.setOnClickListener {
            val intent = Intent(this, AddSymptomsActivity::class.java).apply {
                putExtra("userId", userId)
                putExtra("cycle", binding.tvDateCycle.text)
                putExtra("cycleDay", cycleDay)
                putExtra("dateCycle", dateFormatted)
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
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
                    View.MeasureSpec.makeMeasureSpec(
                        binding.tvInsight.width,
                        View.MeasureSpec.EXACTLY
                    ),
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
                    View.MeasureSpec.makeMeasureSpec(
                        binding.tvInsight.width,
                        View.MeasureSpec.EXACTLY
                    ),
                    View.MeasureSpec.UNSPECIFIED
                )
                val endHeight = binding.tvInsight.measuredHeight

                animateTextViewHeight(binding.tvInsight, startHeight, endHeight)

                binding.tvReadMore.text = "Read more..."
            }

            isExpanded = !isExpanded
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onResult(result: InsightsResponse) {
        Log.e("INSIGHT_RESULT", Gson().toJson(result))
        val possibleConditions = result.insights.summary.possibleConditions
        val recommendations = result.insights.summary.recommendations
        val warnings = result.insights.summary.warnings
        val notes = result.insights.summary.notes
        val builder = SpannableStringBuilder()

        fun appendSection(title: String, items: List<String>) {
            if (items.isNotEmpty()) {
                val start = builder.length
                builder.append("$title:\n")
                builder.setSpan(
                    StyleSpan(Typeface.BOLD),
                    start,
                    builder.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                items.forEach { builder.append("• $it\n") }
                builder.append("\n")
            }
        }

        fun appendSection(title: String, text: String) {
            if (text.isNotBlank()) {
                val start = builder.length
                builder.append("$title:\n")
                builder.setSpan(
                    StyleSpan(Typeface.BOLD),
                    start,
                    builder.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                builder.append(text)
            }
        }

        appendSection("Possible Conditions", possibleConditions)
        appendSection("Recommendations", recommendations)
        appendSection("Warnings", warnings)
        appendSection("Notes", notes)

        binding.tvInsight.text = builder

        setupInsightToggle()

        binding.shimmerInsight.stopShimmer()
        binding.cardInsight.visibility = View.VISIBLE
        binding.shimmerInsight.visibility = View.GONE
    }

    override fun onError(message: String) {
        binding.shimmerInsight.stopShimmer()
        binding.shimmerInsight.visibility = View.GONE
        binding.cardInsight.visibility = View.GONE
        binding.tvLblInsight.visibility = View.GONE
        Snackbar.make(binding.main, "Failed to load insights: $message", Snackbar.LENGTH_SHORT)
            .show()
    }

    override fun onResume() {
        super.onResume()

        startGettingInsight()
    }

    private fun startGettingInsight() {
        if (isFinishing || isDestroyed) return

        val selectedDateMillis = intent.getLongExtra("selectedDate", -1L)
        selectedDate = if (selectedDateMillis != -1L) {
            Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
        } else {
            null
        }

        showMenstrualHistoryShimmer()
        // Observe menstrual history
        menstrualHistoryViewModel.getMensHistory(userId ?: "")
        symptomsActivitiesQ.clear()

        val listOfCategories = SymptomCategory.entries

        lifecycleScope.launch {
            sexDrives.clear()
            moods.clear()
            symptoms.clear()
            vaginalDischarge.clear()
            digestionStool.clear()
            pregnancyTest.clear()
            physicalActivity.clear()

            val existingLog = cycleLogViewModel.getLogByDate(
                userId = userId ?: "",
                date = dateFormatted ?: Utility.getCurrentCycleDate()
            )

            if (existingLog == null) {
                val cycleLog = CycleLogEntity(
                    userId = userId ?: "",
                    cycleDay = cycleDay ?: 0,
                    date = dateFormatted ?: Utility.getCurrentCycleDate(),
                    notes = binding.tvRemarks.text.toString()
                )
                saveCycleDays(cycleLog)
            } else {
                val selectedMap = mutableMapOf<String, List<String>>()

                listOfCategories.forEach { categoryEnum ->
                    val (hasData, options) = when (categoryEnum) {
                        SymptomCategory.SEX_DRIVE -> existingLog.sexActivity?.let { true to it }
                            ?: (false to emptyList())

                        SymptomCategory.MOOD -> existingLog.mood?.let { true to it }
                            ?: (false to emptyList())

                        SymptomCategory.SYMPTOMS -> existingLog.symptoms?.let { true to it }
                            ?: (false to emptyList())

                        SymptomCategory.VAGINAL_DISCHARGE -> existingLog.vaginalDischarge?.let { true to it }
                            ?: (false to emptyList())

                        SymptomCategory.DIGESTION_AND_STOOL -> existingLog.digestionAndStool?.let { true to it }
                            ?: (false to emptyList())

                        SymptomCategory.PREGNANCY_TEST -> existingLog.pregnancyTestResult?.let { true to it }
                            ?: (false to emptyList())

                        SymptomCategory.PHYSICAL_ACTIVITY -> existingLog.physicalActivity?.let { true to it }
                            ?: (false to emptyList())
                    }

                    if (hasData) {
                        selectedMap[categoryEnum.name] = options
                    }

                    Log.e("SELECTED_OPTIONS", Gson().toJson(options))

                    handleCategory(hasData, categoryEnum, options)

                    val labelToEnumListMap = mapOf(
                        SymptomCategory.SEX_DRIVE to sexDrives,
                        SymptomCategory.MOOD to moods,
                        SymptomCategory.SYMPTOMS to symptoms,
                        SymptomCategory.VAGINAL_DISCHARGE to vaginalDischarge,
                        SymptomCategory.DIGESTION_AND_STOOL to digestionStool,
                        SymptomCategory.PREGNANCY_TEST to pregnancyTest,
                        SymptomCategory.PHYSICAL_ACTIVITY to physicalActivity
                    )

                    val isEnumNames = options.all { opt ->
                        categoryEnum.options.any { it.name == opt }
                    }

                    val valuesToAdd = if (isEnumNames) {
                        options
                    } else {
                        mapLabelsToEnumNames(
                            options,
                            categoryEnum.options
                        ) { getString(it.resId) }
                    }

                    Log.d("VALUES_TO_ADD", "Category: $categoryEnum, Converted: $valuesToAdd")

                    labelToEnumListMap[categoryEnum]?.addAll(valuesToAdd)
                }

                Log.d(
                    "SYMPTOMS_RECYCLER",
                    "Calling setupSymptomsRecyclerView with ${symptomsActivitiesQ.size} items"
                )
                setupSymptomsRecyclerView(selectedMap)

                val showLL =
                    sexDrives.isNotEmpty() || moods.isNotEmpty() || symptoms.isNotEmpty() ||
                            vaginalDischarge.isNotEmpty() || digestionStool.isNotEmpty() ||
                            pregnancyTest.isNotEmpty() || physicalActivity.isNotEmpty()

                Log.d("IS_SHOW", showLL.toString())

                binding.rvSelectedSymptoms.visibility = if (showLL) View.VISIBLE else View.GONE

                getInsights(
                    sexDrives = sexDrives,
                    moods = moods,
                    symptoms = symptoms,
                    vaginalDischarge = vaginalDischarge,
                    digestionAndStool = digestionStool,
                    pregnancyTest = pregnancyTest,
                    physicalActivity = physicalActivity,
                    isNeedInsight = showLL
                )
            }
            hideShimmer()
        }
    }

    private fun handleCategory(
        hasData: Boolean,
        category: SymptomCategory,
        choices: List<String>
    ) {
        if (hasData && choices.isNotEmpty()) {
            val data = SymptomsActivitiesQ(category.name, choices)

            Log.e("CATEGORY_SELECTED", Gson().toJson(data))

            symptomsActivitiesQ.add(data)
        }
    }

    private fun mapLabelsToEnumNames(
        labels: List<String>,
        options: List<SymptomOption>,
        labelResolver: (SymptomOption) -> String
    ): List<String> {
        return labels.mapNotNull { label ->
            options.find { label.equals(labelResolver(it), ignoreCase = true) }?.name
        }
    }

    private fun setupSymptomsRecyclerView(preselected: Map<String, List<String>>) {

        lifecycleScope.launch {
            val today = Utility.getCurrentDate()

            preselected.forEach { (categoryKey, selectedOptions) ->
                val category = try {
                    SymptomCategory.valueOf(categoryKey)
                } catch (_: Exception) {
                    return@forEach
                }

                val categoryLabel = getString(category.labelRes)
                val title = "$categoryLabel log activity" // e.g. MOOD log activity

                // Check if reminder for this category already exists today
                val existing =
                    reminderViewModel.getReminderByTitleAndDate(userId ?: "", title, today)
                if (existing != null) return@forEach

                // Convert option names to localized labels
                val optionLabels = backToOrigLabel(selectedOptions.toMutableList())

                if (optionLabels.isEmpty()) return@forEach

                val message = "You logged $categoryLabel: ${optionLabels.joinToString(", ")}"

                val reminder = Reminder(
                    userId = userId ?: "",
                    title = title,
                    message = message,
                    dateTime = today,
                    type = "Reminders"
                )

                lifecycleScope.launch {
                    val isReminderExist = reminderViewModel.getReminderByMessageAndDate(
                        userId = userId ?: "",
                        message = message,
                        date = today
                    )

                    if (isReminderExist == null) {
                        reminderViewModel.insertReminder(reminder)

                        NotificationUtils.showNotification(
                            context = applicationContext,
                            title = title,
                            message = message,
                            userId = userId ?: ""
                        )
                    }
                }
            }
        }

        // Decide which items to show
        val visibleItems = if (isShowingAll) {
            symptomsActivitiesQ
        } else {
            symptomsActivitiesQ.take(2)
        }

        val adapter = SymptomsActivitiesAdapter(
            visibleItems,
            preselected,
            true
        ) { category, selected ->

        }

        binding.rvSelectedSymptoms.adapter = adapter
        binding.rvSelectedSymptoms.layoutManager = LinearLayoutManager(this)

        // Animate RecyclerView height
        binding.rvSelectedSymptoms.post {
            val startHeight = binding.rvSelectedSymptoms.height

            binding.rvSelectedSymptoms.measure(
                View.MeasureSpec.makeMeasureSpec(
                    binding.rvSelectedSymptoms.width,
                    View.MeasureSpec.EXACTLY
                ),
                View.MeasureSpec.UNSPECIFIED
            )
            val targetHeight = binding.rvSelectedSymptoms.measuredHeight

            val animator = ValueAnimator.ofInt(startHeight, targetHeight).apply {
                duration = 300
                addUpdateListener {
                    val value = it.animatedValue as Int
                    binding.rvSelectedSymptoms.layoutParams.height = value
                    binding.rvSelectedSymptoms.requestLayout()
                }
            }
            animator.start()
        }

        // Show/hide or toggle the button text
        if (symptomsActivitiesQ.size > 2) {
            binding.tvLoadMore.visibility = View.VISIBLE
            binding.tvLoadMore.text = if (isShowingAll) "Show less..." else "Show more..."

            binding.tvLoadMore.setOnClickListener {
                isShowingAll = !isShowingAll
                setupSymptomsRecyclerView(preselected)
            }
        } else {
            binding.tvLoadMore.visibility = View.GONE
        }
    }

    private fun editLogPeriod() {
        binding.btnEditPeriodDate.setOnClickListener {
            val bottomSheet = LogNewPeriodFragment.newInstance(userId, object : ILogPeriod {
                override fun onSaveNewLog(menstrualHistory: MenstrualHistoryEntity) {
                    lifecycleScope.launch {
                        val existing = menstrualHistoryViewModel.getMensHistoryNow(userId ?: "")
                        if (existing != null) {
                            val updated = existing.copy(
                                lastPeriodStart = menstrualHistory.lastPeriodStart,
                                periodDurationDays = menstrualHistory.periodDurationDays,
                                cycleIntervalWeeks = menstrualHistory.cycleIntervalWeeks
                            )

                            menstrualHistoryViewModel.updateMenstrualHistory(updated)

                            val reminder = Reminder(
                                userId = userId ?: "",
                                title = "Menstrual History",
                                message = "Your last menstrual period updated to ${menstrualHistory.lastPeriodStart}.",
                                dateTime = Utility.getCurrentDate(),
                                type = "Reminders"
                            )

                            reminderViewModel.insertReminder(reminder)

                            NotificationUtils.showNotification(
                                context = this@LogPeriodActivity,
                                title = "Menstrual History",
                                message = "Your last menstrual period updated to ${menstrualHistory.lastPeriodStart}.",
                                userId = userId ?: ""
                            )
                        } else {
                            // If no existing record, you may want to insert instead
                            menstrualHistoryViewModel.insertMenstrualHistory(menstrualHistory)
                        }
                    }
                }
            })
            bottomSheet.show(supportFragmentManager, "LogNewPeriodFragment")
        }
    }

    override fun onDatePicked(formattedDate: String) {
        lifecycleScope.launch {
            val existing = menstrualHistoryViewModel.getMensHistoryNow(userId ?: "")
            if (existing != null) {
                val updated = existing.copy(
                    lastPeriodStart = formattedDate
                )

                menstrualHistoryViewModel.updateMenstrualHistory(updated)

                val reminder = Reminder(
                    userId = userId ?: "",
                    title = "Menstrual History",
                    message = "Your last menstrual period updated to $formattedDate.",
                    dateTime = Utility.getCurrentDate(),
                    type = "Reminders"
                )

                reminderViewModel.insertReminder(reminder)

                NotificationUtils.showNotification(
                    context = this@LogPeriodActivity,
                    title = "Menstrual History",
                    message = "Your last menstrual period updated to $formattedDate.",
                    userId = userId ?: ""
                )
            } else {
                // If no existing record, you may want to insert instead
                val newEntry = MenstrualHistoryEntity(
                    userId = userId ?: "",
                    firstPeriodReported = false,
                    lastPeriodStart = formattedDate,
                    periodDurationDays = 4,
                    cycleIntervalWeeks = 3
                )

                menstrualHistoryViewModel.insertMenstrualHistory(newEntry)
            }
        }
    }

    private fun showMenstrualHistoryShimmer() {
        binding.shimmerLayout.startShimmer()
        binding.shimmerLlCalendar.startShimmer()
        binding.shimmerLlCalendar.visibility = View.VISIBLE
        binding.calendarCard.visibility = View.GONE
    }

    override fun onWebSocketResult(message: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                val gson = Gson()
                val result = gson.fromJson(message, InsightsResponse::class.java)

                val possibleConditions = result.insights.summary.possibleConditions
                val recommendations = result.insights.summary.recommendations
                val warnings = result.insights.summary.warnings
                val notes = result.insights.summary.notes
                val builder = SpannableStringBuilder()

                fun appendSection(title: String, items: List<String>) {
                    if (items.isNotEmpty()) {
                        val start = builder.length
                        builder.append("$title:\n")
                        builder.setSpan(
                            StyleSpan(Typeface.BOLD),
                            start,
                            builder.length,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        items.forEach { builder.append("• $it\n") }
                        builder.append("\n")
                    }
                }

                fun appendSection(title: String, text: String) {
                    if (text.isNotBlank()) {
                        val start = builder.length
                        builder.append("$title:\n")
                        builder.setSpan(
                            StyleSpan(Typeface.BOLD),
                            start,
                            builder.length,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        builder.append(text)
                    }
                }

                appendSection("Possible Conditions", possibleConditions)
                appendSection("Recommendations", recommendations)
                appendSection("Warnings", warnings)
                appendSection("Notes", notes)

                binding.tvInsight.text = builder
            } catch (_: Exception) {
                // fallback if message is just a plain string
                binding.tvInsight.text = message
            }

            setupInsightToggle()

            binding.shimmerInsight.stopShimmer()
            binding.cardInsight.visibility = View.VISIBLE
            binding.shimmerInsight.visibility = View.GONE
        }
    }

    override fun onWebSocketError(error: String) {
        Log.e("LOG_PERIOD", error)

        webSocketClient?.ping()
    }

    override fun onDestroy() {
        super.onDestroy()
        webSocketClient?.close()
    }
}