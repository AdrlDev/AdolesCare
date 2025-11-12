package dev.adriele.adolescare.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import dev.adriele.adolescare.contracts.ILogPeriod
import dev.adriele.adolescare.database.AppDatabaseProvider
import dev.adriele.adolescare.database.entities.MenstrualHistoryEntity
import dev.adriele.adolescare.database.repositories.implementation.MenstrualHistoryRepositoryImpl
import dev.adriele.adolescare.databinding.FragmentLogNewPeriodBinding
import dev.adriele.adolescare.helpers.Utility
import dev.adriele.adolescare.viewmodel.MenstrualHistoryViewModel
import dev.adriele.adolescare.viewmodel.factory.MenstrualHistoryViewModelFactory

class LogNewPeriodFragment : BottomSheetDialogFragment(), Utility.DatePickedCallback {
    private var _binding: FragmentLogNewPeriodBinding? = null
    private val binding get() = _binding!!

    private lateinit var menstrualHistoryViewModel: MenstrualHistoryViewModel
    private var userId: String? = null
    private var formattedDate: String? = null
    private var callback: ILogPeriod? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentLogNewPeriodBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menstrualHistoryDao = AppDatabaseProvider.getDatabase(requireContext()).menstrualHistoryDao()
        val menstrualHistoryRepo = MenstrualHistoryRepositoryImpl(menstrualHistoryDao)
        val menstrualHistoryViewModelFactory =
            MenstrualHistoryViewModelFactory(menstrualHistoryRepo)
        menstrualHistoryViewModel = ViewModelProvider(
            this,
            menstrualHistoryViewModelFactory
        )[MenstrualHistoryViewModel::class]
        initialize()
    }

    private fun initialize() {

        binding.etSelectDate.setOnClickListener {
            Utility.showDatePicker(requireContext(), childFragmentManager, this)
        }

        binding.btnSave.setOnClickListener {
            val date = binding.etSelectDate.text.toString()
            val days = binding.etSelectPeriodDays.text.toString()
            val weeks = binding.etSelectPeriodWeeks.text.toString()

            when {
                date.isEmpty() -> Snackbar.make(binding.root, "Please select last period date",
                    Snackbar.LENGTH_SHORT).show()
                days.isEmpty() -> Snackbar.make(binding.root, "Please add days of periods",
                    Snackbar.LENGTH_SHORT).show()
                weeks.isEmpty() -> Snackbar.make(binding.root, "Please add weeks of periods",
                    Snackbar.LENGTH_SHORT).show()
                else -> {
                    // If no existing record, you may want to insert instead
                    Log.e("USER_ID", userId ?: "null")
                    Log.e("FORMATTED_DATE", formattedDate ?: "null")

                    val newEntry = MenstrualHistoryEntity(
                        userId = userId ?: "",
                        firstPeriodReported = false,
                        lastPeriodStart = formattedDate,
                        periodDurationDays = days.toInt(),
                        cycleIntervalWeeks = weeks.toInt()
                    )

                    callback?.onSaveNewLog(newEntry)
                    dismiss()
                }
            }
        }
    }

    override fun onDatePicked(formattedDate: String) {
        this.formattedDate = formattedDate
        binding.etSelectDate.setText(formattedDate)
    }

    companion object {
        @JvmStatic
        fun newInstance(userId: String?, callback: ILogPeriod) =
            LogNewPeriodFragment().apply {
                this.userId = userId
                this.callback = callback
            }
    }
}