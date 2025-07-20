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
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialFadeThrough
import dev.adriele.adolescal.model.OvulationInfo
import dev.adriele.adolescare.ui.LogPeriodActivity
import dev.adriele.adolescare.ui.PdfViewerActivity
import dev.adriele.adolescare.ui.VideoPlayerActivity
import dev.adriele.adolescare.adapter.RecentReadWatchAdapter
import dev.adriele.adolescare.helpers.Utility
import dev.adriele.adolescare.api.response.TipResponse
import dev.adriele.adolescare.contracts.IChatBot
import dev.adriele.adolescare.database.AppDatabaseProvider
import dev.adriele.adolescare.database.entities.RecentReadAndWatch
import dev.adriele.adolescare.database.repositories.implementation.ChatBotRepositoryImpl
import dev.adriele.adolescare.database.repositories.implementation.MenstrualHistoryRepositoryImpl
import dev.adriele.adolescare.database.repositories.implementation.ModuleRepositoryImpl
import dev.adriele.adolescare.database.repositories.implementation.RecentReadWatchRepositoryImpl
import dev.adriele.adolescare.database.repositories.implementation.ReminderRepositoryImpl
import dev.adriele.adolescare.databinding.FragmentHomeBinding
import dev.adriele.adolescare.helpers.contracts.IRecentReadAndWatch
import dev.adriele.adolescare.helpers.enums.ModuleContentType
import dev.adriele.adolescare.viewmodel.ChatBotViewModel
import dev.adriele.adolescare.viewmodel.MenstrualHistoryViewModel
import dev.adriele.adolescare.viewmodel.ModuleViewModel
import dev.adriele.adolescare.viewmodel.RecentReadWatchViewModel
import dev.adriele.adolescare.viewmodel.ReminderViewModel
import dev.adriele.adolescare.viewmodel.factory.ChatBotViewModelFactory
import dev.adriele.adolescare.viewmodel.factory.MenstrualHistoryViewModelFactory
import dev.adriele.adolescare.viewmodel.factory.ModuleViewModelFactory
import dev.adriele.adolescare.viewmodel.factory.RecentReadWatchViewModelFactory
import dev.adriele.adolescare.viewmodel.factory.ReminderViewModelFactory
import dev.adriele.language.R

private const val USER_ID = "userID"
private const val USER_NAME = "userName"

class HomeFragment : Fragment(), IRecentReadAndWatch, IChatBot.Tips {
    // TODO: Rename and change types of parameters
    private var userId: String? = null
    private var userName: String? = null

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var chatBotViewModel: ChatBotViewModel
    private lateinit var chatBotViewModelFactory: ChatBotViewModelFactory

    private lateinit var menstrualHistoryViewModel: MenstrualHistoryViewModel
    private lateinit var menstrualHistoryViewModelFactory: MenstrualHistoryViewModelFactory
    private lateinit var recentReadWatchViewModel: RecentReadWatchViewModel
    private lateinit var reminderViewModel: ReminderViewModel

    private lateinit var moduleViewModel: ModuleViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getString(USER_ID)
            userName = it.getString(USER_NAME)
        }

        exitTransition = MaterialFadeThrough()
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

        initializeViewModel()
        afterInit()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        menstrualHistoryViewModel.loadLatestHistory(userId!!, requireContext())
    }

    private fun initializeViewModel() {
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

        val recentReadWatchDao = AppDatabaseProvider.getDatabase(requireActivity()).recentReadAndWatchDao()
        val recentReadWatchRepository = RecentReadWatchRepositoryImpl(recentReadWatchDao)
        val recentReadWatchViewModelFactory = RecentReadWatchViewModelFactory(recentReadWatchRepository)
        recentReadWatchViewModel = ViewModelProvider(this, recentReadWatchViewModelFactory)[RecentReadWatchViewModel::class]

        val reminderDao = AppDatabaseProvider.getDatabase(requireActivity()).reminderDao()
        val reminderRepository = ReminderRepositoryImpl(reminderDao)
        val reminderFactory = ReminderViewModelFactory(reminderRepository)
        reminderViewModel = ViewModelProvider(this, reminderFactory)[ReminderViewModel::class]
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    private fun afterInit() {
        binding.llTips.visibility = View.GONE
        binding.shimmerLayout.startShimmer()

        chatBotViewModel.getTodayTips(this)

        val dateNow = Utility.getCurrentDate()
        binding.tvDateNow.text = dateNow

        menstrualHistoryViewModel.ovulationInfo.observe(viewLifecycleOwner) { ovulationInfo ->
            _binding?.let {
                if (ovulationInfo != null) {
                    displayOvulationInfo(ovulationInfo, it)
                } else {
                    it.tvRemarks.text = "❗ ${R.string.unable_to_calculate_ovulation}"
                }
            }
        }

        binding.btnLogPeriod.setOnClickListener {
            startActivity(Intent(requireActivity(), LogPeriodActivity::class.java)
                .putExtra("userId", userId))
        }

        showShimmer()

        recentReadWatchViewModel.recent.observe(viewLifecycleOwner) { recentList ->
            if(!recentList.isNullOrEmpty()) {
                stopShimmer()
                binding.rvRecent.visibility = View.VISIBLE
                binding.llNoRecent.visibility = View.GONE

                binding.rvRecent.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                binding.rvRecent.adapter = RecentReadWatchAdapter(recentList, moduleViewModel, viewLifecycleOwner,
                    this)
                binding.rvRecent.adapter?.notifyDataSetChanged()
            } else {
                stopShimmer()
                showNoRecent()
            }
        }

        recentReadWatchViewModel.getRecentReadAndWatch()
    }

    private fun showNoRecent() {
        binding.llNoRecent.visibility = View.VISIBLE
        binding.rvRecent.visibility = View.GONE
    }

    private fun stopShimmer() {
        binding.shimmerLayoutRecent.stopShimmer()
        binding.shimmerLayoutRecent.visibility = View.GONE
        binding.llRecent.visibility = View.VISIBLE
    }

    private fun showShimmer() {
        binding.shimmerLayoutRecent.startShimmer()
        binding.llRecent.visibility = View.GONE
    }

    @SuppressLint("SetTextI18n")
    private fun displayOvulationInfo(info: OvulationInfo, view: FragmentHomeBinding) {
        val day = if(info.daysUntilOvulation > 1) "Days" else "Day"

        view.tvOvulationDays.text = "${info.daysUntilOvulation} $day"
        view.tvRemarks.text = info.remarks
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onRecentClick(
        moduleType: ModuleContentType,
        recent: RecentReadAndWatch,
        path: String
    ) {
        when(moduleType) {
            ModuleContentType.PDF -> {
                val intent = Intent(context, PdfViewerActivity::class.java).apply {
                    putExtra("module_id", recent.moduleId)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                startActivity(intent)
            }
            ModuleContentType.VIDEO -> {
                val intent = Intent(requireContext(), VideoPlayerActivity::class.java)
                intent.putExtra("path", path)
                startActivity(intent)
            }
        }
    }

    override fun onResult(result: TipResponse) {
        binding.shimmerLayout.stopShimmer()
        binding.shimmerLayout.visibility = View.GONE
        binding.llTips.visibility = View.VISIBLE

        binding.tvDate.text = result.date
        binding.tvTitle.text = result.title
        binding.tvTips.text = result.tip
    }

    override fun onError(message: String) {
        binding.shimmerLayout.stopShimmer()
        binding.shimmerLayout.visibility = View.GONE
        binding.llTips.visibility = View.VISIBLE

        Snackbar.make(binding.root, "Failed to get todays tips: $message", Snackbar.LENGTH_SHORT).show()
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