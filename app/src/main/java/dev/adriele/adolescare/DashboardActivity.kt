package dev.adriele.adolescare

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import dev.adriele.adolescare.databinding.ActivityDashboardBinding
import dev.adriele.adolescare.fragments.ChatBotFragment

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var bottomNavView: BottomNavigationView
    private lateinit var btnMenu: ImageView

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

        userId = intent.getStringExtra("userId")
        userName = intent.getStringExtra("userName")
        initViews(savedInstanceState)
        afterInit()
    }

    private fun afterInit() {
        btnMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

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
                R.id.action_home -> {}
                R.id.action_module -> {}
                R.id.action_video -> {}
                R.id.action_chat_bot -> loadFragment(ChatBotFragment.newInstance(userId ?: "", userName ?: ""))
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
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.content_frame, fragment)
            .commit()
    }
}