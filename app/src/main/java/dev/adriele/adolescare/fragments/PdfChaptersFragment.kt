package dev.adriele.adolescare.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.transition.MaterialFadeThrough
import dev.adriele.adolescare.ui.PdfViewerActivity
import dev.adriele.adolescare.adapter.ModuleItemAdapter
import dev.adriele.adolescare.database.AppDatabaseProvider
import dev.adriele.adolescare.database.entities.LearningModule
import dev.adriele.adolescare.database.repositories.implementation.ModuleRepositoryImpl
import dev.adriele.adolescare.databinding.FragmentPdfChaptersBinding
import dev.adriele.adolescare.dialogs.MyLoadingDialog
import dev.adriele.adolescare.helpers.contracts.IModules
import dev.adriele.adolescare.helpers.enums.ModuleContentType
import dev.adriele.adolescare.viewmodel.ModuleViewModel
import dev.adriele.adolescare.viewmodel.PdfSearchViewModel
import dev.adriele.adolescare.viewmodel.factory.ModuleViewModelFactory

private const val PDF_CATEGORY = "pdfChapter"

class PdfChaptersFragment : Fragment(), IModules.PDF {
    // TODO: Rename and change types of parameters
    private var pdfCategory: String? = null

    private var _binding: FragmentPdfChaptersBinding? = null
    private val binding get() = _binding!!

    private lateinit var loadingDialog: MyLoadingDialog
    private lateinit var searchViewModel: PdfSearchViewModel
    private lateinit var moduleViewModel: ModuleViewModel

    private lateinit var moduleAdapter: ModuleItemAdapter

    private lateinit var rvChapters: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            pdfCategory = it.getString(PDF_CATEGORY)
        }
        enterTransition = MaterialFadeThrough()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentPdfChaptersBinding.inflate(inflater, container, false)

        initializeViewModel()
        initializeViews()
        initialize()
        afterInitialize()

        return binding.root
    }

    private fun initializeViews() {
        rvChapters = binding.rvChapters

        moduleAdapter = ModuleItemAdapter(this)
        rvChapters.layoutManager = LinearLayoutManager(requireContext())
        rvChapters.adapter = moduleAdapter
        rvChapters.addItemDecoration(
            androidx.recyclerview.widget.DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        )
    }

    private fun afterInitialize() {
        loadingDialog.show("Initializing...")

        moduleViewModel.modules.observe(viewLifecycleOwner) { modules ->
            loadingDialog.dismiss()
            if(!modules.isNullOrEmpty()) {
                moduleAdapter.setModules(modules)
            }
        }

        moduleViewModel.getAllModulesByCategory(ModuleContentType.PDF, pdfCategory!!)
    }

    private fun initialize() {
        loadingDialog = MyLoadingDialog(requireContext())
    }

    private fun initializeViewModel() {
        val moduleDao = AppDatabaseProvider.getDatabase(requireActivity()).moduleDao()
        val moduleRepository = ModuleRepositoryImpl(moduleDao)
        val moduleViewModelFactory = ModuleViewModelFactory(moduleRepository)
        moduleViewModel = ViewModelProvider(this, moduleViewModelFactory)[ModuleViewModel::class]

        searchViewModel = ViewModelProvider(requireActivity())[PdfSearchViewModel::class.java]

        searchViewModel.searchQuery.observe(viewLifecycleOwner) { query ->
            if (!query.isEmpty()) {
                loadingDialog.show("Searching...")
                moduleViewModel.searchModule(ModuleContentType.PDF, pdfCategory!!, query)
            } else {
                // If query is cleared, show original chapter list
                moduleViewModel.getAllModulesByCategory(ModuleContentType.PDF, pdfCategory!!)
            }
        }
    }

    override fun onPdfClick(module: LearningModule) {
        (requireActivity() as? PdfViewerActivity)?.setTabPosition(0, module.contentUrl, module.category)
    }

    companion object {
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(pdfCategory: String) =
            PdfChaptersFragment().apply {
                arguments = Bundle().apply {
                    putString(PDF_CATEGORY, pdfCategory)
                }
            }
    }
}