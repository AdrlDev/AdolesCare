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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.adriele.adolescare.adapter.PdfPageAdapter
import dev.adriele.adolescare.databinding.ActivityPdfViewerBinding
import androidx.core.net.toUri
import com.google.android.material.snackbar.Snackbar
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import dev.adriele.adolescare.Utility.buildTextIndex
import dev.adriele.adolescare.Utility.findPagesWithText
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
    private lateinit var adapter: PdfPageAdapter
    private lateinit var recyclerView: RecyclerView

    private var searchJob: Job? = null
    private val searchScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

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

        initializeViews()

        val pdfUri = intent.getStringExtra("pdf_uri")?.toUri()
        if (pdfUri != null) {
            initializeIndex(pdfUri)
            initializePdf(pdfUri)

            initializeSearch()
        } else {
            finish() // No URI, close the activity
        }
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
                            binding.pdfRecyclerView.scrollToPosition(pages.first())
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
            parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")
            parcelFileDescriptor?.let {
                pdfRenderer = PdfRenderer(it)
                adapter = PdfPageAdapter(pdfRenderer!!)
                binding.pdfRecyclerView.layoutManager = LinearLayoutManager(this)
                binding.pdfRecyclerView.adapter = adapter
            }
        } catch (e: Exception) {
            e.printStackTrace()
            finish()
        }
    }

    private fun initializeViews() {
        recyclerView = binding.pdfRecyclerView
    }

    override fun onDestroy() {
        super.onDestroy()
        pdfRenderer?.close()
        parcelFileDescriptor?.close()
        searchScope.cancel() // Cancel any pending coroutine work
    }
}