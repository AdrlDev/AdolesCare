package dev.adriele.adolescare.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import dev.adriele.adolescare.adapter.RecentReadWatchAdapter
import dev.adriele.adolescare.adapter.ReminderAdapter
import dev.adriele.adolescare.database.AppDatabaseProvider
import dev.adriele.adolescare.database.entities.RecentReadAndWatch
import dev.adriele.adolescare.database.entities.Reminder
import dev.adriele.adolescare.database.repositories.implementation.ArchiveRepositoryImpl
import dev.adriele.adolescare.database.repositories.implementation.ModuleRepositoryImpl
import dev.adriele.adolescare.databinding.ActivityArchiveBinding
import dev.adriele.adolescare.helpers.contracts.IRecentReadAndWatch
import dev.adriele.adolescare.helpers.enums.ModuleContentType
import dev.adriele.adolescare.viewmodel.ArchiveViewModel
import dev.adriele.adolescare.viewmodel.ModuleViewModel
import dev.adriele.adolescare.viewmodel.factory.ArchiveViewModelFactory
import dev.adriele.adolescare.viewmodel.factory.ModuleViewModelFactory

class ArchiveActivity : AppCompatActivity(), IRecentReadAndWatch {
    private lateinit var binding: ActivityArchiveBinding

    private lateinit var archiveViewModel: ArchiveViewModel
    private lateinit var moduleViewModel: ModuleViewModel

    private lateinit var archiveAdapter: RecentReadWatchAdapter

    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityArchiveBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        extractIntentExtras()
        initializeViewModel()
        afterInit()
        observeArchive()

        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun observeArchive() {
        archiveViewModel.archiveReminder.observe(this) { archiveReminders ->
            val reminders = mutableListOf<Reminder>()

            archiveReminders.map { archiveReminder ->
                val reminder = Reminder(
                    archiveReminder.id,
                    archiveReminder.userId,
                    archiveReminder.title,
                    archiveReminder.message,
                    archiveReminder.dateTime,
                    archiveReminder.type
                )
                reminders.add(reminder)
            }
            val adapter = ReminderAdapter(reminders, true, null)
            binding.rvArchiveReminder.adapter = adapter
        }

        archiveViewModel.archiveReadNWatch.observe(this) { archiveRecentReadAndWatches ->
            val recentReadNWatches = archiveRecentReadAndWatches.map {
                RecentReadAndWatch(
                    it.id,
                    it.moduleId,
                    it.timestamp
                )
            }
            archiveAdapter.updateList(recentReadNWatches)
        }
    }

    private fun afterInit() {
        archiveViewModel.getArchiveReminder(userId ?: "")
        archiveViewModel.getArchiveReadNWatch()

        val layoutManager = LinearLayoutManager(this)
        binding.rvArchiveReminder.layoutManager = layoutManager

        // Add divider between items
        val dividerItemDecoration = DividerItemDecoration(this, layoutManager.orientation)
        binding.rvArchiveReminder.addItemDecoration(dividerItemDecoration)

        binding.rvArchiveRecent.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        archiveAdapter = RecentReadWatchAdapter(mutableListOf(), moduleViewModel, this)
        binding.rvArchiveRecent.adapter = archiveAdapter
    }

    private fun extractIntentExtras() {
        userId = intent.getStringExtra("userId")
    }

    private fun initializeViewModel() {
        val archiveDao = AppDatabaseProvider.getDatabase(this).archiveDao()
        val archiveRepository = ArchiveRepositoryImpl(archiveDao)
        val archiveViewModelFactory = ArchiveViewModelFactory(archiveRepository)
        archiveViewModel = ViewModelProvider(this, archiveViewModelFactory)[ArchiveViewModel::class.java]

        val moduleDao = AppDatabaseProvider.getDatabase(this).moduleDao()
        val moduleRepository = ModuleRepositoryImpl(moduleDao)
        val moduleViewModelFactory = ModuleViewModelFactory(moduleRepository)
        moduleViewModel = ViewModelProvider(this, moduleViewModelFactory)[ModuleViewModel::class]
    }

    override fun onRecentClick(
        moduleType: ModuleContentType,
        recent: RecentReadAndWatch,
        path: String
    ) {
        when(moduleType) {
            ModuleContentType.PDF -> {
                val intent = Intent(this, PdfViewerActivity::class.java).apply {
                    putExtra("module_id", recent.moduleId)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                startActivity(intent)
            }
            ModuleContentType.VIDEO -> {
                val intent = Intent(this, VideoPlayerActivity::class.java)
                intent.putExtra("path", path)
                startActivity(intent)
            }
        }
    }
}