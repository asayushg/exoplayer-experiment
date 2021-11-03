package saini.ayush.videoexperiment

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.exoplayer2.util.Util
import saini.ayush.videoexperiment.databinding.ActivityAutoPlayBinding

class AutoPlayActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAutoPlayBinding

    private lateinit var rvadapter: VideosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAutoPlayBinding.inflate(layoutInflater)
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

        binding.itemsRV.updateList(list)
        rvadapter.submitList(list)

    }

    private fun setupRV() {
        val manager = LinearLayoutManager(this)
        rvadapter = VideosAdapter()
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

}