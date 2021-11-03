package saini.ayush.videoexperiment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import saini.ayush.videoexperiment.databinding.ItemVideosBinding

class VideosAdapter(private val interaction: Interaction? = null) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val DIFF_CALLBACK = object : DiffUtil.ItemCallback<FakeItems>() {

        override fun areItemsTheSame(oldItem: FakeItems, newItem: FakeItems): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FakeItems, newItem: FakeItems): Boolean {
            return oldItem == newItem
        }

    }
    private val differ = AsyncListDiffer(this, DIFF_CALLBACK)
    var autoPlay = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return VideosViewHolder(
            ItemVideosBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ),
            interaction
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is VideosViewHolder -> {
                holder.bind(differ.currentList[position], autoPlay)
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    fun submitList(list: List<FakeItems>) {
        differ.submitList(list)
    }

    class VideosViewHolder
    constructor(
        private val binding: ItemVideosBinding,
        private val interaction: Interaction?,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FakeItems, autoPlay: Boolean) {
            binding.playVideoRV.setOnClickListener {
                interaction?.onItemSelected(adapterPosition, item, binding)
                if (!autoPlay) binding.playVideoRV.visibility = View.GONE
            }
            itemView.tag = binding
            Glide.with(binding.root.context)
                .load(item.thumbnail)
                .into(binding.thumbnail)
        }
    }

    interface Interaction {
        fun onItemSelected(position: Int, item: FakeItems, bindingRVItem: ItemVideosBinding)
    }
}

data class FakeItems(
    val id: Int,
    val url: String,
    val thumbnail: String,
)
