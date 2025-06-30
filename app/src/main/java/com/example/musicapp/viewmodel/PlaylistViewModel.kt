package com.example.myapplication.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.model.Playlist
import com.google.firebase.database.*

class PlaylistViewModel : ViewModel() {
    private val _playlists = MutableLiveData<List<Playlist>>()
    val playlists: LiveData<List<Playlist>> = _playlists

    init {
        val database = FirebaseDatabase.getInstance("https://appmusicrealtime-default-rtdb.asia-southeast1.firebasedatabase.app")
        val ref = database.getReference("playlist")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Playlist>()
                for (data in snapshot.children) {
                    data.getValue(Playlist::class.java)?.let { list.add(it) }
                }
                _playlists.value = list
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
