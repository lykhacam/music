package com.example.myapplication.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemPlaylistBinding
import com.example.myapplication.model.Playlist

class PlaylistAdapter(
    private var playlists: List<Playlist>,
    private val onItemClick: (Playlist) -> Unit
) :RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

    inner class PlaylistViewHolder(val binding: ItemPlaylistBinding) :
        RecyclerView.ViewHolder(binding.root){
            fun bind(playlist: Playlist) {
                binding.playlistName.text = playlist.name
                binding.playlistDesc.text = playlist.description
                binding.playlistImage.setImageResource(playlist.imageResId)

                binding.root.setOnClickListener {
                    onItemClick(playlist)
                }
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val binding = ItemPlaylistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlaylistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
//        val playlist = playlists[position]
//        holder.binding.playlistName.text = playlist.name
//        holder.binding.playlistDesc.text = playlist.description
//        holder.binding.playlistImage.setImageResource(playlist.imageResId)
        holder.bind(playlists[position])
    }

    override fun getItemCount(): Int = playlists.size

    fun setData(newPlaylists: List<Playlist>) {
        playlists = newPlaylists
        notifyDataSetChanged()
    }
}
