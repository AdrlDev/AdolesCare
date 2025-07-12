package dev.adriele.adolescare.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.transition.TransitionManager
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.android.material.transition.platform.MaterialFade
import com.google.android.material.transition.platform.MaterialSharedAxis
import com.google.gson.Gson
import dev.adriele.adolescare.BaseActivity
import dev.adriele.adolescare.R
import dev.adriele.adolescare.authentication.LoginActivity
import dev.adriele.adolescare.database.AppDatabaseProvider
import dev.adriele.adolescare.database.repositories.implementation.ModuleRepositoryImpl
import dev.adriele.adolescare.databinding.ActivityDashboardBinding
import dev.adriele.adolescare.dialogs.MyLoadingDialog
import dev.adriele.adolescare.fragments.ChatBotFragment
import dev.adriele.adolescare.fragments.HomeFragment
import dev.adriele.adolescare.fragments.MenstrualTrackerFragment
import dev.adriele.adolescare.fragments.ModulesFragment
import dev.adriele.adolescare.fragments.VideosFragment
import dev.adriele.adolescare.helpers.Utility
import dev.adriele.adolescare.viewmodel.ModuleViewModel
import dev.adriele.adolescare.viewmodel.factory.ModuleViewModelFactory
import dev.adriele.language.LanguageManager

class DashboardActivity : BaseActivity() {
    private lateinit var binding: ActivityDashboardBinding

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var bottomNavView: BottomNavigationView
    private lateinit var btnMenu: ImageView

    private lateinit var moduleViewModel: ModuleViewModel

    private var userId: String? = null
    private var userName: String? = null
    private var isHTUDone: Boolean = false
    private var step = 0

    private lateinit var loading: MyLoadingDialog

