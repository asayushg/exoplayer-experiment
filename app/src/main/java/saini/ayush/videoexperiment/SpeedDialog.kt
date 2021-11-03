package saini.ayush.videoexperiment

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class SpeedDialog(private val setSpeedClickListener: SetSpeedClickListener) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle(R.string.set_speed)
                .setItems(R.array.colors_array) { _, which ->
                    setSpeedClickListener.setSpeedOnClicked(which)
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    interface SetSpeedClickListener {
        fun setSpeedOnClicked(speed: Int)
    }

}
