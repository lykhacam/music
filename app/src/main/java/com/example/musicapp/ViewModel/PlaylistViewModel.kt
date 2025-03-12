package com.example.myapplication.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.R
import com.example.myapplication.model.Playlist

class PlaylistViewModel : ViewModel() {
    private val _playlists = MutableLiveData<List<Playlist>>()
    val playlists: LiveData<List<Playlist>> get() = _playlists

    init {
        loadPlaylists()
    }

    private fun loadPlaylists() {
        _playlists.value = listOf(
            Playlist("Chill Vibes", "Relaxing & soft music", R.drawable.sample_playlist),
            Playlist("Top Hits", "Most played songs", R.drawable.sample_playlist),
            Playlist("Workout", "Energetic beats", R.drawable.sample_playlist),
            Playlist("Lo-Fi Beats", "Study & focus music", R.drawable.sample_playlist)
        )
    }
}
