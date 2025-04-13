package com.example.myapplication.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemSongBinding
import com.example.myapplication.model.Song

class SongAdapter(
    private var songs: List<Song>,
    private val onItemClick: (Song) -> Unit
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    inner class SongViewHolder(val binding: ItemSongBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(song: Song) {
            binding.songTitle.text = song.title
            binding.songArtist.text = song.artist
            binding.songImage.setImageResource(song.imageResId)

            binding.root.setOnClickListener {
                onItemClick(song)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding = ItemSongBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
//        val song = songs[position]
//        holder.binding.songTitle.text = song.title
//        holder.binding.songArtist.text = song.artist
//        holder.binding.songImage.setImageResource(song.imageResId)
        holder.bind(songs[position])
    }

    override fun getItemCount(): Int = songs.size

    fun setData(newSongs: List<Song>) {
        songs = newSongs
        notifyDataSetChanged()
    }
}
