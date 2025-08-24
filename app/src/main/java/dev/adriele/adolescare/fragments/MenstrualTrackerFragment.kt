package dev.adriele.adolescare.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.text.style.StyleSpan
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialFadeThrough
import com.google.gson.Gson
import dev.adriele.adolescal.model.OvulationInfo
import dev.adriele.adolescare.api.request.InsightsRequest
import dev.adriele.adolescare.api.response.InsightsResponse
import dev.adriele.adolescare.api.websocket.WebSocketClient
import dev.adriele.adolescare.api.websocket.contracts.IWebSocket
import dev.adriele.adolescare.contracts.IChatBot
import dev.adriele.adolescare.ui.LogPeriodActivity
import dev.adriele.adolescare.database.AppDatabaseProvider
import dev.adriele.adolescare.database.repositories.implementation.ChatBotRepositoryImpl
import dev.adriele.adolescare.database.repositories.implementation.CycleLogRepositoryImpl
import dev.adriele.adolescare.database.repositories.implementation.MenstrualHistoryRepositoryImpl
import dev.adriele.adolescare.databinding.FragmentMenstrualTrackerBinding
import dev.adriele.adolescare.helpers.Utility
import dev.adriele.adolescare.helpers.Utility.animateTextViewHeight
import dev.adriele.adolescare.helpers.enums.SymptomCategory
import dev.adriele.adolescare.helpers.enums.SymptomOption
import dev.adriele.adolescare.viewmodel.ChatBotViewModel
import dev.adriele.adolescare.viewmodel.CycleLogViewModel
import dev.adriele.adolescare.viewmodel.MenstrualHistoryViewModel
import dev.adriele.adolescare.viewmodel.factory.ChatBotViewModelFactory
import dev.adriele.adolescare.viewmodel.factory.CycleLogViewModelFactory
import dev.adriele.adolescare.viewmodel.factory.MenstrualHistoryViewModelFactory
import dev.adriele.calendarview.receivers.DateChangeReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private const val USER_ID = "userID"
private const val USER_NAME = "userName"

class MenstrualTrackerFragment : Fragment(), IChatBot.Insight, IWebSocket {
    private var userId: String? = null
    private var userName: String? = null

    private var _binding: FragmentMenstrualTrackerBinding? = null
    private val binding get() = _binding!!

    private lateinit var menstrualHistoryViewModel: MenstrualHistoryViewModel
    private lateinit var menstrualHistoryViewModelFactory: MenstrualHistoryViewModelFactory
    private lateinit var cycleLogViewModel: CycleLogViewModel
    private lateinit var chatBotViewModel: ChatBotViewModel

    private var sexDrives: MutableList<String> = mutableListOf()
    private var moods: MutableList<String> = mutableListOf()
    private var symptoms: MutableList<String> = mutableListOf()
    private var vaginalDischarge: MutableList<String> = mutableListOf()
    private var digestionStool: MutableList<String> = mutableListOf()
    private var pregnancyTest: MutableList<String> = mutableListOf()
    private var physicalActivity: MutableList<String> = mutableListOf()

