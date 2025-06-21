package dev.adriele.adolescare.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.google.gson.Gson
import dev.adriele.adolescare.ui.PdfViewerActivity
import dev.adriele.adolescare.helpers.enums.ModuleContentType
import dev.adriele.adolescare.adapter.PdfModulesAdapter
import dev.adriele.adolescare.database.AppDatabaseProvider
import dev.adriele.adolescare.database.entities.LearningModule
import dev.adriele.adolescare.database.entities.RecentReadAndWatch
import dev.adriele.adolescare.database.repositories.implementation.ModuleRepositoryImpl
import dev.adriele.adolescare.database.repositories.implementation.RecentReadWatchRepositoryImpl
import dev.adriele.adolescare.databinding.FragmentModulesBinding
import dev.adriele.adolescare.dialogs.MyLoadingDialog
import dev.adriele.adolescare.helpers.contracts.IModules
import dev.adriele.adolescare.viewmodel.ModuleViewModel
import dev.adriele.adolescare.viewmodel.RecentReadWatchViewModel
import dev.adriele.adolescare.viewmodel.factory.ModuleViewModelFactory
import dev.adriele.adolescare.viewmodel.factory.RecentReadWatchViewModelFactory
import kotlinx.coroutines.launch

private const val USER_ID = "userID"

class ModulesFragment : Fragment(), IModules.PDF {
    // TODO: Rename and change types of parameters
    private var userId: String? = null

    private var _binding: FragmentModulesBinding? = null
    private val binding get() = _binding!!

    private lateinit var moduleViewModel: ModuleViewModel
    private lateinit var recentReadWatchViewModel: RecentReadWatchViewModel

    private lateinit var loadingDialog: MyLoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getString(USER_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentModulesBinding.inflate(layoutInflater, container, false)

        loadingDialog = MyLoadingDialog(requireContext())
        initializeViewModel()
        loadModules()
        afterInitialize()

        return binding.root
    }

    private fun loadModules() {
        binding.shimmerLayout.startShimmer()
        binding.llModules.visibility = View.GONE

        moduleViewModel.modules.observe(viewLifecycleOwner) { modules ->
            if (!modules.isNullOrEmpty()) {
                lifecycleScope.launch {
                    Log.e("MODULES_LIST", Gson().toJson(modules))

                    binding.rvModules.layoutManager = GridLayoutManager(requireContext(), 2)
                    binding.rvModules.setHasFixedSize(true)
                    binding.rvModules.adapter = PdfModulesAdapter(modules, this@ModulesFragment)
                    binding.llModules.visibility = View.VISIBLE
                    hideShimmer()
                }
            }
        }

        moduleViewModel.getAllModules(ModuleContentType.PDF)
    }

    private fun hideShimmer() {
        binding.rvModules.visibility = View.VISIBLE
        binding.shimmerLayout.visibility = View.GONE
        binding.shimmerLayout.stopShimmer()
    }

    private fun initializeViewModel() {
        val moduleDao = AppDatabaseProvider.getDatabase(requireActivity()).moduleDao()
        val moduleRepository = ModuleRepositoryImpl(moduleDao)
        val moduleViewModelFactory = ModuleViewModelFactory(moduleRepository)
        moduleViewModel = ViewModelProvider(this, moduleViewModelFactory)[ModuleViewModel::class]

        val recentReadWatchDao = AppDatabaseProvider.getDatabase(requireActivity()).recentReadAndWatchDao()
        val recentReadWatchRepository = RecentReadWatchRepositoryImpl(recentReadWatchDao)
        val recentReadWatchViewModelFactory = RecentReadWatchViewModelFactory(recentReadWatchRepository)
        recentReadWatchViewModel = ViewModelProvider(this, recentReadWatchViewModelFactory)[RecentReadWatchViewModel::class]
    }

    private fun afterInitialize() {
        recentReadWatchViewModel.addRecentStatus.observe(viewLifecycleOwner) { (isSuccess, moduleId) ->
            if (isSuccess) {
                lifecycleScope.launch {
                    moduleViewModel.getModuleByIdLive(moduleId).observe(viewLifecycleOwner) { module ->
                        if(module != null) {
                            val intent = Intent(requireContext(), PdfViewerActivity::class.java).apply {
                                putExtra("module_category", module.category)
                                putExtra("module_url", module.contentUrl)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }

                            loadingDialog.dismiss()
                            startActivity(intent)
                        }
                    }
                }
            }
        }
    }

    override fun onPdfClick(module: LearningModule) {
        recentReadWatchViewModel.addRecent(RecentReadAndWatch(
            moduleId = module.id,
            timestamp = System.currentTimeMillis()
        ))
    }

    companion object {
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(userId: String) =
            ModulesFragment().apply {
                arguments = Bundle().apply {
                    putString(USER_ID, userId)
                }
            }
    }
}