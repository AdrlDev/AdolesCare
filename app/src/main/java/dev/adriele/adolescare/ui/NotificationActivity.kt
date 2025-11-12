package dev.adriele.adolescare.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import dev.adriele.adolescare.adapter.ReminderAdapter
import dev.adriele.adolescare.database.AppDatabaseProvider
import dev.adriele.adolescare.database.entities.ArchiveReminder
import dev.adriele.adolescare.database.entities.Reminder
import dev.adriele.adolescare.database.repositories.implementation.ArchiveRepositoryImpl
import dev.adriele.adolescare.database.repositories.implementation.ReminderRepositoryImpl
import dev.adriele.adolescare.databinding.ActivityNotificationBinding
import dev.adriele.adolescare.dialogs.MyLoadingDialog
import dev.adriele.adolescare.helpers.contracts.IReminder
import dev.adriele.adolescare.helpers.enums.ClickFunction
import dev.adriele.adolescare.model.InsertState
import dev.adriele.adolescare.viewmodel.ArchiveViewModel
import dev.adriele.adolescare.viewmodel.ReminderViewModel
import dev.adriele.adolescare.viewmodel.factory.ArchiveViewModelFactory
import dev.adriele.adolescare.viewmodel.factory.ReminderViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationActivity : AppCompatActivity(), IReminder {
    private lateinit var binding: ActivityNotificationBinding

    private lateinit var viewModel: ReminderViewModel
    private lateinit var archiveViewModel: ArchiveViewModel
    private lateinit var loadingDialog: MyLoadingDialog

    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        extractIntentExtras()
        initializeViewModel()
        afterInitialization()
        observeState()

        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun observeState() {
        archiveViewModel.insertState.observe(this) { state ->
            when (state) {
                is InsertState.Loading -> showLoadingSpinner()
                is InsertState.Success -> {
                    hideLoading()
                }
                is InsertState.Error -> {
                    hideLoading()
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_SHORT).show()
                }
                is InsertState.DataToArchive -> {
                    state.reminder?.let { archiveReminder ->
                        viewModel.deleteReminder(archiveReminder.userId, archiveReminder.id)
                    }
                }
                InsertState.Idle -> Unit
            }
        }
    }

    private fun hideLoading() {
        loadingDialog.dismiss()
    }

    private fun showLoadingSpinner() {
        loadingDialog.show("archiving reminder, please wait...")
    }

    private fun afterInitialization() {
        val layoutManager = LinearLayoutManager(this)
        binding.rvNotification.layoutManager = layoutManager
        // Add divider between items
        val dividerItemDecoration = DividerItemDecoration(this, layoutManager.orientation)
        binding.rvNotification.addItemDecoration(dividerItemDecoration)

        viewModel.reminder.observe(this) { reminders ->
            // Auto-archive reminders older than 24 hours
            val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
            val now = Date()

            // Show only reminders that are not archived (less than 24 hrs)
            val recentReminders = reminders.filter {
                try {
                    val reminderDate = sdf.parse(it.createdAt)
                    reminderDate != null && now.time - reminderDate.time < (24 * 60 * 60 * 1000)
                } catch (_: Exception) {
                    true
                }
            }

            // Sort by date descending (latest first)
            val sortedReminders = recentReminders.sortedByDescending {
                try {
                    sdf.parse(it.createdAt)?.time ?: 0L
                } catch (_: Exception) {
                    0L
                }
            }

            val adapter = ReminderAdapter(sortedReminders, false, this)
            binding.rvNotification.adapter = adapter
        }

        viewModel.getAllReminder(userId ?: "")
    }

    private fun extractIntentExtras() {
        userId = intent.getStringExtra("userId")
    }

    private fun initializeViewModel() {
        loadingDialog = MyLoadingDialog(this)

        val reminderDao = AppDatabaseProvider.getDatabase(this).reminderDao()
        val reminderRepository = ReminderRepositoryImpl(reminderDao)
        val reminderViewModelFactory = ReminderViewModelFactory(reminderRepository)
        viewModel = ViewModelProvider(this, reminderViewModelFactory)[ReminderViewModel::class.java]

        val archiveDao = AppDatabaseProvider.getDatabase(this).archiveDao()
        val archiveRepository = ArchiveRepositoryImpl(archiveDao)
        val archiveViewModelFactory = ArchiveViewModelFactory(archiveRepository)
        archiveViewModel = ViewModelProvider(this, archiveViewModelFactory)[ArchiveViewModel::class.java]
    }

    override fun onClickReminder(reminder: Reminder, function: ClickFunction) {
        when(function) {
            ClickFunction.ARCHIVE -> {
                val archive = ArchiveReminder(
                    id = reminder.id,
                    userId = reminder.userId,
                    title = reminder.title,
                    message = reminder.message,
                    dateTime = reminder.dateTime,
                    type = reminder.type
                )
                archiveViewModel.insertArchiveReminder(archive)
            }
            ClickFunction.DELETE -> {
                viewModel.deleteReminder(reminder.userId, reminder.id)
            }
        }
    }
}