package dev.adriele.adolescare.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import dev.adriele.adolescare.LogPeriodActivity
import dev.adriele.adolescare.ModuleContentType
import dev.adriele.adolescare.Utility
import dev.adriele.adolescare.api.response.TipResponse
import dev.adriele.adolescare.contracts.IChatBot
import dev.adriele.adolescare.database.AppDatabaseProvider
import dev.adriele.adolescare.database.repositories.implementation.ChatBotRepositoryImpl
import dev.adriele.adolescare.database.repositories.implementation.MenstrualHistoryRepositoryImpl
import dev.adriele.adolescare.database.repositories.implementation.ModuleRepositoryImpl
import dev.adriele.adolescare.databinding.FragmentHomeBinding
import dev.adriele.adolescare.model.OvulationInfo
import dev.adriele.adolescare.viewmodel.ChatBotViewModel
import dev.adriele.adolescare.viewmodel.MenstrualHistoryViewModel
import dev.adriele.adolescare.viewmodel.ModuleViewModel
import dev.adriele.adolescare.viewmodel.factory.ChatBotViewModelFactory
import dev.adriele.adolescare.viewmodel.factory.MenstrualHistoryViewModelFactory
import dev.adriele.adolescare.viewmodel.factory.ModuleViewModelFactory

private const val USER_ID = "userID"
private const val USER_NAME = "userName"

class HomeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var userId: String? = null
    private var userName: String? = null

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var chatBotViewModel: ChatBotViewModel
    private lateinit var chatBotViewModelFactory: ChatBotViewModelFactory

    private lateinit var menstrualHistoryViewModel: MenstrualHistoryViewModel
    private lateinit var menstrualHistoryViewModelFactory: MenstrualHistoryViewModelFactory

    private lateinit var moduleViewModel: ModuleViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getString(USER_ID)
            userName = it.getString(USER_NAME)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(layoutInflater, container, false)

        // Apply insets to the fragment root
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                systemBarsInsets.left,
                systemBarsInsets.top,
                systemBarsInsets.right,
                systemBarsInsets.bottom
            )
            insets
        }

        init()
        afterInit()

        return binding.root
    }

    private fun init() {
        val conversationDao = AppDatabaseProvider.getDatabase(requireActivity()).conversationDao()
        val chatbotRepo = ChatBotRepositoryImpl(conversationDao)
        chatBotViewModelFactory = ChatBotViewModelFactory(chatbotRepo, userId!!)
        chatBotViewModel = ViewModelProvider(this, chatBotViewModelFactory)[ChatBotViewModel::class]

        val menstrualHistoryDao = AppDatabaseProvider.getDatabase(requireActivity()).menstrualHistoryDao()
        val menstrualHistoryRepo = MenstrualHistoryRepositoryImpl(menstrualHistoryDao)
        menstrualHistoryViewModelFactory = MenstrualHistoryViewModelFactory(menstrualHistoryRepo)
        menstrualHistoryViewModel = ViewModelProvider(this, menstrualHistoryViewModelFactory)[MenstrualHistoryViewModel::class]

        val moduleDao = AppDatabaseProvider.getDatabase(requireActivity()).moduleDao()
        val moduleRepository = ModuleRepositoryImpl(moduleDao)
        val moduleViewModelFactory = ModuleViewModelFactory(moduleRepository)
        moduleViewModel = ViewModelProvider(this, moduleViewModelFactory)[ModuleViewModel::class]
    }

    @SuppressLint("SetTextI18n")
    private fun afterInit() {
        binding.llTips.visibility = View.GONE
        binding.shimmerLayout.startShimmer()

        chatBotViewModel.getTodayTips(object : IChatBot.Tips {
            override fun onResult(result: TipResponse) {
                binding.shimmerLayout.stopShimmer()
                binding.shimmerLayout.visibility = View.GONE
                binding.llTips.visibility = View.VISIBLE

                binding.tvDate.text = result.date
                binding.tvTitle.text = result.title
                binding.tvTips.text = result.tip
            }
        })

        val dateNow = Utility.getCurrentDate()
        binding.tvDateNow.text = dateNow

        menstrualHistoryViewModel.loadLatestHistory(userId!!, requireContext())

        menstrualHistoryViewModel.ovulationInfo.observe(viewLifecycleOwner) { ovulationInfo ->
            if (ovulationInfo != null) {
                displayOvulationInfo(ovulationInfo)
            } else {
                binding.tvRemarks.text = "❗ Unable to calculate ovulation info."
            }
        }

        binding.btnLogPeriod.setOnClickListener {
            startActivity(Intent(requireContext(), LogPeriodActivity::class.java)
                .putExtra("userId", userId))
        }

        moduleViewModel.getAllModules(ModuleContentType.PDF).observe(viewLifecycleOwner) { modules ->
            if (modules != null) {
                val path = modules[0].contentUrl
                val file = Utility.copyAssetToCache(requireContext(), path)
                val thumbnail = Utility.generatePdfThumbnail(file)

                binding.imgModule.setImageBitmap(thumbnail)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun displayOvulationInfo(info: OvulationInfo) {
        val day = if(info.daysUntilOvulation > 1) "Days" else "Day"

        binding.tvOvulationDays.text = "${info.daysUntilOvulation} $day"
        binding.tvRemarks.text = info.remarks
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(userId: String, userName: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(USER_ID, userId)
                    putString(USER_NAME, userName)
                }
            }
    }
}