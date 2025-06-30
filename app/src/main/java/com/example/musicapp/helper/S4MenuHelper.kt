package com.example.myapplication.helper

import android.content.Context
import android.content.Intent
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import com.example.myapplication.R
import com.example.myapplication.global.GlobalStorage
import com.example.myapplication.model.Song
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

object S4MenuHelper {

    fun setup(context: Context, btnMenu: ImageButton) {
        btnMenu.setOnClickListener { view ->
            val popup = PopupMenu(context, view)
            popup.menuInflater.inflate(R.menu.menu_s4, popup.menu)

            popup.setOnMenuItemClickListener { item ->
                val currentSong = getCurrentSong() ?: return@setOnMenuItemClickListener false
                when (item.itemId) {
                    R.id.action_like -> {
                        toggleLike(context, currentSong)
                        true
                    }
                    R.id.action_download -> {
                        Toast.makeText(context, "📥 Đã đánh dấu là đã tải (demo)", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.action_share -> {
                        shareSong(context, currentSong)
                        true
                    }
                    else -> false
                }
            }

            popup.show()
        }
    }

    private fun getCurrentSong(): Song? {
        val list = GlobalStorage.currentSongList
        val index = GlobalStorage.currentSongIndex
        return list.getOrNull(index)
    }

    private fun toggleLike(context: Context, song: Song) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val favRef = FirebaseDatabase.getInstance("https://appmusicrealtime-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("users").child(uid).child("favorites")

        favRef.child(song.id).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    favRef.child(song.id).removeValue()
                    Toast.makeText(context, "💔 Đã xoá khỏi yêu thích", Toast.LENGTH_SHORT).show()
                } else {
                    favRef.child("placeholder").removeValue()
                    favRef.child(song.id).setValue(true)
                    Toast.makeText(context, "❤️ Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Lỗi: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun shareSong(context: Context, song: Song) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Nghe bài hát ${song.title}")
            putExtra(Intent.EXTRA_TEXT, "Nghe ngay tại: ${song.url}")
        }
        context.startActivity(Intent.createChooser(shareIntent, "Chia sẻ qua"))
    }
}
