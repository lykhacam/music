package com.example.myapplication.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.View.S3Activity
import com.example.myapplication.databinding.ItemUserPlaylistBinding
import com.example.myapplication.model.UserPlaylist

class UserPlaylistAdapter(
    private var playlists: List<UserPlaylist>
) : RecyclerView.Adapter<UserPlaylistAdapter.PlaylistViewHolder>() {

    inner class PlaylistViewHolder(val binding: ItemUserPlaylistBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val binding = ItemUserPlaylistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlaylistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val playlist = playlists[position]
        holder.binding.playlistName.text = playlist.name
        holder.binding.songCount.text = "${playlist.songIds.size} bài hát"

        // Bắt sự kiện click để mở S3Activity (hoặc activity nào bạn dùng để xem playlist)
        holder.binding.root.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, S3Activity::class.java)
            intent.putExtra("playlist_name", playlist.name)
            intent.putStringArrayListExtra("playlist_song_ids", ArrayList(playlist.songIds))
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = playlists.size

    fun updatePlaylists(newList: List<UserPlaylist>) {
        playlists = newList
        notifyDataSetChanged()
    }
}
