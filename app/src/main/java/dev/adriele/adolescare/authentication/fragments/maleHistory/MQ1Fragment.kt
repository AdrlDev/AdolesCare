package dev.adriele.adolescare.authentication.fragments.maleHistory

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.transition.MaterialFadeThrough
import dev.adriele.adolescare.authentication.contracts.FragmentDataListener
import dev.adriele.adolescare.databinding.FragmentMQ1Binding

class MQ1Fragment : Fragment() {
    private var _binding: FragmentMQ1Binding? = null
    private val binding get() = _binding!!

    private var dataListener: FragmentDataListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMQ1Binding.inflate(layoutInflater, container, false)

        binding.card1.setOnClickListener {
            binding.card1.isChecked = true
            binding.card2.isChecked = false
        }

        binding.card2.setOnClickListener {
            binding.card2.isChecked = true
            binding.card1.isChecked = false
        }

        binding.card1.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                handleSelection(MaleQuestions.TRACK_PARTNERS_MENS.value)
            }
        }

        binding.card2.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                handleSelection(MaleQuestions.WANT_TO_LEARN_ABOUT_PREGNANCY.value)
            }
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
        val data = mapOf("male_q1" to selected)
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