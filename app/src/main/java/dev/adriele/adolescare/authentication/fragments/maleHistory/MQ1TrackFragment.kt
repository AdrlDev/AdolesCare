package dev.adriele.adolescare.authentication.fragments.maleHistory

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dev.adriele.adolescare.authentication.contracts.FragmentDataListener
import dev.adriele.adolescare.databinding.FragmentMQ1TrackBinding

class MQ1TrackFragment : Fragment() {
    private var _binding: FragmentMQ1TrackBinding? = null
    private val binding get() = _binding!!

    private var dataListener: FragmentDataListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMQ1TrackBinding.inflate(inflater, container, false)

        binding.btnOk.setOnClickListener {
            handleSelection(MaleQuestions.CONFIRM.value)
        }

        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FragmentDataListener) {
            dataListener = context
        }
    }

    private fun handleSelection(selected: Int) {
        val data = mapOf("male_q1_track" to selected)
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