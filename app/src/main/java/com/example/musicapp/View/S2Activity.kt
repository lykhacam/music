package com.example.myapplication.View

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.adapter.PlaylistAdapter
import com.example.myapplication.adapters.SongAdapter
import com.example.myapplication.databinding.ActivityScreen2Binding
import com.example.myapplication.viewmodel.PlaylistViewModel
import com.example.myapplication.viewmodel.SongViewModel

class S2Activity : AppCompatActivity() {
    private lateinit var binding: ActivityScreen2Binding
    private val playlistViewModel: PlaylistViewModel by viewModels()
    private lateinit var playlistAdapter: PlaylistAdapter
    private val songViewModel: SongViewModel by viewModels()
    private lateinit var songAdapter: SongAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScreen2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Playlist RecyclerView
        binding.playListRecycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        playlistViewModel.playlists.observe(this, Observer { playlists ->
            playlistAdapter = PlaylistAdapter(playlists)
            binding.playListRecycler.adapter = playlistAdapter
        })

        // Setup Song RecyclerView with ViewModel
        binding.favRecycler.layoutManager = LinearLayoutManager(this)
        songViewModel.songs.observe(this, Observer { songs ->
            songAdapter = SongAdapter(songs)
            binding.favRecycler.adapter = songAdapter
        })

        // Cấu hình TabLayout
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Playlist"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Songs"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Playlist"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Songs"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Playlist"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Songs"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Playlist"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Songs"))    }
}
