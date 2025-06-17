package dev.adriele.adolescare.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import dev.adriele.adolescare.ModuleContentType
import dev.adriele.adolescare.adapter.VideoThumbnailAdapter
import dev.adriele.adolescare.database.AppDatabaseProvider
import dev.adriele.adolescare.database.repositories.implementation.ModuleRepositoryImpl
import dev.adriele.adolescare.databinding.FragmentVideosBinding
import dev.adriele.adolescare.viewmodel.ModuleViewModel
import dev.adriele.adolescare.viewmodel.factory.ModuleViewModelFactory

private const val USER_ID = "userID"

class VideosFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var userId: String? = null

    private var _binding: FragmentVideosBinding? = null
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
        _binding = FragmentVideosBinding.inflate(layoutInflater, container, false)

        initializeViewModel()
        initializeVideos()

        return binding.root
    }

    private fun initializeVideos() {
        binding.shimmerLayout.startShimmer()

        moduleViewModel.modules.observe(viewLifecycleOwner) { modules ->
            if (modules != null) {
                binding.noDataLl.visibility = View.GONE
                binding.rvVideo.visibility = View.VISIBLE

                val adapter = VideoThumbnailAdapter(requireContext(), modules)
                binding.rvVideo.layoutManager = GridLayoutManager(requireContext(), 3)
                binding.rvVideo.adapter = adapter
                hideShimmer()
            } else {
                binding.noDataLl.visibility = View.VISIBLE
                binding.rvVideo.visibility = View.GONE
            }
        }

        // Trigger it
        moduleViewModel.getAllModules(ModuleContentType.VIDEO)
    }

    private fun hideShimmer() {
        binding.rvVideo.visibility = View.VISIBLE
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
            VideosFragment().apply {
                arguments = Bundle().apply {
                    putString(USER_ID, userId)
                }
            }
    }
}