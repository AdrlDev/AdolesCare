package dev.adriele.adolescare

import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import dev.adriele.adolescare.databinding.ActivityPdfViewerBinding
import androidx.core.net.toUri
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.google.android.material.snackbar.Snackbar
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import dev.adriele.adolescare.dialogs.MyLoadingDialog
import dev.adriele.adolescare.helpers.Utility
import dev.adriele.adolescare.helpers.Utility.buildTextIndex
import dev.adriele.adolescare.helpers.Utility.findPagesWithText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PdfViewerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPdfViewerBinding
    private var pdfRenderer: PdfRenderer? = null
    private var parcelFileDescriptor: ParcelFileDescriptor? = null
    private lateinit var pdfView: PDFView

    private var searchJob: Job? = null
    private val searchScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private lateinit var loadingDialog: MyLoadingDialog

    private lateinit var textIndex: Map<Int, String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPdfViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        PDFBoxResourceLoader.init(applicationContext)
        loadingDialog = MyLoadingDialog(this)

        initializeViews()

        val pdfUri = intent.getStringExtra("pdf_uri")?.toUri()
        if (pdfUri != null) {
            loadingDialog.show("Loading PDF...")
            initializeIndex(pdfUri)
            initializePdf(pdfUri)

            initializeSearch()
        } else {
            finish() // No URI, close the activity
        }

        binding.btnBack.setOnClickListener {
            goBack()
        }
    }

    private fun goBack() {
        onBackPressedDispatcher.onBackPressed()
    }

    private fun initializeSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(editable: Editable?) {
                searchJob?.cancel() // Cancel any ongoing job

                searchJob = searchScope.launch {
                    delay(500) // debounce delay in ms
                    val query = editable.toString().trim()
                    if (query.isNotEmpty()) {
                        val pages = findPagesWithText(query, textIndex)
                        if (pages.isNotEmpty()) {
                            pdfView.jumpTo(pages.first(), true)
                        } else {
                            Snackbar.make(binding.main, "Not found", Snackbar.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
    }

    private fun initializeIndex(pdfUri: Uri) {
        val file = Utility.getFileFromUri(this, pdfUri)
        textIndex = buildTextIndex(file)  // index all pages
    }

    private fun initializePdf(uri: Uri) {
        try {
            pdfView.fromUri(uri) // Place this in assets folder
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
                .onError {
                    loadingDialog.dismiss()
                    Snackbar.make(binding.main, "Failed to load PDF", Snackbar.LENGTH_LONG).show()
                    finish()
                }
                .load()
        } catch (e: Exception) {
            e.printStackTrace()
            finish()
        }
    }

    private fun initializeViews() {
        pdfView = binding.pdfView
    }

    private fun isNightMode(): Boolean {
        val currentNightMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
    }

    override fun onDestroy() {
        super.onDestroy()
        pdfRenderer?.close()
        parcelFileDescriptor?.close()
        searchScope.cancel() // Cancel any pending coroutine work
    }
}