package dev.adriele.adolescare.authentication.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import dev.adriele.adolescare.authentication.contracts.FragmentDataListener
import dev.adriele.adolescare.databinding.FragmentSelectSexBinding

class SelectSexFragment : Fragment() {
    private var _binding: FragmentSelectSexBinding? = null
    private val binding get() = _binding!!

    private var dataListener: FragmentDataListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSelectSexBinding.inflate(inflater, container, false)
        init()
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FragmentDataListener) {
            dataListener = context
        }
    }

    private fun init() {
        val genderList = listOf(
            getString(dev.adriele.language.R.string.male), getString(dev.adriele.language.R.string.female)
        )

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, genderList)
        binding.autoSelectSex.setAdapter(adapter)

        // 🔹 Listener for item selection
        binding.autoSelectSex.setOnItemClickListener { parent, _, position, _ ->
            val selectedGender = parent.getItemAtPosition(position).toString()
            handleSelection(selectedGender)
        }
    }

    private fun handleSelection(selectedSex: String) {
        val data = mapOf("sex" to selectedSex)
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
}