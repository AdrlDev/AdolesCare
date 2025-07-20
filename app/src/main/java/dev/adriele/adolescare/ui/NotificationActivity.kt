package dev.adriele.adolescare.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import dev.adriele.adolescare.adapter.ReminderAdapter
import dev.adriele.adolescare.database.AppDatabaseProvider
import dev.adriele.adolescare.database.repositories.implementation.ReminderRepositoryImpl
import dev.adriele.adolescare.databinding.ActivityNotificationBinding
import dev.adriele.adolescare.viewmodel.ReminderViewModel
import dev.adriele.adolescare.viewmodel.factory.ReminderViewModelFactory

class NotificationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNotificationBinding

    private lateinit var viewModel: ReminderViewModel

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
    }

    private fun afterInitialization() {
        val layoutManager = LinearLayoutManager(this)
        binding.rvNotification.layoutManager = layoutManager
        // Add divider between items
        val dividerItemDecoration = DividerItemDecoration(this, layoutManager.orientation)
        binding.rvNotification.addItemDecoration(dividerItemDecoration)

        viewModel.reminder.observe(this) { reminders ->
            val adapter = ReminderAdapter(reminders)
            binding.rvNotification.adapter = adapter
        }

        viewModel.getAllReminder(userId ?: "")
    }

    private fun extractIntentExtras() {
        userId = intent.getStringExtra("userId")
    }

    private fun initializeViewModel() {
        val reminderDao = AppDatabaseProvider.getDatabase(this).reminderDao()
        val reminderRepository = ReminderRepositoryImpl(reminderDao)
        val reminderViewModelFactory = ReminderViewModelFactory(reminderRepository)
        viewModel = ViewModelProvider(this, reminderViewModelFactory)[ReminderViewModel::class.java]
    }
}