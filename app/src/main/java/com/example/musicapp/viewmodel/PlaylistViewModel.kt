package com.example.myapplication.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.model.Playlist
import com.google.firebase.database.*

class PlaylistViewModel : ViewModel() {

    private val _playlists = MutableLiveData<List<Playlist>>()
    val playlists: LiveData<List<Playlist>> get() = _playlists

    private val databaseRef: DatabaseReference = FirebaseDatabase
        .getInstance("https://appmusicrealtime-default-rtdb.asia-southeast1.firebasedatabase.app")
        .getReference("categories")

    init {
        fetchPlaylists()
    }

    private fun fetchPlaylists() {
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val playlistList = mutableListOf<Playlist>()
                for (playlistSnapshot in snapshot.children) {
                    val playlist = playlistSnapshot.getValue(Playlist::class.java)
                    playlist?.let { playlistList.add(it) }
                }
                _playlists.postValue(playlistList)
            }

            override fun onCancelled(error: DatabaseError) {
                // handle error
            }
        })
    }
}
