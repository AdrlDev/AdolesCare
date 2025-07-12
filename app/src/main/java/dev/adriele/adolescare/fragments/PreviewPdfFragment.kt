package dev.adriele.adolescare.fragments

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialFadeThrough
import dev.adriele.adolescare.databinding.FragmentPreviewPdfBinding
import dev.adriele.adolescare.dialogs.MyLoadingDialog
import dev.adriele.adolescare.helpers.Utility
import dev.adriele.adolescare.helpers.Utility.generateHighlightedBitmap
import dev.adriele.adolescare.viewmodel.PdfSearchViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val PDF_URI = "pdfUri"

class PreviewPdfFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var pdfUri: String? = null

    private var _binding: FragmentPreviewPdfBinding? = null
    private val binding get() = _binding!!

    private lateinit var loadingDialog: MyLoadingDialog
    private lateinit var searchViewModel: PdfSearchViewModel
    private var currentHighlightedPage = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            pdfUri = it.getString(PDF_URI)
        }
        enterTransition = MaterialFadeThrough()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentPreviewPdfBinding.inflate(layoutInflater, container, false)

        loadingDialog = MyLoadingDialog(requireContext())

        initializeViewModel()
        initializePdf()

        return binding.root
    }

    private fun initializeViewModel() {
        searchViewModel = ViewModelProvider(requireActivity())[PdfSearchViewModel::class.java]

        searchViewModel.searchMatches.observe(viewLifecycleOwner) { pages ->
            if (pages.isNotEmpty()) {
                val pageIndex = pages.first()

                val file = Utility.getFileFromUri(requireContext(), pdfUri!!.toUri())

                lifecycleScope.launch {
                    val highlightedBitmap = generateHighlightedBitmap(
                        file = file,
                        pageIndex = pageIndex,
                        query = searchViewModel.searchQuery.value ?: ""
                    )

                    withContext(Dispatchers.Main) {
                        binding.highlightOverlay.setImageBitmap(highlightedBitmap)
                        binding.highlightOverlay.visibility = View.VISIBLE
                        binding.pdfViewer.jumpTo(pageIndex, true)
                        currentHighlightedPage = pageIndex
                    }
                }
            }
        }

        searchViewModel.searchQuery.observe(viewLifecycleOwner) { query ->
            if (query.isEmpty()) {
                binding.highlightOverlay.setImageDrawable(null)
                binding.highlightOverlay.visibility = View.GONE
            }
        }
    }

    private fun initializePdf() {
        if (pdfUri != null) {
            loadingDialog.show("Loading PDF...")
            initializePdf(pdfUri?.toUri()!!)
        }
    }

    private fun initializePdf(uri: Uri) {
        try {
            binding.pdfViewer.fromUri(uri) // Place this in assets folder
                .enableSwipe(true)
                .swipeHorizontal(false)
                .enableDoubletap(true)
                .defaultPage(0)
                .enableAntialiasing(true)
                .enableAnnotationRendering(false)
                .pageFitPolicy(FitPolicy.WIDTH)
                .fitEachPage(false) // fit each page to the view, else smaller pages are scaled relative to largest page.
                .pageSnap(false) // snap pages to screen boundaries
                .pageFling(false) // make a fling change only a single page like ViewPager
                .nightMode(isNightMode()) // toggle night mode
                .onLoad {
                    // PDF successfully loaded, hide loading
                    loadingDialog.dismiss()
                }
                .onPageChange { page, pageCount ->
                    if (page != currentHighlightedPage) {
                        binding.highlightOverlay.visibility = View.GONE
                    }
                }
                .onError {
                    loadingDialog.dismiss()
                    Snackbar.make(binding.root, "Failed to load PDF", Snackbar.LENGTH_LONG).show()
                }
                .load()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun isNightMode(): Boolean {
        val currentNightMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
    }

    companion object {
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(pdfUri: String) =
            PreviewPdfFragment().apply {
                arguments = Bundle().apply {
                    putString(PDF_URI, pdfUri)
                }
            }
    }
}