package dev.adriele.adolescare.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import dev.adriele.adolescare.R
import dev.adriele.adolescare.database.AppDatabaseProvider
import dev.adriele.adolescare.database.repositories.implementation.ModuleRepositoryImpl
import dev.adriele.adolescare.databinding.ActivityDashboardBinding
import dev.adriele.adolescare.fragments.ChatBotFragment
import dev.adriele.adolescare.fragments.HomeFragment
import dev.adriele.adolescare.fragments.MenstrualTrackerFragment
import dev.adriele.adolescare.fragments.ModulesFragment
import dev.adriele.adolescare.fragments.VideosFragment
import dev.adriele.adolescare.helpers.Utility
import dev.adriele.adolescare.viewmodel.ModuleViewModel
import dev.adriele.adolescare.viewmodel.factory.ModuleViewModelFactory

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var bottomNavView: BottomNavigationView
    private lateinit var btnMenu: ImageView

    private lateinit var moduleViewModel: ModuleViewModel

    private var userId: String? = null
    private var userName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.navigationView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    showExitConfirmationDialog() // Fully exit the app
                }
            }
        })

        userId = intent.getStringExtra("userId")
        userName = intent.getStringExtra("userName")
        initViews(savedInstanceState)
        initializeViewModel()
        afterInit()
    }

    private fun showExitConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Exit App")
            .setMessage("Are you sure you want to exit?")
            .setPositiveButton("Yes") { _, _ ->
                finishAffinity() // Closes the app
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun initializeViewModel() {
        val moduleDao = AppDatabaseProvider.getDatabase(this).moduleDao()
        val moduleRepository = ModuleRepositoryImpl(moduleDao)
        val moduleViewModelFactory = ModuleViewModelFactory(moduleRepository)
        moduleViewModel = ViewModelProvider(this, moduleViewModelFactory)[ModuleViewModel::class]
    }

    @SuppressLint("SetTextI18n")
    private fun afterInit() {
        insertModules()

        btnMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Set username in nav header
        val headerView = navView.getHeaderView(0)
        val usernameTextView = headerView.findViewById<TextView>(R.id.tv_username)
        usernameTextView.text = "Hi, $userName!"

        // Drawer item selection
        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_account -> {}
                R.id.nav_setting -> {}
                R.id.nav_notification -> {}
                R.id.nav_about -> {}
                R.id.nav_logout -> {}
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // Bottom nav item click
        bottomNavView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.action_home -> loadFragment(HomeFragment.Companion.newInstance(userId ?: "", userName ?: ""))
                R.id.action_module -> loadFragment(ModulesFragment.Companion.newInstance(userId ?: ""))
                R.id.action_calendar -> loadFragment(MenstrualTrackerFragment.Companion.newInstance(userId ?: "", userName ?: ""))
                R.id.action_video -> loadFragment(VideosFragment.Companion.newInstance(userId ?: ""))
                R.id.action_chat_bot -> loadFragment(ChatBotFragment.Companion.newInstance(userId ?: "", userName ?: ""))
            }
            true
        }
    }

    private fun initViews(savedInstanceState: Bundle?) {
        drawerLayout = binding.main
        navView = binding.navigationView
        bottomNavView = binding.bottomNav
        btnMenu = binding.menuIcon

        // Set default screen
        if (savedInstanceState == null) {
            bottomNavView.selectedItemId = R.id.action_home
            loadFragment(HomeFragment.Companion.newInstance(userId ?: "", userName ?: ""))
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.content_frame, fragment)
            .commit()
    }

    private fun insertModules() {
        val chapters = Utility.loadLearningModules(this).toMutableList()
        val videos = Utility.loadLearningVideos(this).toMutableList()

        Log.e("VIDEOS_MODULE", Gson().toJson(videos))

        for(video in videos) {
            chapters.add(video)
        }

        moduleViewModel.insertModules(chapters)
    }
}