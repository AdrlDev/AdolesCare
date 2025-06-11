package dev.adriele.adolescare.authentication.fragments.femaleHistoryFragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dev.adriele.adolescare.Utility.animateTypingWithCursor
import dev.adriele.adolescare.authentication.contracts.FragmentDataListener
import dev.adriele.adolescare.databinding.FragmentFLastPeriodNoMsgBinding
import kotlinx.coroutines.Job

class FLastPeriodNoMsgFragment : Fragment() {
    private var _binding: FragmentFLastPeriodNoMsgBinding? = null
    private val binding get() = _binding!!

    private var typingJob: Job? = null

    private var dataListener: FragmentDataListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentFLastPeriodNoMsgBinding.inflate(layoutInflater, container, false)

        binding.tvMessage.animateTypingWithCursor(
            getString(dev.adriele.language.R.string.last_period_started_no_msg),
            onTypingComplete = {
                binding.btnChange.visibility = View.VISIBLE // ✅ Show when done
            }
        )

        binding.btnChange.setOnClickListener {
            val data = mapOf(FemaleMenstrualHistory.CHANGE_LAST_PERIOD_STARTED.name to true)
            dataListener?.onDataCollected(data)
        }

        return binding.root
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

    override fun onDestroy() {
        super.onDestroy()
        typingJob?.cancel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}