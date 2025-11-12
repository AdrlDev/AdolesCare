package dev.adriele.adolescare.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import dev.adriele.adolescare.adapter.HistoryAdapter
import dev.adriele.adolescare.database.AppDatabaseProvider
import dev.adriele.adolescare.database.repositories.implementation.ArchiveRepositoryImpl
import dev.adriele.adolescare.databinding.ActivityHistoryBinding
import dev.adriele.adolescare.viewmodel.ArchiveViewModel
import dev.adriele.adolescare.viewmodel.factory.ArchiveViewModelFactory
import java.text.SimpleDateFormat
import java.util.Locale

class HistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistoryBinding

    private lateinit var archiveViewModel: ArchiveViewModel

    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        extractIntentExtras()
        setUpToolbar()
        initializeViewModel()
        afterInitialize()
        observeState()
    }

    private fun observeState() {
        val layoutManager = LinearLayoutManager(this)
        binding.rvHistory.layoutManager = layoutManager
        // Add divider between items
        val dividerItemDecoration = DividerItemDecoration(this, layoutManager.orientation)
        binding.rvHistory.addItemDecoration(dividerItemDecoration)

        archiveViewModel.archiveReminder.observe(this) { reminders ->
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

            // Sort from latest to oldest
            val sortedReminders = reminders.sortedByDescending { reminder ->
                try {
                    sdf.parse(reminder.dateTime)?.time ?: 0L
                } catch (_: Exception) {
                    0L
                }
            }

            val adapter = HistoryAdapter(sortedReminders)
            binding.rvHistory.adapter = adapter
        }
    }

    private fun extractIntentExtras() {
        userId = intent.getStringExtra("userId")
    }

    private fun afterInitialize() {
        archiveViewModel.getArchiveReminder(userId ?: "")
    }

    private fun initializeViewModel() {
        val archiveDao = AppDatabaseProvider.getDatabase(this).archiveDao()
        val archiveRepository = ArchiveRepositoryImpl(archiveDao)
        val archiveViewModelFactory = ArchiveViewModelFactory(archiveRepository)
        archiveViewModel = ViewModelProvider(this, archiveViewModelFactory)[ArchiveViewModel::class.java]
    }

    private fun setUpToolbar() {
        binding.topAppBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}