package dev.adriele.adolescare.authentication.fragments.femaleHistoryFragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dev.adriele.adolescare.databinding.FragmentFPeriodNoLastBinding

class FPeriodNoLastFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var hasPeriod: Boolean? = null

    private var _binding: FragmentFPeriodNoLastBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            hasPeriod = it.getBoolean(FemaleMenstrualHistory.FIRST_PERIOD.name, false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentFPeriodNoLastBinding.inflate(layoutInflater, container, false)

        binding.tvHasPeriod.text = if(hasPeriod == true) {
             getString(dev.adriele.language.R.string.yes)
        } else {
            getString(dev.adriele.language.R.string.no)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance(hasPeriod: Boolean) =
            FPeriodNoLastFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(FemaleMenstrualHistory.FIRST_PERIOD.name, hasPeriod)
                }
            }
    }
}