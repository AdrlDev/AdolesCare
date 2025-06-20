package dev.adriele.adolescare.authentication.fragments.femaleHistory

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.snackbar.Snackbar
import dev.adriele.adolescare.R
import dev.adriele.adolescare.helpers.Utility
import dev.adriele.adolescare.authentication.contracts.FragmentDataListener
import dev.adriele.adolescare.databinding.FragmentSelectLPSDateBinding

class SelectLPSDateFragment : Fragment(), Utility.OnDatePickedCallback {
    private var _binding: FragmentSelectLPSDateBinding? = null
    private val binding get() = _binding!!

    private var dataListener: FragmentDataListener? = null
    private var selectedDate: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSelectLPSDateBinding.inflate(layoutInflater, container, false)

        init()

        return binding.root
    }

    private fun init() {
        Utility.setupDatePicker(binding.btnYes, "Select when your last period started", false, requireActivity(), this)

        binding.toggleGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btn_no -> {
                        if (selectedDate == null) {
                            Snackbar.make(
                                binding.root,
                                "Please select a date of your last period.",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        } else {
                            handleSelection(selectedDate!!)
                        }
                    }
                }
            }
        }
    }

    private fun handleSelection(selected: String) {
        val data = mapOf(FemaleMenstrualHistory.LAST_PERIOD_STARTED_DATE.name to selected)
        dataListener?.onDataCollected(data)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FragmentDataListener) {
            dataListener = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        dataListener = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDatePicked(formattedDate: String, computedResult: String) {
        this.selectedDate = formattedDate
        binding.tvSelectedDate.text = formattedDate
    }
}