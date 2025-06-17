package dev.adriele.adolescare.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import dev.adriele.adolescare.ModuleContentType
import dev.adriele.adolescare.PDFModulesCategory
import dev.adriele.adolescare.adapter.PdfModulesAdapter
import dev.adriele.adolescare.database.AppDatabaseProvider
import dev.adriele.adolescare.database.repositories.implementation.ModuleRepositoryImpl
import dev.adriele.adolescare.databinding.FragmentModulesBinding
import dev.adriele.adolescare.model.CategoryModuleGroup
import dev.adriele.adolescare.viewmodel.ModuleViewModel
import dev.adriele.adolescare.viewmodel.factory.ModuleViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val USER_ID = "userID"

class ModulesFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var userId: String? = null

    private var _binding: FragmentModulesBinding? = null
    private val binding get() = _binding!!

    private lateinit var moduleViewModel: ModuleViewModel

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

        initializeViewModel()
        loadModules()

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
        binding.rvModules.layoutManager = LinearLayoutManager(requireContext())
        binding.rvModules.setHasFixedSize(true)
        binding.rvModules.setItemViewCacheSize(20) // optional
        binding.rvModules.setRecycledViewPool(RecyclerView.RecycledViewPool()) // reuse
        binding.rvModules.adapter = PdfModulesAdapter(grouped, viewLifecycleOwner.lifecycleScope)
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