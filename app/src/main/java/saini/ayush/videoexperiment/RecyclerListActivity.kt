package saini.ayush.videoexperiment

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.util.Util
import saini.ayush.videoexperiment.databinding.ActivityRecyclerListBinding
import saini.ayush.videoexperiment.databinding.ItemVideosBinding

class RecyclerListActivity : AppCompatActivity() {


    private lateinit var binding: ActivityRecyclerListBinding

    private lateinit var rvadapter: VideosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecyclerListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRV()

        addItems()

    }


    private fun addItems() {

        val list = ArrayList<FakeItems>()
        for (i in 1..10) {
            list.add(
                FakeItems(
                    i,
                    getString(R.string.mux_2),
                    "https://images.ctfassets.net/hrltx12pl8hq/3MbF54EhWUhsXunc5Keueb/60774fbbff86e6bf6776f1e17a8016b4/04-nature_721703848.jpg?fit=fill&w=480&h=270"
                )
            )
        }

        binding.itemsRV.autoPlay = false
        rvadapter.autoPlay = false
        binding.itemsRV.updateList(list)
        rvadapter.submitList(list)

    }

    private fun setupRV() {
        val manager = LinearLayoutManager(this)
        rvadapter = VideosAdapter(binding.itemsRV)
        binding.itemsRV.apply {
            layoutManager = manager
            adapter = rvadapter
        }
    }

    public override fun onStart() {
        super.onStart()
        if (Util.SDK_INT >= 24) {
            binding.itemsRV.initRV()
        }
    }

    public override fun onResume() {
        super.onResume()
        if ((Util.SDK_INT < 24 || binding.itemsRV.videoPlayer == null)) {
            binding.itemsRV.initRV()
        }
    }

    public override fun onPause() {
        super.onPause()
        if (Util.SDK_INT < 24) {
            binding.itemsRV.releasePlayer()
        }
    }


    public override fun onStop() {
        super.onStop()
        if (Util.SDK_INT >= 24) {
            binding.itemsRV.releasePlayer()
        }
    }


//    public override fun onStart() {
//        super.onStart()
//        if (Util.SDK_INT >= 24) {
//            initializePlayer()
//        }
//    }
//
//    public override fun onResume() {
//        super.onResume()
//        if ((Util.SDK_INT < 24 || player == null)) {
//            initializePlayer()
//        }
//    }
//
//    public override fun onPause() {
//        super.onPause()
//        if (Util.SDK_INT < 24) {
//            releasePlayer()
//        }
//    }
//
//
//    public override fun onStop() {
//        super.onStop()
//        if (Util.SDK_INT >= 24) {
//            releasePlayer()
//        }
//    }
//
//    private fun releasePlayer() {
//        player?.run {
//            playbackPosition = this.currentPosition
//            currentWindow = this.currentWindowIndex
//            playWhenReady = this.playWhenReady
//            release()
//        }
//        player = null
//    }
//
//
//    private fun initializePlayer() {
//
//
//        trackSelector = DefaultTrackSelector(this).apply {
//            setParameters(buildUponParameters().setMaxVideoSizeSd())
//        }
//
//        player = SimpleExoPlayer.Builder(this)
//            .setTrackSelector(trackSelector)
//            .build()
//    }
//
//    override fun onItemSelected(position: Int, item: FakeItems, bindingRVItem: ItemVideosBinding) {
////        player?.stop()
////        prevBinding?.mainPlayerRVItem?.player = null
////        prevBinding?.playVideoRV?.visibility = View.VISIBLE
////        prevBinding?.thumbnail?.visibility = View.VISIBLE
////        prevBinding = bindingRVItem
////        mediaItem = MediaItem.Builder().setUri(item.url).build()
////        currentWindow = 0
////        playbackPosition = 0
////        bindingRVItem.mainPlayerRVItem.player = player
////        bindingRVItem.thumbnail.visibility = View.GONE
////        player?.setMediaItem(mediaItem)
////        player?.playWhenReady = playWhenReady
////        player?.seekTo(currentWindow, playbackPosition)
////        player?.prepare()
//    }

}