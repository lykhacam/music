package com.example.myapplication.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.model.Song
import com.google.firebase.database.*

class SongViewModel : ViewModel() {

    // Tất cả bài hát (dùng cho S3, playlist)
    private val _songs = MutableLiveData<List<Song>>()
    val songs: LiveData<List<Song>> get() = _songs

    // Top 100 bài có count cao nhất (dùng cho Top50Fragment)
    private val _topSongs = MutableLiveData<List<Song>>()
    val topSongs: LiveData<List<Song>> get() = _topSongs

    private val databaseRef = FirebaseDatabase
        .getInstance("https://appmusicrealtime-default-rtdb.asia-southeast1.firebasedatabase.app")
        .getReference("songs")

    init {
        fetchSongs()
    }

    private fun fetchSongs() {
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val allSongs = mutableListOf<Song>()
                for (songSnapshot in snapshot.children) {
                    val song = songSnapshot.getValue(Song::class.java)
                    song?.let { allSongs.add(it) }
                }

                // Gán toàn bộ bài hát
                _songs.postValue(allSongs)

                // Gán danh sách top 100 bài có count lớn nhất
                val top = allSongs.sortedByDescending { it.count }
                    .take(100)
                _topSongs.postValue(top)

                Log.d("SongViewModel", "🎵 Total Songs: ${allSongs.size} | Top Songs: ${top.size}")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("SongViewModel", "❌ Failed to fetch songs: ${error.message}")
            }
        })
    }
}
