package dev.adriele.adolescare.authentication.fragments.femaleHistory

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.transition.MaterialFadeThrough
import dev.adriele.adolescare.helpers.Utility.animateTypingWithCursor
import dev.adriele.adolescare.databinding.FragmentFPeriodNoBinding
import kotlinx.coroutines.Job

class FPeriodNoFragment : Fragment() {
    private var _binding: FragmentFPeriodNoBinding? = null
    private val binding get() = _binding!!

    private var typingJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentFPeriodNoBinding.inflate(layoutInflater, container, false)

        binding.tvMessage.animateTypingWithCursor(getString(dev.adriele.language.R.string.first_period_no_msg))

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        typingJob?.cancel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}