package dev.adriele.adolescare.ui

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.transition.platform.MaterialSharedAxis
import dev.adriele.adolescare.BaseActivity
import dev.adriele.adolescare.adapter.CalendarAdapter
import dev.adriele.adolescare.database.AppDatabaseProvider
import dev.adriele.adolescare.database.repositories.implementation.MenstrualHistoryRepositoryImpl
import dev.adriele.adolescare.databinding.ActivityLogPeriodYearlyBinding
import dev.adriele.adolescare.helpers.Utility
import dev.adriele.adolescare.viewmodel.MenstrualHistoryViewModel
import dev.adriele.adolescare.viewmodel.factory.MenstrualHistoryViewModelFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class LogPeriodYearlyActivity : BaseActivity(), CalendarAdapter.ICalendar {
    private lateinit var binding: ActivityLogPeriodYearlyBinding

    private lateinit var menstrualHistoryViewModel: MenstrualHistoryViewModel

    private var userId: String? = null
    private var displayedYear = Calendar.getInstance().get(Calendar.YEAR)

    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedAxis = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        window.enterTransition = sharedAxis
        window.exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLogPeriodYearlyBinding.inflate(layoutInflater)

        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        userId = intent.getStringExtra("userId")

        initializeViewModel()
        displayFullYearCalendar(displayedYear)
        handleButtons()
    }

    private fun handleButtons() {
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnNext.isEnabled = displayedYear < Calendar.getInstance().get(Calendar.YEAR) + 10
        binding.btnPrev.isEnabled = displayedYear > 1900

        binding.btnPrev.setOnClickListener {
            displayedYear--
            displayFullYearCalendar(displayedYear)
        }

        binding.btnNext.setOnClickListener {
            displayedYear++
            displayFullYearCalendar(displayedYear)
        }
    }

    private fun displayFullYearCalendar(year: Int) {
        binding.tvYear.text = year.toString()
        binding.rvFl.visibility = View.GONE
        binding.shimmerLayout.visibility = View.VISIBLE
        binding.shimmerLayout.startShimmer()

        binding.rvCalendar.animate().alpha(0f).setDuration(150).withEndAction {
            menstrualHistoryViewModel.getMensHistory(userId ?: "")

            menstrualHistoryViewModel.mensHistory.observe(this) { history ->
                if (history != null) {
                    val lmp = history.lastPeriodStart ?: Utility.getTwoWeeksAgo()
                    val periodDays = history.periodDurationDays ?: 3
                    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH)

                    val periodDates = mutableListOf<Calendar>()
                    val calendar = Calendar.getInstance().apply {
                        time = sdf.parse(lmp)!!
                    }

                    repeat(periodDays) {
                        periodDates.add(calendar.clone() as Calendar)
                        calendar.add(Calendar.DATE, 1)
                    }

                    val adapter = CalendarAdapter(this, year, periodDates, this)
                    binding.rvCalendar.layoutManager = GridLayoutManager(this, 2)
                    binding.rvCalendar.adapter = adapter

                    binding.rvCalendar.animate().alpha(1f).setDuration(150).start()

                    hideShimmer()
                }
            }
        }.start()
    }

    private fun hideShimmer() {
        binding.shimmerLayout.stopShimmer()
        binding.shimmerLayout.visibility = View.GONE
        binding.rvFl.visibility = View.VISIBLE
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
    }

    override fun onCalendarClick(date: Calendar) {
        val bundle = ActivityOptions.makeSceneTransitionAnimation(this@LogPeriodYearlyActivity).toBundle()
        val intent = Intent(this, LogPeriodActivity::class.java).apply {
            putExtra("selectedDate", date.timeInMillis)
            putExtra("userId", userId)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent, bundle)
    }
}