package com.example.myapplication.adapter

import android.graphics.Color
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

    private var currentlyPlayingId: String? = null

    fun updateList(newSongs: List<Song>) {
        songList = newSongs
        notifyDataSetChanged()
    }

    fun setCurrentlyPlaying(songId: String?) {
        currentlyPlayingId = songId
        notifyDataSetChanged()
    }

    inner class SongViewHolder(val binding: ItemSongBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(song: Song) {
            val artistNames = song.artistNames.joinToString(", ")
            binding.songTitle.text = song.title
            binding.songArtist.text = artistNames
            binding.songDuration.text = formatDuration(song.duration)
            if (song.isDownloaded) {
                binding.downloadStatus.setImageResource(R.drawable.ic_download)
                binding.downloadStatus.visibility = View.VISIBLE
            } else {
                binding.downloadStatus.visibility = View.GONE
            }

            Glide.with(binding.root)
                .load(song.image)
                .placeholder(R.drawable.img)
                .into(binding.songImage)

            // Highlight nếu đang phát
            if (song.id == currentlyPlayingId) {
                binding.itemContainer.setBackgroundResource(R.drawable.bg_song_highlight)
                binding.songTitle.setTextColor(ContextCompat.getColor(binding.root.context, R.color.blue))
                binding.songArtist.setTextColor(ContextCompat.getColor(binding.root.context, R.color.light_blue))
            } else {
                binding.itemContainer.setBackgroundColor(Color.TRANSPARENT)
                binding.songTitle.setTextColor(Color.WHITE)
                binding.songArtist.setTextColor(Color.GRAY)
            }

            binding.root.setOnClickListener {
                onClick(song)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding = ItemSongBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SongViewHolder(binding)
    }

    override fun getItemCount(): Int = songList.size

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(songList[position])
    }

    private fun formatDuration(seconds: Int): String {
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format("%d:%02d", minutes, secs)
    }
}
