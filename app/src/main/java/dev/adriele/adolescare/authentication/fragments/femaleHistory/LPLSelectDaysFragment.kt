package dev.adriele.adolescare.authentication.fragments.femaleHistory

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.google.android.material.transition.MaterialFadeThrough
import dev.adriele.adolescare.authentication.contracts.FragmentDataListener
import dev.adriele.adolescare.databinding.FragmentLPLSelectDaysBinding

class LPLSelectDaysFragment : Fragment() {
    private var _binding: FragmentLPLSelectDaysBinding? = null
    private val binding get() = _binding!!

    private var dataListener: FragmentDataListener? = null
    private var selectedDays: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentLPLSelectDaysBinding.inflate(layoutInflater, container, false)

        init()

        return binding.root
    }

    private fun init() {
        val days = listOf(
            "1", "2", "3", "4", "5", "6", "7"
        )

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, days)
        binding.autoDays.setAdapter(adapter)

        // 🔹 Listener for item selection
        binding.autoDays.setOnItemClickListener { parent, _, position, _ ->
            selectedDays = parent.getItemAtPosition(position).toString()
        }

        binding.btnNext.setOnClickListener {
            selectedDays?.let {
                handleSelection(it.toInt())
            }
        }
    }

    private fun handleSelection(selected: Int) {
        val data = mapOf(FemaleMenstrualHistory.LAST_PERIOD_LASTED_DAYS.name to selected)
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
}