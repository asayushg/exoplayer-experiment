package saini.ayush.videoexperiment

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import saini.ayush.videoexperiment.databinding.ActivitySplashBinding

class SplashVideoActivity : AppCompatActivity() {


    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.button.setOnClickListener {
            startVideoActivity(1)
        }
        binding.button2.setOnClickListener {
            startVideoActivity(2)
        }
        binding.button3.setOnClickListener {
            startVideoActivity(3)
        }
        binding.button4.setOnClickListener {
            startVideoActivity(4)
        }

        binding.button5.setOnClickListener {
            startVideoActivity(5)
        }

        binding.button9.setOnClickListener {
            startActivity(Intent(this, RecyclerListActivity::class.java))
        }

        binding.button10.setOnClickListener {
            startActivity(Intent(this, AutoPlayActivity::class.java))
        }
    }

    private fun startVideoActivity(i: Int) {

        startActivity(MainActivity.getIntent(this, i))

    }


}