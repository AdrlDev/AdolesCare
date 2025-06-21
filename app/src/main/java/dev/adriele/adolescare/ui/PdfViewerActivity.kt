package dev.adriele.adolescare.ui

import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import dev.adriele.adolescare.adapter.PdfViewerPagerAdapter
import dev.adriele.adolescare.database.AppDatabaseProvider
import dev.adriele.adolescare.database.repositories.implementation.ModuleRepositoryImpl
import dev.adriele.adolescare.databinding.ActivityPdfViewerBinding
import dev.adriele.adolescare.dialogs.MyLoadingDialog
import dev.adriele.adolescare.helpers.Utility
import dev.adriele.adolescare.viewmodel.ModuleViewModel
import dev.adriele.adolescare.viewmodel.PdfSearchViewModel
import dev.adriele.adolescare.viewmodel.factory.ModuleViewModelFactory
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
    private lateinit var vp: ViewPager2
    private lateinit var tabLayout: TabLayout

    private var searchJob: Job? = null
    private val searchScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private lateinit var loadingDialog: MyLoadingDialog
    private lateinit var moduleViewModel: ModuleViewModel

    private lateinit var textIndex: Map<Int, String>
    private var moduleCategory: String? = null
    private var moduleUrl: String? = null
    private var moduleId: String? = null

    private var moduleCategoryFromId: String? = null
    private var moduleUrlFromId: String? = null

    private val pdfSearchViewModel by viewModels<PdfSearchViewModel>()

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

        moduleCategory = intent.getStringExtra("module_category")
        moduleUrl = intent.getStringExtra("module_url")
        moduleId = intent.getStringExtra("module_id")

        initializeViews()
        initializeViewModel()
        initializeSearch()

        // ✅ Call viewpager only if data is directly available
        if (moduleId == null && moduleCategory != null && moduleUrl != null) {
            initializeViewPager(moduleUrl!!, moduleCategory!!)
        }

        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun initializeViewModel() {
        val moduleDao = AppDatabaseProvider.getDatabase(this).moduleDao()
        val moduleRepository = ModuleRepositoryImpl(moduleDao)
        val moduleViewModelFactory = ModuleViewModelFactory(moduleRepository)
        moduleViewModel = ViewModelProvider(this, moduleViewModelFactory)[ModuleViewModel::class]

        if (moduleId != null) {
            moduleViewModel.getModuleByIdLive(moduleId!!).observe(this) { module ->
                if (module != null) {
                    moduleCategoryFromId = module.category
                    moduleUrlFromId = module.contentUrl
                    initializeViewPager(moduleUrlFromId!!, moduleCategoryFromId!!) // ✅ defer here if using moduleId
                }
            }
        }
    }

    private fun initializeViewPager(moduleUrl: String, moduleCategory: String) {
        val cachedFile = Utility.copyAssetToCache(this, moduleUrl)

        val uri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            cachedFile!!
        )

        initializeIndex(uri.toString())
        vp.adapter = PdfViewerPagerAdapter(this, uri.toString(), moduleCategory)
        vp.offscreenPageLimit = 2

        TabLayoutMediator(tabLayout, vp) { tab, position ->
            tab.text = if (position == 0) "Preview" else "Chapter"
        }.attach()
    }

    private fun initializeViews() {
        tabLayout = binding.tabLl
        vp = binding.vp
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

                    val isPreviewTab = binding.vp.currentItem == 0

                    if (query.isNotEmpty()) {
                        if(isPreviewTab) {
                            val pages = Utility.findPagesWithText(query, textIndex)
                            if (pages.isNotEmpty()) {
                                pdfSearchViewModel.searchQuery.postValue(query)
                                pdfSearchViewModel.searchMatches.postValue(pages)
                            } else {
                                Snackbar.make(binding.main, "Not found", Snackbar.LENGTH_SHORT).show()
                            }
                        } else {
                            pdfSearchViewModel.searchQuery.postValue(query)
                        }
                    }
                }
            }
        })
    }

    private fun initializeIndex(pdfUri: String) {
        val file = Utility.getFileFromUri(this, pdfUri.toUri())
        val fileName = file.name

        loadingDialog.show("Indexing PDF...")

        lifecycleScope.launch {
            textIndex = Utility.loadOcrCache(this@PdfViewerActivity, fileName)
                ?: Utility.buildTextIndexViaOcr(file).also {
                    Utility.saveOcrCache(this@PdfViewerActivity, fileName, it)
                }

            loadingDialog.dismiss()

            Log.d("PDF_TEXT_INDEX", "Text index size: ${textIndex.size}")
            textIndex.forEach { (page, content) ->
                Log.d("PDF_TEXT_INDEX", "Page $page: ${content.take(100)}")
            }
        }
    }

    fun setTabPosition(position: Int, moduleUrl: String, moduleCategory: String) {
        binding.vp.currentItem = position
        initializeViewPager(moduleUrl, moduleCategory)
    }

    override fun onDestroy() {
        super.onDestroy()
        pdfRenderer?.close()
        parcelFileDescriptor?.close()
        searchScope.cancel() // Cancel any pending coroutine work
    }
}