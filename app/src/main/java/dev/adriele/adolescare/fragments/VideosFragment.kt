package dev.adriele.adolescare.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialFadeThrough
import dev.adriele.adolescare.ui.VideoPlayerActivity
import dev.adriele.adolescare.helpers.enums.ModuleContentType
import dev.adriele.adolescare.adapter.VideoThumbnailAdapter
import dev.adriele.adolescare.database.AppDatabaseProvider
import dev.adriele.adolescare.database.entities.LearningModule
import dev.adriele.adolescare.database.entities.RecentReadAndWatch
import dev.adriele.adolescare.database.repositories.implementation.ModuleRepositoryImpl
import dev.adriele.adolescare.database.repositories.implementation.RecentReadWatchRepositoryImpl
import dev.adriele.adolescare.databinding.FragmentVideosBinding
import dev.adriele.adolescare.dialogs.MyLoadingDialog
import dev.adriele.adolescare.helpers.contracts.IModules
import dev.adriele.adolescare.viewmodel.ModuleViewModel
import dev.adriele.adolescare.viewmodel.RecentReadWatchViewModel
import dev.adriele.adolescare.viewmodel.factory.ModuleViewModelFactory
import dev.adriele.adolescare.viewmodel.factory.RecentReadWatchViewModelFactory
import kotlinx.coroutines.launch

private const val USER_ID = "userID"

class VideosFragment : Fragment(), IModules.VIDEO {
    // TODO: Rename and change types of parameters
    private var userId: String? = null

    private var _binding: FragmentVideosBinding? = null
    private val binding get() = _binding!!

    private lateinit var moduleViewModel: ModuleViewModel
    private lateinit var recentReadWatchViewModel: RecentReadWatchViewModel

    private var videoList: MutableList<LearningModule> = mutableListOf()
    private var videoPosition: Int = 0

    private lateinit var loadingDialog: MyLoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getString(USER_ID)
        }
        enterTransition = MaterialFadeThrough()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentVideosBinding.inflate(layoutInflater, container, false)

        loadingDialog = MyLoadingDialog(requireContext())

        initializeViewModel()
        initializeVideos()

        return binding.root
    }

    private fun initializeVideos() {
        binding.shimmerLayout.startShimmer()

        moduleViewModel.modules.observe(viewLifecycleOwner) { modules ->
            videoList.clear()
            if (modules != null) {
                videoList.addAll(modules)

                binding.noDataLl.visibility = View.GONE
                binding.rvVideo.visibility = View.VISIBLE

                val adapter = VideoThumbnailAdapter(requireContext(), modules, this)
                binding.rvVideo.layoutManager = LinearLayoutManager(requireContext())
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

        val recentReadWatchDao = AppDatabaseProvider.getDatabase(requireActivity()).recentReadAndWatchDao()
        val recentReadWatchRepository = RecentReadWatchRepositoryImpl(recentReadWatchDao)
        val recentReadWatchViewModelFactory = RecentReadWatchViewModelFactory(recentReadWatchRepository)
        recentReadWatchViewModel = ViewModelProvider(this, recentReadWatchViewModelFactory)[RecentReadWatchViewModel::class]
    }

    override fun onVideoClick(position: Int, path: String) {
        loadingDialog.show("Please wait...")
        videoPosition = position

        lifecycleScope.launch {
            val isRecentExist = recentReadWatchViewModel.isRecentExist(videoList[position].id)

            if(!isRecentExist) {
                recentReadWatchViewModel.addRecent(RecentReadAndWatch(
                    moduleId = videoList[position].id,
                    timestamp = System.currentTimeMillis()
                ))
            }
        }

        moduleViewModel.getModuleByIdLive(videoList[position].id).observe(viewLifecycleOwner) { module ->
            if (module != null) {
                loadingDialog.dismiss()
                val intent = Intent(requireContext(), VideoPlayerActivity::class.java)
                intent.putExtra("path", module.contentUrl)
                startActivity(intent)
            } else {
                Snackbar.make(binding.root, "Module not found", Snackbar.LENGTH_SHORT).show()
            }
        }
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