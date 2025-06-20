package dev.adriele.adolescare.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import dev.adriele.adolescal.model.OvulationInfo
import dev.adriele.adolescare.LogPeriodActivity
import dev.adriele.adolescare.database.AppDatabaseProvider
import dev.adriele.adolescare.database.repositories.implementation.MenstrualHistoryRepositoryImpl
import dev.adriele.adolescare.databinding.FragmentMenstrualTrackerBinding
import dev.adriele.adolescare.helpers.Utility
import dev.adriele.adolescare.viewmodel.MenstrualHistoryViewModel
import dev.adriele.adolescare.viewmodel.factory.MenstrualHistoryViewModelFactory
import dev.adriele.calendarview.receivers.DateChangeReceiver

private const val USER_ID = "userID"
private const val USER_NAME = "userName"

class MenstrualTrackerFragment : Fragment() {
    private var userId: String? = null
    private var userName: String? = null

    private var _binding: FragmentMenstrualTrackerBinding? = null
    private val binding get() = _binding!!

    private lateinit var menstrualHistoryViewModel: MenstrualHistoryViewModel
    private lateinit var menstrualHistoryViewModelFactory: MenstrualHistoryViewModelFactory

    private lateinit var dateChangeReceiver: DateChangeReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getString(USER_ID)
            userName = it.getString(USER_NAME)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMenstrualTrackerBinding.inflate(layoutInflater, container, false)

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
        menstrualHistoryViewModel = ViewModelProvider(this, menstrualHistoryViewModelFactory)[MenstrualHistoryViewModel::class]
    }

    @SuppressLint("SetTextI18n")
    private fun initialize() {
        binding.tvGreeting.text = "Good Day, $userName!"
        binding.tvToday.text = Utility.getCurrentDateOnly()
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
    }

    override fun onPause() {
        super.onPause()
        requireContext().unregisterReceiver(dateChangeReceiver)
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