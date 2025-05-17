package com.example.myapplication.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.model.Artist
import com.google.firebase.database.*

class ArtistViewModel : ViewModel() {

    private val _artists = MutableLiveData<List<Artist>>()
    val artists: LiveData<List<Artist>> = _artists

    private val dbRef = FirebaseDatabase
        .getInstance("https://appmusicrealtime-default-rtdb.asia-southeast1.firebasedatabase.app")
        .getReference("artists")

    init {
        fetchArtists()
    }

    private fun fetchArtists() {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val artistList = mutableListOf<Artist>()
                for (child in snapshot.children) {
                    child.getValue(Artist::class.java)?.let {
                        artistList.add(it)
                    }
                }
                _artists.value = artistList
                Log.d("ArtistViewModel", "Fetched artists: $artistList")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ArtistViewModel", "Failed to fetch artists", error.toException())
            }
        })
    }
}
