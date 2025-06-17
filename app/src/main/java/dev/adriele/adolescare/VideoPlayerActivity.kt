package dev.adriele.adolescare

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import dev.adriele.adolescare.Utility.copyAssetToCache
import dev.adriele.adolescare.databinding.ActivityVideoPlayerBinding

class VideoPlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVideoPlayerBinding

    private lateinit var player: ExoPlayer

    private var path: String? = null
    private var isFullscreen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        Utility.enableFullscreen(window)
        path = intent.getStringExtra("path")
        setupFullscreenToggle()
    }

    override fun onStart() {
        super.onStart()
        initializePlayer(path!!)
    }

    private fun initializePlayer(assetPath: String) {
        player = ExoPlayer.Builder(this).build()
        binding.playerView.player = player

        // Copy asset to temp file because ExoPlayer doesn't support asset:// URIs directly
        val file = copyAssetToCache(this, assetPath)
        val uri = Uri.fromFile(file)

        val mediaItem = MediaItem.fromUri(uri)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = true
    }

    @OptIn(UnstableApi::class)
    private fun setupFullscreenToggle() {
        binding.playerView.setFullscreenButtonState(true)

        binding.playerView.setControllerVisibilityListener(object : PlayerView.ControllerVisibilityListener {
            override fun onVisibilityChanged(visibility: Int) {
                Log.d("VideoPlayerActivity", "Controller visibility changed: $visibility")

                val fullscreenButton = binding.playerView.findViewById<View>(
                    androidx.media3.ui.R.id.exo_fullscreen
                )

                fullscreenButton?.setOnClickListener {
                    toggleFullscreen()
                }
            }
        })
    }

    private fun toggleFullscreen() {
        isFullscreen = !isFullscreen
        requestedOrientation = if (isFullscreen) {
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }

        window.decorView.systemUiVisibility = if (isFullscreen) {
            View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        } else {
            View.SYSTEM_UI_FLAG_VISIBLE
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Adjust fullscreen behavior on rotation
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            supportActionBar?.hide()
        } else {
            supportActionBar?.show()
        }
    }

    override fun onStop() {
        super.onStop()
        player.release()
    }
}