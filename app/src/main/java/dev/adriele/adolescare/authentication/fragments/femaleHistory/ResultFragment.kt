package dev.adriele.adolescare.authentication.fragments.femaleHistory

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dev.adriele.adolescare.databinding.FragmentResultBinding

class ResultFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var hasPeriod: Boolean? = null
    private var lastPeriodStarted: String? = null
    private var numberOfDays: Int? = null
    private var numberOfWeeks: Int? = null

    private var _binding: FragmentResultBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            hasPeriod = it.getBoolean(FemaleMenstrualHistory.FIRST_PERIOD.name, false)
            lastPeriodStarted = it.getString(FemaleMenstrualHistory.LAST_PERIOD_STARTED_DATE.name)
            numberOfDays = it.getInt(FemaleMenstrualHistory.LAST_PERIOD_LASTED_DAYS.name, 0)
            numberOfWeeks = it.getInt(FemaleMenstrualHistory.NUMBER_OF_WEEKS_SELECTED.name, 0)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentResultBinding.inflate(layoutInflater, container, false)

        binding.tvHasPeriod.text = if(hasPeriod == true) {
             getString(dev.adriele.language.R.string.yes)
        } else {
            getString(dev.adriele.language.R.string.no)
        }

        binding.tvLastPeriodStarted.text = lastPeriodStarted ?: "N/A"
        binding.tvNumberOfDays.text = "$numberOfDays Days"
        binding.tvNumberOfWeeks.text = "$numberOfWeeks Weeks"

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance(hasPeriod: Boolean, lastPeriodStarted: String, numberOfDays: Int, numberOfWeeks: Int) =
            ResultFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(FemaleMenstrualHistory.FIRST_PERIOD.name, hasPeriod)
                    putString(FemaleMenstrualHistory.LAST_PERIOD_STARTED_DATE.name, lastPeriodStarted)
                    putInt(FemaleMenstrualHistory.LAST_PERIOD_LASTED_DAYS.name, numberOfDays)
                    putInt(FemaleMenstrualHistory.NUMBER_OF_WEEKS_SELECTED.name, numberOfWeeks)
                }
            }
    }
}