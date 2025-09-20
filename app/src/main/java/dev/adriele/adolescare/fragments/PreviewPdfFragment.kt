package dev.adriele.adolescare.fragments

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialFadeThrough
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dev.adriele.adolescare.databinding.FragmentPreviewPdfBinding
import dev.adriele.adolescare.dialogs.MyLoadingDialog
import dev.adriele.adolescare.helpers.contracts.OnUserInteractionListener
import dev.adriele.adolescare.viewmodel.PdfSearchViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import dev.adriele.adolescare.helpers.Utility.toBitmap

private const val PDF_URI = "pdfUri"

class PreviewPdfFragment : Fragment() {

    private var pdfUri: String? = null
    private var _binding: FragmentPreviewPdfBinding? = null
    private val binding get() = _binding!!

    private lateinit var loadingDialog: MyLoadingDialog
    private lateinit var searchViewModel: PdfSearchViewModel
    private var interactionListener: OnUserInteractionListener? = null

    private val pageTextMap = mutableMapOf<Int, List<Text.TextBlock>>()
    private var currentPage = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { pdfUri = it.getString(PDF_URI) }
        enterTransition = MaterialFadeThrough()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPreviewPdfBinding.inflate(inflater, container, false)
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
                val query = searchViewModel.searchQuery.value ?: ""

                lifecycleScope.launch {
                    val (bitmap, blocks) = getPageTextBitmap(pageIndex)
                    pageTextMap[pageIndex] = blocks

                    val highlighted = highlightQuery(bitmap, blocks, query)

                    withContext(Dispatchers.Main) {
                        binding.highlightOverlay.setImageBitmap(highlighted)
                        binding.highlightOverlay.visibility = View.VISIBLE
                        binding.pdfViewer.jumpTo(pageIndex, true)
                        currentPage = pageIndex
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
        pdfUri?.let { uri ->
            loadingDialog.show("Loading PDF...")
            binding.pdfViewer.fromUri(uri.toUri())
                .enableSwipe(true)
                .swipeHorizontal(false)
                .enableDoubletap(true)
                .defaultPage(0)
                .enableAntialiasing(true)
                .pageFitPolicy(FitPolicy.WIDTH)
                .onLoad { loadingDialog.dismiss() }
                .onPageChange { page, _ ->
                    currentPage = page
                    binding.highlightOverlay.visibility = View.GONE
                }
                .onTap {
                    interactionListener?.onUserInteraction()
                    true
                }
                .load()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun getPageTextBitmap(pageIndex: Int): Pair<Bitmap, List<Text.TextBlock>> {
        return withContext(Dispatchers.IO) {
            val bitmap = binding.pdfViewer.toBitmap(pageIndex)

            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val inputImage = InputImage.fromBitmap(bitmap, 0)

            val visionText = suspendCancellableCoroutine<Text> { cont ->
                recognizer.process(inputImage)
                    .addOnSuccessListener { cont.resume(it, null) }
                    .addOnFailureListener { cont.resumeWith(Result.failure(it)) }
            }

            bitmap to visionText.textBlocks
        }
    }

    private fun highlightQuery(bitmap: Bitmap, textBlocks: List<Text.TextBlock>, query: String): Bitmap {
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            style = Paint.Style.FILL
            color = Color.YELLOW
            alpha = 120
        }

        textBlocks.forEach { block ->
            if (block.text.contains(query, ignoreCase = true)) {
                block.boundingBox?.let { canvas.drawRect(it, paint) }
            }
        }
        return bitmap
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Now binding is ready
        binding.highlightOverlay.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val x = event.x.toInt()
                val y = event.y.toInt()
                val blocks = pageTextMap[currentPage] ?: return@setOnTouchListener false

                blocks.forEach { block ->
                    block.boundingBox?.let { box ->
                        if (box.contains(x, y)) {
                            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("PDF Text", block.text)
                            clipboard.setPrimaryClip(clip)
                            Snackbar.make(binding.root, "Copied: ${block.text}", Snackbar.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            true
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnUserInteractionListener) interactionListener = context
    }

    override fun onDetach() {
        super.onDetach()
        interactionListener = null
    }

    companion object {
        @JvmStatic
        fun newInstance(pdfUri: String, listener: OnUserInteractionListener) =
            PreviewPdfFragment().apply {
                arguments = Bundle().apply { putString(PDF_URI, pdfUri) }
                interactionListener = listener
            }
    }
}
