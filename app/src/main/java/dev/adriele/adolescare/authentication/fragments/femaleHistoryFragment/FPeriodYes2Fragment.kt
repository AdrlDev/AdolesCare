package dev.adriele.adolescare.authentication.fragments.femaleHistoryFragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dev.adriele.adolescare.R
import dev.adriele.adolescare.authentication.contracts.FragmentDataListener
import dev.adriele.adolescare.databinding.FragmentFPeriodYes2Binding

class FPeriodYes2Fragment : Fragment() {
    private var _binding: FragmentFPeriodYes2Binding? = null
    private val binding get() = _binding!!

    private var dataListener: FragmentDataListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentFPeriodYes2Binding.inflate(layoutInflater, container, false)

        init()

        return binding.root
    }

    private fun handleSelection(lastPeriodLasted: Boolean) {
        val data = mapOf(FemaleMenstrualHistory.LAST_PERIOD_LASTED.name to lastPeriodLasted)
        dataListener?.onDataCollected(data)
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
}