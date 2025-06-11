package com.example.myapplication.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.View.S4Activity
import com.example.myapplication.model.Song

class SongApproveAdapter(
    private val songs: List<Pair<String, Song>>,
    val onApprove: (String, Song) -> Unit,
    val onReject: (String) -> Unit
) : RecyclerView.Adapter<SongApproveAdapter.SongViewHolder>() {

    inner class SongViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val songImage: ImageView = view.findViewById(R.id.songImage)
        val songTitle: TextView = view.findViewById(R.id.songTitle)
        val songArtist: TextView = view.findViewById(R.id.songArtist)
        val songDuration: TextView = view.findViewById(R.id.songDuration)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }

    override fun getItemCount() = songs.size

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val (songId, song) = songs[position]

        holder.songTitle.text = song.title ?: "Không tên"
        holder.songArtist.text = song.artistNames?.joinToString(", ") ?: "Không rõ tác giả"
        holder.songDuration.text = formatDuration(song.duration)
        Glide.with(holder.itemView.context)
            .load(song.image)
            .placeholder(R.drawable.img)
            .into(holder.songImage)

        holder.itemView.setOnClickListener {
            openSongDetail(holder.itemView.context, song, songs.map { it.second }, position)
        }

        holder.itemView.setOnLongClickListener {
            showApproveDialog(holder.itemView.context, songId, song)
            true
        }

    }


    private fun formatDuration(seconds: Int?): String {
        val total = seconds ?: 0
        val min = total / 60
        val sec = total % 60
        return String.format("%d:%02d", min, sec)
    }

    private fun showApproveDialog(context: Context, songId: String, song: Song) {
        AlertDialog.Builder(context)
            .setTitle("Duyệt bài hát?")
            .setMessage("Bạn muốn duyệt hay từ chối bài hát \"${song.title}\"?")
            .setPositiveButton("✅ Duyệt") { _, _ -> onApprove(songId, song) }
            .setNegativeButton("❌ Từ chối") { _, _ -> onReject(songId) }
            .show()
    }

    private fun playDemo(url: String?, context: Context) {
        if (!url.isNullOrEmpty()) {
            val mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(url)
            mediaPlayer.setOnPreparedListener { it.start() }
            mediaPlayer.prepareAsync()
            Toast.makeText(context, "▶️ Phát thử bài hát...", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "⚠️ Không có file mp3", Toast.LENGTH_SHORT).show()
        }
    }
    private fun openSongDetail(context: Context, song: Song, songList: List<Song>, index: Int) {
        val intent = Intent(context, S4Activity::class.java).apply {
            putExtra("song_id", song.id)
            putExtra("song_title", song.title)
            putExtra("song_image", song.image)
            putExtra("song_url", song.url)
            putExtra("EXTRA_CATEGORY", song.categoryIds?.firstOrNull() ?: "")
            putParcelableArrayListExtra("song_list", ArrayList(songList))
            putExtra("current_index", index)
            putExtra("source", "admin")
        }
        context.startActivity(intent)
    }


}
