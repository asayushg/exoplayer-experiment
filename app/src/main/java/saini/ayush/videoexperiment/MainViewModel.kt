package saini.ayush.videoexperiment

import android.app.Dialog
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {


    var currentWindow = 0
    var playbackPosition = 0L

     var fullscreenDialog: Dialog? = null
     var isFullscreen = false
     var isPlaying = false

    var speedSelected = 1f
}