    private lateinit var dateChangeReceiver: DateChangeReceiver
    private var webSocketClient: WebSocketClient? = null
    private var insightRequest: InsightsRequest? = null
    private var isExpanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getString(USER_ID)
            userName = it.getString(USER_NAME)
        }
        enterTransition = MaterialFadeThrough()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMenstrualTrackerBinding.inflate(layoutInflater, container, false)

        webSocketClient = WebSocketClient("insight", this)

        initialize()
        initializeViewModel()
        afterInitialize()

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    private fun afterInitialize() {
        menstrualHistoryViewModel.ovulationInfo.observe(viewLifecycleOwner) { ovulationInfo ->
            if (ovulationInfo != null) {
                displayOvulationInfo(ovulationInfo)
            } else {
                binding.tvRemarks.text = "❗ Unable to calculate ovulation info."
            }
        }

        menstrualHistoryViewModel.loadLatestHistory(userId!!, requireContext())

        binding.btnLogPeriod.setOnClickListener {
            startActivity(Intent(requireContext(), LogPeriodActivity::class.java)
                .putExtra("userId", userId))
        }
    }

    @SuppressLint("SetTextI18n")
    private fun displayOvulationInfo(info: OvulationInfo) {
        val day = if(info.daysUntilOvulation > 1) "Days" else "Day"

        binding.tvOvulationDays.text = "${info.daysUntilOvulation} $day"
        binding.tvRemarks.text = info.remarks
    }

    private fun initializeViewModel() {
        val menstrualHistoryDao = AppDatabaseProvider.getDatabase(requireActivity()).menstrualHistoryDao()
        val menstrualHistoryRepo = MenstrualHistoryRepositoryImpl(menstrualHistoryDao)
        menstrualHistoryViewModelFactory = MenstrualHistoryViewModelFactory(menstrualHistoryRepo)
        menstrualHistoryViewModel = ViewModelProvider(requireActivity(), menstrualHistoryViewModelFactory)[MenstrualHistoryViewModel::class]

        val cycleLogDao = AppDatabaseProvider.getDatabase(requireContext()).cycleLogDao()
        val cycleDao = AppDatabaseProvider.getDatabase(requireContext()).cycleDao()
        val cycleRepository = CycleLogRepositoryImpl(cycleLogDao, cycleDao)
        val cycleViewModelFactory = CycleLogViewModelFactory(cycleRepository)
        cycleLogViewModel = ViewModelProvider(requireActivity(), cycleViewModelFactory)[CycleLogViewModel::class]

        val chatBotDao = AppDatabaseProvider.getDatabase(requireContext()).conversationDao()
        val chatRepo = ChatBotRepositoryImpl(chatBotDao)
        val chatBotViewModelFactory = ChatBotViewModelFactory(chatRepo, userId!!)
        chatBotViewModel = ViewModelProvider(requireActivity(), chatBotViewModelFactory)[ChatBotViewModel::class]
    }

    @SuppressLint("SetTextI18n")
    private fun initialize() {
        binding.tvGreeting.text = "Good Day, $userName!"
        binding.tvToday.text = Utility.getCurrentDateOnly()

        webSocketClient?.connect()
    }

    override fun onResume() {
        super.onResume()

        dateChangeReceiver = DateChangeReceiver {
            binding.customCalendar.refreshToToday() // or findViewById if needed
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_DATE_CHANGED)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIME_TICK) // optional, fires every minute
        }

        requireContext().registerReceiver(dateChangeReceiver, filter)

        val today = Calendar.getInstance()
        val dateFormatted = SimpleDateFormat("MMM d", Locale.ENGLISH).format(today.time)

        val listOfCategories = SymptomCategory.entries

        lifecycleScope.launch {
            sexDrives.clear()
            moods.clear()
            symptoms.clear()
            vaginalDischarge.clear()
            digestionStool.clear()
            pregnancyTest.clear()
            physicalActivity.clear()

            val existingLog = cycleLogViewModel.getLogByDate(userId = userId ?: "", date = dateFormatted ?: Utility.getCurrentCycleDate())

            val selectedMap = mutableMapOf<String, List<String>>()

            listOfCategories.forEach { categoryEnum ->
                val (hasData, options) = when (categoryEnum) {
                    SymptomCategory.SEX_DRIVE -> existingLog?.sexActivity?.let { true to it }
                        ?: (false to emptyList())

                    SymptomCategory.MOOD -> existingLog?.mood?.let { true to it }
                        ?: (false to emptyList())

                    SymptomCategory.SYMPTOMS -> existingLog?.symptoms?.let { true to it }
                        ?: (false to emptyList())

                    SymptomCategory.VAGINAL_DISCHARGE -> existingLog?.vaginalDischarge?.let { true to it }
                        ?: (false to emptyList())

                    SymptomCategory.DIGESTION_AND_STOOL -> existingLog?.digestionAndStool?.let { true to it }
                        ?: (false to emptyList())

                    SymptomCategory.PREGNANCY_TEST -> existingLog?.pregnancyTestResult?.let { true to it }
                        ?: (false to emptyList())

                    SymptomCategory.PHYSICAL_ACTIVITY -> existingLog?.physicalActivity?.let { true to it }
                        ?: (false to emptyList())
                }

                if (hasData) {
                    selectedMap[categoryEnum.name] = options
                }

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

            val showLL =
                sexDrives.isNotEmpty() || moods.isNotEmpty() || symptoms.isNotEmpty() ||
                        vaginalDischarge.isNotEmpty() || digestionStool.isNotEmpty() ||
                        pregnancyTest.isNotEmpty() || physicalActivity.isNotEmpty()

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

            insightRequest = InsightsRequest(
                sexDrives = sexDrives,
                moods = moods,
                symptoms = symptoms,
                vaginalDischarge = vaginalDischarge,
                digestionAndStool = digestionAndStool,
                pregnancyTest = pregnancyTest,
                physicalActivity = physicalActivity
            )

            lifecycleScope.launch {
                delay(2000)
                webSocketClient?.sendMessage(
                    Gson().toJson(
                        insightRequest
                    )
                )
            }
        } else {
            binding.shimmerInsight.visibility = View.GONE
            binding.cardInsight.visibility = View.GONE
            binding.tvLblInsight.visibility = View.GONE
        }
    }

    override fun onPause() {
        super.onPause()
        requireContext().unregisterReceiver(dateChangeReceiver)
    }

    override fun onResult(result: InsightsResponse) {
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
        binding.let { binding ->
            binding.shimmerInsight.stopShimmer()
            binding.shimmerInsight.visibility = View.GONE
            binding.cardInsight.visibility = View.GONE
            binding.tvLblInsight.visibility = View.GONE
            Snackbar.make(binding.root, "Failed to load insights: $message", Snackbar.LENGTH_SHORT)
                .show()
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
            } catch (e: Exception) {
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
        chatBotViewModel.getInsights(
            insightsRequest = insightRequest,
            this@MenstrualTrackerFragment
        )

        webSocketClient?.ping()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        webSocketClient?.close()
    }

    companion object {
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(userId: String, userName: String) =
            MenstrualTrackerFragment().apply {
                arguments = Bundle().apply {
                    putString(USER_ID, userId)
                    putString(USER_NAME, userName)
                }
            }
    }
}