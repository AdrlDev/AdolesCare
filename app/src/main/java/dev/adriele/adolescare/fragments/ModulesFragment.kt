package dev.adriele.adolescare.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import dev.adriele.adolescare.PdfViewerActivity
import dev.adriele.adolescare.helpers.enums.ModuleContentType
import dev.adriele.adolescare.helpers.enums.PDFModulesCategory
import dev.adriele.adolescare.adapter.PdfModulesAdapter
import dev.adriele.adolescare.database.AppDatabaseProvider
import dev.adriele.adolescare.database.entities.RecentReadAndWatch
import dev.adriele.adolescare.database.repositories.implementation.ModuleRepositoryImpl
import dev.adriele.adolescare.database.repositories.implementation.RecentReadWatchRepositoryImpl
import dev.adriele.adolescare.databinding.FragmentModulesBinding
import dev.adriele.adolescare.dialogs.MyLoadingDialog
import dev.adriele.adolescare.helpers.Utility
import dev.adriele.adolescare.helpers.contracts.IModules
import dev.adriele.adolescare.model.CategoryModuleGroup
import dev.adriele.adolescare.viewmodel.ModuleViewModel
import dev.adriele.adolescare.viewmodel.RecentReadWatchViewModel
import dev.adriele.adolescare.viewmodel.factory.ModuleViewModelFactory
import dev.adriele.adolescare.viewmodel.factory.RecentReadWatchViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val USER_ID = "userID"

class ModulesFragment : Fragment(), IModules.PDF {
    // TODO: Rename and change types of parameters
    private var userId: String? = null

    private var _binding: FragmentModulesBinding? = null
    private val binding get() = _binding!!

    private lateinit var moduleViewModel: ModuleViewModel
    private lateinit var recentReadWatchViewModel: RecentReadWatchViewModel

    private lateinit var loadingDialog: MyLoadingDialog
    private var pdfPosition: Int = 0
    private var pdfPath: String? = null

    private var groupModules: MutableList<CategoryModuleGroup> = mutableListOf()

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
                lifecycleScope.launch(Dispatchers.Default) {
                    val grouped = PDFModulesCategory.entries.mapNotNull { categoryEnum ->
                        val category = categoryEnum.category
                        val normalizedEnumCategory = category
                            .lowercase()
                            .replace("ﬁ", "fi")
                            .replace(Regex("[^a-z0-9 ]"), "")
                            .replace(Regex("\\s+"), " ")
                            .trim()

                        Log.d("ModulesFragment", "Category: $normalizedEnumCategory")

                        val filtered = modules.filter { module ->
                            val normalizedModuleCategory = module.category
                                .lowercase()
                                .replace("ﬁ", "fi")
                                .replace(Regex("[^a-z0-9 ]"), "")
                                .replace(Regex("\\s+"), " ")
                                .trim()

                            normalizedModuleCategory == normalizedEnumCategory
                        }

                        if (filtered.isNotEmpty()) {
                            CategoryModuleGroup(normalizedEnumCategory, filtered)
                        } else {
                            null
                        }
                    }

                    withContext(Dispatchers.Main) {
                        Log.d("ModulesFragment", "Group Category size: ${grouped.size}")
                        Log.d("ModulesFragment", "Group Category: " + Gson().toJson(grouped))

                        displayGroupedModules(grouped)
                    }
                }
            }
        }

        moduleViewModel.getAllModules(ModuleContentType.PDF)
    }

    private fun displayGroupedModules(grouped: List<CategoryModuleGroup>) {
        groupModules.clear()
        groupModules.addAll(grouped)

        binding.rvModules.layoutManager = LinearLayoutManager(requireContext())
        binding.rvModules.setHasFixedSize(true)
        binding.rvModules.setItemViewCacheSize(20) // optional
        binding.rvModules.setRecycledViewPool(RecyclerView.RecycledViewPool()) // reuse
        binding.rvModules.adapter = PdfModulesAdapter(grouped, viewLifecycleOwner.lifecycleScope, this)
        binding.llModules.visibility = View.VISIBLE
        hideShimmer()
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
        recentReadWatchViewModel.addRecentStatus.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                lifecycleScope.launch {
                    val cachedFile = withContext(Dispatchers.IO) {
                        Utility.copyAssetToCache(requireContext(), pdfPath!!)
                    }

                    val uri = FileProvider.getUriForFile(
                        requireContext(),
                        "${requireContext().packageName}.fileprovider",
                        cachedFile
                    )

                    val intent = Intent(requireContext(), PdfViewerActivity::class.java).apply {
                        putExtra("pdf_uri", uri.toString())
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }

                    loadingDialog.dismiss() // move after loading is done
                    startActivity(intent)
                }
            }
        }
    }

    override fun onPdfClick(position: Int, pdfCategoryPosition: Int, path: String) {
        loadingDialog.show("Please wait...")
        pdfPosition = position
        pdfPath = path

        recentReadWatchViewModel.addRecent(RecentReadAndWatch(
            moduleId = groupModules[pdfCategoryPosition].modules[position].id,
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