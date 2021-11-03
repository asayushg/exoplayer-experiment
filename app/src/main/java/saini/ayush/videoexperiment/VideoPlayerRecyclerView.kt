package saini.ayush.videoexperiment

import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.view.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.analytics.AnalyticsListener
import com.google.android.exoplayer2.source.LoadEventInfo
import com.google.android.exoplayer2.source.MediaLoadData
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import saini.ayush.videoexperiment.databinding.ItemVideosBinding


class VideoPlayerRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : RecyclerView(context, attrs, defStyle), VideosAdapter.Interaction {


    var videoPlayer: SimpleExoPlayer? = null

    private var videoSurfaceDefaultHeight = 0
    private var screenDefaultHeight = 0

    var autoPlay: Boolean = true
    private lateinit var mediaItem: MediaItem
    private var trackSelector = DefaultTrackSelector(context).apply {
        setParameters(buildUponParameters().setMaxVideoSizeSd())
    }
    private var currentWindow = 0
    private var playbackPosition = 0L
    private var playPosition = -1
    var isPlaying = false
    var speedSelected = 1f

    // Change these for custom data
    private var mediaObjects: ArrayList<FakeItems> = ArrayList()
    private var itemBinding: ItemVideosBinding? = null
    private val playbackStateListener: Player.Listener = playbackStateListener()
    private val analyticsListener: AnalyticsListener = exoplayerAnalytics()

    private var playFirst = true


    fun initRV() {

        val display =
            (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        val point = Point()
        display.getSize(point)
        videoSurfaceDefaultHeight = point.x
        screenDefaultHeight = point.y

        if (videoPlayer == null) {

            videoPlayer = SimpleExoPlayer.Builder(context)
                .setTrackSelector(trackSelector)
                .build()
                .also { exoPlayer ->
                    exoPlayer.addListener(playbackStateListener)
                    exoPlayer.addAnalyticsListener(analyticsListener)
                }

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == SCROLL_STATE_IDLE) {
                        if (autoPlay) autoPlayVideo(!recyclerView.canScrollVertically(1))
                        else {
                            val first =
                                (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                            val last =
                                (layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
                            if (playPosition !in first..last) {
                                resetVideo()
                                playPosition = -1
                            }
                        }
                    }
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                }
            })

            addOnChildAttachStateChangeListener(object :
                OnChildAttachStateChangeListener {
                override fun onChildViewAttachedToWindow(view: View) {
                }

                override fun onChildViewDetachedFromWindow(view: View) {
                    if (autoPlay) resetVideo()
                }

            })
        }
    }

    private fun autoPlayVideo(lastItem: Boolean) {

        val targetPosition: Int

        if (lastItem) {
            targetPosition = mediaObjects.size - 1
        } else {
            val startPosition =
                (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            var endPosition =
                (layoutManager as LinearLayoutManager).findLastVisibleItemPosition()


            // if there is more than 2 list-items on the screen, set the difference to be 1
            if (endPosition - startPosition > 1) {
                endPosition = startPosition + 1
            }

            // something is wrong. return.
            if (startPosition < 0 || endPosition < 0) {
                return
            }

            // if there is more than 1 list-item on the screen
            targetPosition = if (startPosition != endPosition) {
                val startPositionVideoHeight: Int = getVisibleVideoSurfaceHeight(startPosition)
                val endPositionVideoHeight: Int = getVisibleVideoSurfaceHeight(endPosition)
                if (startPositionVideoHeight > endPositionVideoHeight) startPosition else endPosition
            } else {
                startPosition
            }
        }

        // video is already playing so return
        if (targetPosition == playPosition) return
        // set the position of the list-item that is to be played
        playPosition = targetPosition


        val currentPosition =
            targetPosition - (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        val itemVideosBinding: ItemVideosBinding =
            getChildAt(currentPosition).tag as ItemVideosBinding

        playVideo(itemVideosBinding)

    }

    private fun playVideo(itemVideosBinding: ItemVideosBinding) {
        resetVideo()
        mediaItem = MediaItem.Builder().setUri(mediaObjects[playPosition].url).build()

        videoPlayer?.let {
            itemVideosBinding.mainPlayerRVItem.player = it
            it.setMediaItem(mediaItem)
            it.playWhenReady = true
            it.seekTo(currentWindow, playbackPosition)
            it.prepare()
        }
        itemBinding = itemVideosBinding
        showThumbnail(false)
    }

    private fun showThumbnail(b: Boolean) {
        val v = if (b) VISIBLE else GONE
        itemBinding?.thumbnail?.visibility = v
        itemBinding?.playVideoRV?.visibility = v
    }


    private fun resetVideo() {
        videoPlayer?.stop()

        itemBinding?.mainPlayerRVItem?.player = null
        showThumbnail(true)
        itemBinding = null
    }


    private fun playbackStateListener() = object : Player.Listener {

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            itemBinding?.mainPlayerRVItem?.keepScreenOn = isPlaying
            this@VideoPlayerRecyclerView.isPlaying = isPlaying
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_READY -> {
                    itemBinding?.progressBar?.visibility = View.GONE
                }
                Player.STATE_ENDED -> {
                    isPlaying = false
                    itemBinding?.progressBar?.visibility = View.GONE
                }
                Player.STATE_BUFFERING -> {
                    itemBinding?.progressBar?.visibility = View.VISIBLE
                }
                Player.STATE_IDLE -> {
                    //your logic
                }
                else -> {

                }
            }
        }
    }

    // Analytics
    private fun exoplayerAnalytics() = object : AnalyticsListener {
        override fun onLoadStarted(
            eventTime: AnalyticsListener.EventTime,
            loadEventInfo: LoadEventInfo,
            mediaLoadData: MediaLoadData,
        ) {
            super.onLoadStarted(eventTime, loadEventInfo, mediaLoadData)

        }
    }


    fun releasePlayer() {
        videoPlayer?.run {
            playbackPosition = this.currentPosition
            currentWindow = this.currentWindowIndex
            playWhenReady = this.playWhenReady
            removeListener(playbackStateListener)
            removeAnalyticsListener(analyticsListener)
            release()
        }
        videoPlayer = null
        playPosition = -1
    }


    /**
     * Returns the visible region of the video surface on the screen.
     * if some is cut off, it will return less than the @videoSurfaceDefaultHeight
     * @param playPosition
     * @return
     */
    private fun getVisibleVideoSurfaceHeight(playPosition: Int): Int {
        val at =
            playPosition - (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()

        val child = getChildAt(at) ?: return 0
        val location = IntArray(2)
        child.getLocationInWindow(location)
        return if (location[1] < 0) {
            location[1] + videoSurfaceDefaultHeight
        } else {
            screenDefaultHeight - location[1]
        }
    }

    override fun onItemSelected(position: Int, item: FakeItems, bindingRVItem: ItemVideosBinding) {
        playPosition = position
        playVideo(bindingRVItem)
    }

    fun updateList(list: ArrayList<FakeItems>) {
        mediaObjects = list
        // auto play 1st video
        post {
            val itemVideosBinding: ItemVideosBinding =
                getChildAt(0).tag as ItemVideosBinding
            playPosition = 0
            playVideo(itemVideosBinding)
        }
    }

}