package dev.adriele.adolescare.ui

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.google.android.material.transition.platform.MaterialSharedAxis
import dev.adriele.adolescare.BaseActivity
import dev.adriele.adolescare.databinding.ActivityVideoPlayerBinding
import dev.adriele.adolescare.helpers.Utility

class VideoPlayerActivity : BaseActivity() {
    private lateinit var binding: ActivityVideoPlayerBinding

    private lateinit var player: ExoPlayer

    private var path: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedAxis = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        window.enterTransition = sharedAxis
        window.exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)

        Utility.enableFullscreen(window)
        path = intent.getStringExtra("path")
    }

    override fun onStart() {
        super.onStart()

        if (!path.isNullOrEmpty()) {
            initializePlayer(path!!)
        } else {
            finish()
        }
    }

    private fun initializePlayer(assetPath: String) {
        player = ExoPlayer.Builder(this).build()
        binding.playerView.player = player

        // Copy asset to temp file because ExoPlayer doesn't support asset:// URIs directly
        val file = Utility.copyAssetToCache(this, assetPath)
        val uri = Uri.fromFile(file)

        val mediaItem = MediaItem.Builder()
            .setUri(uri)
            .setMimeType(MimeTypes.VIDEO_MP4)
            .build()

        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = true

        // 🔽 Show/hide back button based on controller visibility
        binding.playerView.setControllerVisibilityListener(
            PlayerView.ControllerVisibilityListener { visibility -> binding.btnBack.visibility = if (visibility == View.VISIBLE) View.VISIBLE else View.GONE }
        )

        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onPause() {
        super.onPause()
        player.playWhenReady = false
    }

    override fun onStop() {
        super.onStop()
        if (::player.isInitialized) {
            player.release()
        }
    }
}