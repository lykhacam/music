package com.example.myapplication.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.model.Song
import com.google.firebase.database.*

class SongViewModel : ViewModel() {

    private val _songs = MutableLiveData<List<Song>>()
    val songs: LiveData<List<Song>> get() = _songs

    private val databaseRef = FirebaseDatabase
        .getInstance("https://appmusicrealtime-default-rtdb.asia-southeast1.firebasedatabase.app")
        .getReference("songs")

    init {
        fetchSongs()
    }

    private fun fetchSongs() {

        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val songList = mutableListOf<Song>()
                for (songSnapshot in snapshot.children) {
                    val song = songSnapshot.getValue(Song::class.java)
                    song?.let { songList.add(it) }
                }
                _songs.postValue(songList)

            }

            override fun onCancelled(error: DatabaseError) {
                // handle error
            }
        })
    }
}
