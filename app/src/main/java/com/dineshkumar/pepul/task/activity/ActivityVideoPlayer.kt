package com.dineshkumar.pepul.task.activity

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.dineshkumar.pepul.task.R
import com.dineshkumar.pepul.task.databinding.LayoutVideoPlayerBinding
import com.dineshkumar.pepul.task.databinding.LayoutViewImageBinding
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import java.io.*
import java.util.*

class ActivityVideoPlayer : AppCompatActivity() {
    lateinit var binding: LayoutVideoPlayerBinding
    private lateinit var simpleExoPlayer: SimpleExoPlayer
    private lateinit var mediaSource: MediaSource
    var url = ""
    private lateinit var fullScreen: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutVideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        url = intent.extras!!.getString("url")!!
        println("---url $url")

        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.title = "" + url

        binding.playerView.keepScreenOn = true
        initPlayer()

        fullScreen = findViewById(R.id.exo_fullscreen)
        fullScreen.setOnClickListener {
            val orientation = this.resources.configuration.orientation
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                println("---ORIENTATION_PORTRAIT")
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                fullScreen.setImageDrawable(
                    ContextCompat.getDrawable(
                        this@ActivityVideoPlayer,
                        R.drawable.ic_baseline_fullscreen_exit_24
                    )
                )
            } else {
                println("---ORIENTATION_LANDSCAPE")
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                fullScreen.setImageDrawable(
                    ContextCompat.getDrawable(
                        this@ActivityVideoPlayer,
                        R.drawable.ic_baseline_fullscreen_24
                    )
                )
            }

        }
    }

    private fun initPlayer() {
        simpleExoPlayer = SimpleExoPlayer.Builder(this).build()
        simpleExoPlayer.addListener(playerListener)
        binding.playerView.player = simpleExoPlayer

        //create mediaSource
        createMediaSource()
    }

    private fun createMediaSource() {
        simpleExoPlayer.seekTo(0)
        if (url.contains("/hls/")) {
            val dataSourceFactory: DataSource.Factory =
                DefaultDataSourceFactory(this, Util.getUserAgent(this, applicationInfo.name))
            mediaSource = HlsMediaSource.Factory(dataSourceFactory).createMediaSource(
                MediaItem.fromUri(
                    Uri.parse(url)
                )
            )
        } else {
            val dataSourceFactory: DataSource.Factory =
                DefaultDataSourceFactory(this, Util.getUserAgent(this, applicationInfo.name))
            mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(
                MediaItem.fromUri(
                    Uri.parse(url)
                )
            )
        }

        simpleExoPlayer.setMediaSource(mediaSource)
        simpleExoPlayer.prepare()
        simpleExoPlayer.playWhenReady = true
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        val orientation = this.resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            println("---ORIENTATION_PORTRAIT")
            // hideSystemUI()
            binding.toolbar.visibility = View.VISIBLE
            binding.playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT;
        } else {
            println("---ORIENTATION_LANDSCAPE")
            //showSystemUI()
            binding.playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL;

            binding.toolbar.visibility = View.GONE
            binding.toolbar.animate()
                .translationY(0F)
                .alpha(1F).setDuration(1500).interpolator = DecelerateInterpolator()
        }
        window.decorView.requestLayout()
    }

    override fun onResume() {
        super.onResume()
        simpleExoPlayer.playWhenReady = true
        simpleExoPlayer.play()
    }

    override fun onPause() {
        super.onPause()
        simpleExoPlayer.pause()
        simpleExoPlayer.playWhenReady = false
    }

    override fun onStop() {
        super.onStop()
        simpleExoPlayer.pause()
        simpleExoPlayer.playWhenReady = false
    }

    override fun onDestroy() {
        super.onDestroy()
        simpleExoPlayer.removeListener(playerListener)
        simpleExoPlayer.stop()
        simpleExoPlayer.clearMediaItems()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    private var playerListener = object : Player.Listener {
        override fun onRenderedFirstFrame() {
            super.onRenderedFirstFrame()
            // playerView.useController = !url.contains("/hls/")
        }

        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)
            Toast.makeText(this@ActivityVideoPlayer, "" + { error.message }, Toast.LENGTH_LONG)
                .show()
        }
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
        }
        return super.onOptionsItemSelected(menuItem)
    }
}