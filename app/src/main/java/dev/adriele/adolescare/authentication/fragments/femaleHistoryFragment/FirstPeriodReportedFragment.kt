package dev.adriele.adolescare.authentication.fragments.femaleHistoryFragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dev.adriele.adolescare.R
import dev.adriele.adolescare.authentication.contracts.FragmentDataListener
import dev.adriele.adolescare.databinding.FragmentFirstPeriodReportedBinding

class FirstPeriodReportedFragment : Fragment() {
    private var _binding: FragmentFirstPeriodReportedBinding? = null
    private val binding get() = _binding!!

    private var dataListener: FragmentDataListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentFirstPeriodReportedBinding.inflate(inflater, container, false)

        init()

        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FragmentDataListener) {
            dataListener = context
        }
    }

    private fun handleSelection(firstPeriodReported: Boolean) {
        val data = mapOf("firstPeriodReported" to firstPeriodReported)
        dataListener?.onDataCollected(data)
    }

    override fun onDetach() {
        super.onDetach()
        dataListener = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun init() {
        binding.toggleGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btn_yes -> {
                        // User selected "Yes"
                        handleSelection(true)
                    }
                    R.id.btn_no -> {
                        // User selected "No"
                        handleSelection(false)
                    }
                }
            }
        }
    }
}