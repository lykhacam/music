package com.example.myapplication.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.databinding.ItemPlaylistBinding // Sử dụng lại layout cũ
import com.example.myapplication.model.Playlist

class PlaylistAdapter(
    private var PlaylistList: List<Playlist>,
    private val onClick: (Playlist) -> Unit
) : RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

    fun setData(newList: List<Playlist>) {
        PlaylistList = newList
        notifyDataSetChanged()
    }

    inner class PlaylistViewHolder(val binding: ItemPlaylistBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(Playlist: Playlist) {
            binding.playlistName.isSelected = true
            binding.playlistName.text = Playlist.name
            binding.playlistDesc.text = Playlist.description

            Glide.with(binding.root.context)
                .load(Playlist.image)
                .into(binding.playlistImage)

            binding.root.setOnClickListener {
                onClick(Playlist)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val binding = ItemPlaylistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlaylistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.bind(PlaylistList[position])
    }

    override fun getItemCount(): Int = PlaylistList.size
}
