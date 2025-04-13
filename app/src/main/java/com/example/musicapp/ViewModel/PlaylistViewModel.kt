package com.example.myapplication.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.R
import com.example.myapplication.model.Playlist

class PlaylistViewModel : ViewModel() {

    private val _playlists = MutableLiveData<List<Playlist>>()
    val playlists: LiveData<List<Playlist>> = _playlists

    init {
        loadPlaylists()
    }

    private fun loadPlaylists() {
        _playlists.value = listOf(
            Playlist("Morning Vibes", "Start your day fresh", R.drawable.sample_playlist),
            Playlist("Workout Hits", "Energy booster songs", R.drawable.sample_playlist),
            Playlist("Chill Zone", "Relax and unwind", R.drawable.sample_playlist),
            Playlist("Morning Vibes", "Start your day fresh", R.drawable.sample_playlist),
            Playlist("Workout Hits", "Energy booster songs", R.drawable.sample_playlist),
            Playlist("Chill Zone", "Relax and unwind", R.drawable.sample_playlist),
        )
    }
}
