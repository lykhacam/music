package com.example.myapplication.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemSongBinding
import com.example.myapplication.model.Song

class SongAdapter(private val songs: List<Song>) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    inner class SongViewHolder(private val binding: ItemSongBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(song: Song) {
            binding.songTitle.text = song.title
            binding.songArtist.text = song.artist
            binding.songDuration.text = song.duration
            binding.songImage.setImageResource(song.imageResId)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding = ItemSongBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(songs[position])
    }

    override fun getItemCount() = songs.size
}