    private lateinit var settingsLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedAxis = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        window.enterTransition = sharedAxis
        window.exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)

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
        ViewCompat.setOnApplyWindowInsetsListener(binding.containerHtu) { v, insets ->
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

        settingsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val languageChanged = result.data?.getBooleanExtra("LANGUAGE_CHANGED", false) ?: false
                if (languageChanged) {
                    recreate() // This will trigger BaseActivity's attachBaseContext again
                }
            }
        }

        initViews(savedInstanceState)
        initializeViewModel()
        afterInit()

        binding.root.isClickable = isHTUDone
        binding.root.isEnabled = isHTUDone

        binding.containerHtu.visibility = View.VISIBLE

        setUpHTU()
    }

    private fun setUpHTU() {
        binding.btnSkipHtu.setOnClickListener {
            finishHtu()
        }

        binding.containerHtu.setOnClickListener {
            when(step) {
                0 -> hideHTUModule()
                1 -> hideHTUCalendar()
                2 -> hideHTUVideo()
                3 -> hideHTUChat()
                4 -> hideHTUMenu()
                5 -> hideHTULogPeriod()
            }
            step++
        }
    }

    private fun hideHTUModule() {
        val materialFade = MaterialFade().apply {
            duration = 84L
        }
        TransitionManager.beginDelayedTransition(binding.containerHtu, materialFade)
        binding.tvModuleHtu.visibility = View.GONE
        binding.viewModule.visibility = View.GONE
        showHTUCalendar()
    }

    private fun hideHTUCalendar() {
        val materialFade = MaterialFade().apply {
            duration = 84L
        }
        TransitionManager.beginDelayedTransition(binding.containerHtu, materialFade)
        binding.tvCalendarHtu.visibility = View.GONE
        binding.viewCalendar.visibility = View.GONE
        showHTUVideo()
    }

    private fun hideHTUVideo() {
        val materialFade = MaterialFade().apply {
            duration = 84L
        }
        TransitionManager.beginDelayedTransition(binding.containerHtu, materialFade)
        binding.tvVideoHtu.visibility = View.GONE
        binding.viewVideo.visibility = View.GONE
        showHTUChat()
    }

    private fun hideHTUChat() {
        val materialFade = MaterialFade().apply {
            duration = 84L
        }
        TransitionManager.beginDelayedTransition(binding.containerHtu, materialFade)
        binding.tvChatHtu.visibility = View.GONE
        binding.viewChat.visibility = View.GONE
        showHTUMenu()
    }

    private fun hideHTUMenu() {
        val materialFade = MaterialFade().apply {
            duration = 84L
        }
        TransitionManager.beginDelayedTransition(binding.containerHtu, materialFade)
        binding.tvMenuHtu.visibility = View.GONE
        binding.viewMenu.visibility = View.GONE
        showHTULogPeriod()
    }

    private fun hideHTULogPeriod() {
        val materialFade = MaterialFade().apply {
            duration = 84L
        }
        TransitionManager.beginDelayedTransition(binding.containerHtu, materialFade)
        binding.tvLogPeriodHtu.visibility = View.GONE
        binding.viewLogPeriod.visibility = View.GONE
        finishHtu()
    }

    private fun finishHtu() {
        val materialFade = MaterialFade().apply {
            duration = 84L
        }
        TransitionManager.beginDelayedTransition(binding.containerHtu, materialFade)
        binding.containerHtu.visibility = View.GONE
        isHTUDone = true
    }

    private fun showHTUCalendar() {
        val materialFade = MaterialFade().apply {
            duration = 150L
        }
        TransitionManager.beginDelayedTransition(binding.containerHtu, materialFade)
        binding.tvCalendarHtu.visibility = View.VISIBLE
        binding.viewCalendar.visibility = View.VISIBLE
    }

    private fun showHTUVideo() {
        val materialFade = MaterialFade().apply {
            duration = 150L
        }
        TransitionManager.beginDelayedTransition(binding.containerHtu, materialFade)
        binding.tvVideoHtu.visibility = View.VISIBLE
        binding.viewVideo.visibility = View.VISIBLE
    }

    private fun showHTUChat() {
        val materialFade = MaterialFade().apply {
            duration = 150L
        }
        TransitionManager.beginDelayedTransition(binding.containerHtu, materialFade)
        binding.tvChatHtu.visibility = View.VISIBLE
        binding.viewChat.visibility = View.VISIBLE
    }

    private fun showHTUMenu() {
        val materialFade = MaterialFade().apply {
            duration = 150L
        }
        TransitionManager.beginDelayedTransition(binding.containerHtu, materialFade)
        binding.tvMenuHtu.visibility = View.VISIBLE
        binding.viewMenu.visibility = View.VISIBLE
    }

    private fun showHTULogPeriod() {
        val materialFade = MaterialFade().apply {
            duration = 150L
        }
        TransitionManager.beginDelayedTransition(binding.containerHtu, materialFade)
        binding.tvLogPeriodHtu.visibility = View.VISIBLE
        binding.viewLogPeriod.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        if (LanguageManager.consumeLanguageChanged(this)) {
            recreate()
        }
    }

    private fun showExitConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Exit App")
            .setMessage("Are you sure you want to exit?")
            .setPositiveButton(getString(dev.adriele.language.R.string.yes)) { _, _ ->
                finishAffinity() // Closes the app
            }
            .setNegativeButton(getString(dev.adriele.language.R.string.cancel), null)
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
        val tvSubMessage = headerView.findViewById<TextView>(R.id.tv_sub_message)
        usernameTextView.text = "Hi, $userName."
        tvSubMessage.text = getString(dev.adriele.language.R.string.welcome_message) + " ${getString(
            dev.adriele.language.R.string.app_name)}"

        // Drawer item selection
        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_account -> gotoAccount()
                R.id.nav_setting -> gotoSettings()
                R.id.nav_notification -> {}
                R.id.nav_about -> {}
                R.id.nav_logout -> logout()
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

    private fun gotoSettings() {
        val intent = Intent(this, SettingsActivity::class.java).apply {
            putExtra("userId", userId)
        }
        settingsLauncher.launch(intent)
    }

    private fun gotoAccount() {
        val intent = Intent(this, AccountActivity::class.java).apply {
            putExtra("userId", userId)
        }
        startActivity(intent)
    }

    private fun logout() {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(dev.adriele.language.R.string.menu_logout))
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton(getString(dev.adriele.language.R.string.yes)) { _, _ ->
                loading.show("Please wait...")

                Handler(Looper.getMainLooper()).postDelayed({
                    loading.dismiss()
                    val intent = Intent(this, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                    startActivity(intent)
                }, 1500)
            }
            .setNegativeButton(getString(dev.adriele.language.R.string.cancel), null)
            .show()
    }

    private fun initViews(savedInstanceState: Bundle?) {
        drawerLayout = binding.main
        navView = binding.navigationView
        bottomNavView = binding.bottomNav
        btnMenu = binding.menuIcon

        loading = MyLoadingDialog(this)

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