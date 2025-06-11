package dev.adriele.adolescare.authentication.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import dev.adriele.adolescare.authentication.contracts.FragmentDataListener
import dev.adriele.adolescare.databinding.FragmentSelectBarangayBinding

class SelectBarangayFragment : Fragment() {
    private var _binding: FragmentSelectBarangayBinding? = null
    private val binding get() = _binding!!

    private var dataListener: FragmentDataListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelectBarangayBinding.inflate(inflater, container, false)

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
        val barangayList = listOf(
            "Ambulong", "Ansiray", "Bagong Sikat", "Bangkal", "Barangay 1", "Barangay 2",
            "Barangay 3", "Barangay 4", "Barangay 5", "Barangay 6", "Barangay 7",
            "Barangay 8", "Batasan", "Bayotbot", "Bubog", "Buri", "Camburay", "Caminawit",
            "Catayungan", "Central", "Iling Proper", "Inasakan", "Ipil", "La Curva",
            "Labangan Iling", "Labangan Poblacion", "Mabini", "Magbay", "Mangarin",
            "Mapaya", "Monte Claro", "Murtha", "Naibuan", "Natandol", "Pag-asa", "Pawican",
            "San Agustin", "San Isidro", "San Roque"
        )

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, barangayList)
        binding.autoSelectBarangay.setAdapter(adapter)

        // 🔹 Listener for item selection
        binding.autoSelectBarangay.setOnItemClickListener { parent, _, position, _ ->
            val selectedBarangay = parent.getItemAtPosition(position).toString()
            handleSelection(selectedBarangay)
        }
    }

    private fun handleSelection(selectedBarangay: String) {
        val data = mapOf("barangay" to selectedBarangay)
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