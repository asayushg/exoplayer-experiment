package saini.ayush.videoexperiment

import android.annotation.SuppressLint
import android.app.ActionBar
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.analytics.AnalyticsListener
import com.google.android.exoplayer2.source.LoadEventInfo
import com.google.android.exoplayer2.source.MediaLoadData
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.MappingTrackSelector
import com.google.android.exoplayer2.ui.DefaultTimeBar
import com.google.android.exoplayer2.ui.TimeBar
import com.google.android.exoplayer2.ui.TrackSelectionDialogBuilder
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.util.Util
import com.mux.stats.sdk.core.model.CustomerData
import com.mux.stats.sdk.core.model.CustomerPlayerData
import com.mux.stats.sdk.core.model.CustomerVideoData
import com.mux.stats.sdk.core.model.CustomerViewData
import com.mux.stats.sdk.muxstats.MuxStatsExoPlayer
import saini.ayush.videoexperiment.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity(), SpeedDialog.SetSpeedClickListener {

    private lateinit var binding: ActivityMainBinding

    private var player: SimpleExoPlayer? = null
    private var muxStatsExoPlayer: MuxStatsExoPlayer? = null


    private var playWhenReady = false

    private lateinit var mediaItem: MediaItem
    private lateinit var trackSelector: DefaultTrackSelector

    private lateinit var viewModel: MainViewModel

    companion object {
        const val TYPE = "type"
        fun getIntent(context: Context, int: Int): Intent {
            return Intent(context, MainActivity::class.java).apply {
                putExtra(TYPE, int)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        setContentView(binding.root)
        setType()
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        binding.button6.setOnClickListener {
            SpeedDialog(this).show(supportFragmentManager, "abc")
        }

        binding.button7.setOnClickListener {
            videoQualityDialog()
        }

        binding.button8.setOnClickListener {
            openFullscreenDialog()
        }

        binding.playVideoThumbnail.setOnClickListener {
            viewModel.isPlaying = true
            it.visibility = View.GONE
            binding.customThumbnail.visibility = View.GONE
            binding.mainPlayer.controllerHideOnTouch = true
            player?.play()
        }

        initFullScreenDialog()
        if (viewModel.isPlaying) {
            binding.playVideoThumbnail.performClick()
            playWhenReady = true
        }

        addSeekBarPreview()

        val orientation = resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            showSystemUI()
        } else {
            hideSystemUI()
        }

    }

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, binding.mainPlayer).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun showSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(window,
            binding.mainPlayer).show(WindowInsetsCompat.Type.systemBars())
    }


    private fun videoQualityDialog() {
        val mappedTrackInfo = trackSelector.currentMappedTrackInfo
        var videoRenderer: Int? = null

        if (mappedTrackInfo == null) return

        for (i in 0 until mappedTrackInfo.rendererCount) {
            if (isVideoRenderer(mappedTrackInfo, i)) {
                videoRenderer = i
            }
        }

        if (videoRenderer == null) return

        val trackSelectionDialogBuilder = TrackSelectionDialogBuilder(
            this,
            getString(R.string.qualitySelector),
            trackSelector,
            videoRenderer
        )

        trackSelectionDialogBuilder.setTrackNameProvider {
            // Override function getTrackName
            getString(R.string.exo_track_resolution_pixel, it.height)
        }
        val trackDialog = trackSelectionDialogBuilder.build()
        trackDialog.show()

    }

    private fun setType() {
        intent.getIntExtra(TYPE, 0).let {
            mediaItem = when (it) {
                1 -> MediaItem.fromUri(getString(R.string.media_url_mp4_drive))
                2 -> MediaItem.fromUri(getString(R.string.gdrive_mp4))
                3 -> MediaItem.Builder()
                    .setUri(getString(R.string.media_url_dash))
                    .setMimeType(MimeTypes.APPLICATION_MPD)
                    .build()
                4 -> MediaItem.Builder().setUri(getString(R.string.media_url_m3u8)).build()
                5 -> MediaItem.Builder().setUri(getString(R.string.mux_2)).build()
                else -> MediaItem.fromUri(getString(R.string.media_url_mp3))
            }
        }
    }

    override fun onBackPressed() {
        if (viewModel.isFullscreen) closeFullscreenDialog()
        else super.onBackPressed()
    }

    private fun initializePlayer() {

        Glide.with(this)
            .load("https://images.ctfassets.net/hrltx12pl8hq/3MbF54EhWUhsXunc5Keueb/60774fbbff86e6bf6776f1e17a8016b4/04-nature_721703848.jpg?fit=fill&w=480&h=270")
            .into(binding.customThumbnail)


        trackSelector = DefaultTrackSelector(this).apply {
            setParameters(buildUponParameters().setMaxVideoSizeSd())
        }

        player = SimpleExoPlayer.Builder(this)
            .setTrackSelector(trackSelector)
            .build()
            .also { exoPlayer ->
                (binding.mainPlayer.videoSurfaceView as SurfaceView).setSecure(true)
                val customerPlayerData = CustomerPlayerData()
                customerPlayerData.environmentKey = getString(R.string.mux_env)
                val customerVideoData = CustomerVideoData()
                customerVideoData.videoTitle = "Sample"
                val customerViewData = CustomerViewData()
                customerVideoData.videoId = "12345"
                customerVideoData.videoStreamType = "m3u8"

                muxStatsExoPlayer = MuxStatsExoPlayer(
                    this,
                    exoPlayer,
                    "demo",
                    CustomerData(
                        customerPlayerData,
                        customerVideoData,
                        customerViewData
                    )
                )
                muxStatsExoPlayer?.setPlayerView(binding.mainPlayer)

                binding.mainPlayer.player = exoPlayer
                exoPlayer.addListener(playbackStateListener)
                exoPlayer.addAnalyticsListener(analyticsListener)

                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.playWhenReady = playWhenReady
                exoPlayer.seekTo(viewModel.currentWindow, viewModel.playbackPosition)

                exoPlayer.prepare()
            }

        updateSpeed()
    }

    private val playbackStateListener: Player.Listener = playbackStateListener()
    private val analyticsListener: AnalyticsListener = exoplayerAnalytics()

    private fun playbackStateListener() = object : Player.Listener {

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            binding.mainPlayer.keepScreenOn = isPlaying
            viewModel.isPlaying = isPlaying
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_READY -> {
                    binding.progressBar.visibility = View.GONE
                }
                Player.STATE_ENDED -> {
                    viewModel.isPlaying = false
                }
                Player.STATE_BUFFERING -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                Player.STATE_IDLE -> {
                    //your logic
                }
                else -> {

                }
            }
        }

        override fun onPlayerError(error: ExoPlaybackException) {
            super.onPlayerError(error)
            Log.d("exoplayer error", "onPlayerError: ${error.sourceException.message}")
        }
    }

    private fun exoplayerAnalytics() = object : AnalyticsListener {
        override fun onLoadStarted(
            eventTime: AnalyticsListener.EventTime,
            loadEventInfo: LoadEventInfo,
            mediaLoadData: MediaLoadData,
        ) {
            super.onLoadStarted(eventTime, loadEventInfo, mediaLoadData)

        }
    }

    public override fun onStart() {
        super.onStart()
        if (Util.SDK_INT >= 24) {
            initializePlayer()
        }
    }

    public override fun onResume() {
        super.onResume()
        if ((Util.SDK_INT < 24 || player == null)) {
            initializePlayer()
        }
    }

    public override fun onPause() {
        super.onPause()
        if (Util.SDK_INT < 24) {
            releasePlayer()
        }
    }


    public override fun onStop() {
        super.onStop()
        if (Util.SDK_INT >= 24) {
            releasePlayer()
        }
    }

    private fun releasePlayer() {
        player?.run {
            viewModel.playbackPosition = this.currentPosition
            viewModel.currentWindow = this.currentWindowIndex
            playWhenReady = this.playWhenReady
            removeListener(playbackStateListener)
            removeAnalyticsListener(analyticsListener)
            release()
            muxStatsExoPlayer?.release()
        }
        player = null
    }

    override fun setSpeedOnClicked(speed: Int) {
        viewModel.speedSelected = when (speed) {
            0 -> 0.5f
            1 -> 1f
            2 -> 1.25f
            3 -> 1.5f
            4 -> 2f
            else -> 1f
        }
        updateSpeed()
    }

    private fun updateSpeed() {
        val playbackParams = PlaybackParameters(viewModel.speedSelected)
        player?.playbackParameters = playbackParams
    }


    private fun isVideoRenderer(
        mappedTrackInfo: MappingTrackSelector.MappedTrackInfo,
        rendererIndex: Int,
    ): Boolean {
        val trackGroupArray = mappedTrackInfo.getTrackGroups(rendererIndex)
        if (trackGroupArray.length == 0) {
            return false
        }
        val trackType = mappedTrackInfo.getRendererType(rendererIndex)
        return C.TRACK_TYPE_VIDEO == trackType
    }

    // fullscreen
    private fun openFullscreenDialog() {
        //  exoFullScreenIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_fullscreen_shrink))
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        //(binding.mainPlayer.parent as ViewGroup).removeView(binding.mainPlayer)
//        viewModel.fullscreenDialog?.addContentView(binding.mainPlayer,
//            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.MATCH_PARENT))
        viewModel.isFullscreen = true
//
//        viewModel.fullscreenDialog?.show()
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun closeFullscreenDialog() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
//        (binding.mainPlayer.parent as ViewGroup).removeView(binding.mainPlayer)
//        mainFrameLayout.addView(binding.mainPlayer)
//        exoFullScreenIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_fullscreen_expand))
        viewModel.isFullscreen = false
//        viewModel.fullscreenDialog?.dismiss()
    }

    private fun initFullScreenDialog() {
        if (viewModel.fullscreenDialog == null) viewModel.fullscreenDialog =
            object : Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen) {
                override fun onBackPressed() {
                    if (viewModel.isFullscreen) closeFullscreenDialog()
                    else super.onBackPressed()
                }
            }

        if (viewModel.isFullscreen) openFullscreenDialog()

    }


    /// seek bar

    private lateinit var previewFrameLayout: FrameLayout
    private lateinit var previewImage: ImageView
    private lateinit var exoProgress: DefaultTimeBar


    private fun addSeekBarPreview() {

        val playerView = binding.mainPlayer

        previewFrameLayout = playerView.findViewById(R.id.preview_frame_layout)
        previewImage = playerView.findViewById(R.id.preview_image)
        exoProgress = playerView.findViewById(R.id.exo_progress)
        if (  intent.getIntExtra(TYPE, 0) == 5) {
            exoProgress.addListener(object : TimeBar.OnScrubListener {
                override fun onScrubMove(timeBar: TimeBar, position: Long) {
                    previewFrameLayout.visibility = View.VISIBLE
                    previewFrameLayout.x =
                        updatePreviewX(position.toInt(), player?.duration?.toInt()).toFloat()

                    val drawable = previewImage.drawable
                    var glideOptions = RequestOptions().dontAnimate().skipMemoryCache(false)
                    if (drawable != null) {
                        glideOptions = glideOptions.placeholder(drawable)
                    }
                    val THUMBNAIL_MOSAIQUE = getString(R.string.mux_2_img)
                    Glide.with(previewImage).asBitmap()
                        .apply(glideOptions)
                        .load(THUMBNAIL_MOSAIQUE)
                        .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                        .transform(GlideThumbnailTransformation(position))
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .into(previewImage)
                }

                override fun onScrubStop(timeBar: TimeBar, position: Long, canceled: Boolean) {
                    previewFrameLayout.visibility = View.INVISIBLE
                }

                override fun onScrubStart(timeBar: TimeBar, position: Long) {}
            })
        }

    }

    private fun updatePreviewX(progress: Int, max: Int?): Int {
        if (max == null || max == 0) {
            return 0
        }

        val parent = previewFrameLayout.parent as ViewGroup
        val layoutParams = previewFrameLayout.layoutParams as ViewGroup.MarginLayoutParams
        val offset = progress.toFloat() / max
        val minimumX: Int = previewFrameLayout.left
        val maximumX = (parent.width - parent.paddingRight - layoutParams.rightMargin)

        val previewPaddingRadius: Int =
            resources.getDimensionPixelSize(R.dimen.scrubber_dragged_size).div(2)
        val previewLeftX = (exoProgress as View).left.toFloat()
        val previewRightX = (exoProgress as View).right.toFloat()
        val previewSeekBarStartX: Float = previewLeftX + previewPaddingRadius
        val previewSeekBarEndX: Float = previewRightX - previewPaddingRadius
        val currentX = (previewSeekBarStartX + (previewSeekBarEndX - previewSeekBarStartX) * offset)
        val startX: Float = currentX - previewFrameLayout.width / 2f
        val endX: Float = startX + previewFrameLayout.width

        // Clamp the moves
        return if (startX >= minimumX && endX <= maximumX) {
            startX.toInt()
        } else if (startX < minimumX) {
            minimumX
        } else {
            maximumX - previewFrameLayout.width
        }
    }

}