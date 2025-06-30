package com.example.myapplication.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.databinding.ItemSongBinding
import com.example.myapplication.model.Song

class SongAdapter(
    private var songList: List<Song>,
    private val onClick: (Song) -> Unit
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    private var playingSongId: String? = null
    private var onDelete: ((Song) -> Unit)? = null  // ✅ thêm hàm xoá nhưng không thay đổi constructor

    fun updateList(newSongs: List<Song>) {
        songList = newSongs
        notifyDataSetChanged()
    }

    fun setCurrentlyPlaying(songId: String?) {
        playingSongId = songId
        notifyDataSetChanged()
    }

    // ✅ Cho phép gán callback xoá ở nơi cần (ví dụ MySongsFragment)
    fun setOnDeleteListener(listener: ((Song) -> Unit)?) {
        onDelete = listener
    }

    inner class SongViewHolder(private val binding: ItemSongBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(song: Song) {
            binding.songTitle.text = song.title
            binding.songArtist.text = song.artistNames.joinToString(", ")
            binding.songDuration.text = formatDuration(song.duration)

            if (song.isDownloaded) {
                binding.downloadStatus.setImageResource(R.drawable.ic_download)
                binding.downloadStatus.visibility = View.VISIBLE
            } else {
                binding.downloadStatus.visibility = View.GONE
            }

            Glide.with(binding.root.context)
                .load(song.image)
                .placeholder(R.drawable.img)
                .into(binding.songImage)

            val isPlaying = song.id == playingSongId
            binding.itemContainer.setBackgroundResource(
                if (isPlaying) R.drawable.bg_song_highlight else android.R.color.transparent
            )
            binding.songTitle.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    if (isPlaying) R.color.blue else android.R.color.white
                )
            )
            binding.songArtist.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    if (isPlaying) R.color.light_blue else android.R.color.darker_gray
                )
            )

            binding.root.setOnClickListener { onClick(song) }

            // ✅ Hiển thị nút xoá nếu listener được gán
            if (onDelete != null) {
                binding.btnDelete.visibility = View.VISIBLE
                binding.btnDelete.setOnClickListener { onDelete?.invoke(song) }
            } else {
                binding.btnDelete.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding = ItemSongBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(songList[position])
    }

    override fun getItemCount(): Int = songList.size

    private fun formatDuration(seconds: Int): String {
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format("%d:%02d", minutes, secs)
    }
}
