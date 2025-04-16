package com.example.myapplication.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.View.S3Activity
import com.example.myapplication.View.S4Activity
import com.example.myapplication.ViewModel.PlaylistViewModel
import com.example.myapplication.ViewModel.SongViewModel
import com.example.myapplication.adapter.PlaylistAdapter
import com.example.myapplication.adapter.SongAdapter
import com.example.myapplication.databinding.FragmentTopBinding
import com.example.myapplication.model.Playlist
import com.example.myapplication.model.Song

class TopFragment : Fragment() {

    private var _binding: FragmentTopBinding? = null
    private val binding get() = _binding!!

    private val playlistViewModel: PlaylistViewModel by viewModels()
    private val songViewModel: SongViewModel by viewModels()

    private lateinit var playlistAdapter: PlaylistAdapter
    private lateinit var songAdapter: SongAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTopBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        observeViewModels()
    }

    private fun setupRecyclerViews() {
        // Setup danh sách playlist (ngang)
        playlistAdapter = PlaylistAdapter(emptyList()) { playlist ->
            goToPlaylistDetail(playlist)
        }
        binding.playListRecycler.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = playlistAdapter
        }

        // Setup danh sách bài hát (dọc)
        songAdapter = SongAdapter(emptyList()) { song ->
            goToSongDetail(song)
        }
        binding.favRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = songAdapter
        }
    }

    private fun observeViewModels() {
        playlistViewModel.playlists.observe(viewLifecycleOwner) {
            playlistAdapter.setData(it)
        }

        songViewModel.songs.observe(viewLifecycleOwner) {
            songAdapter.setData(it)
        }
    }

    private fun goToPlaylistDetail(playlist: Playlist) {
        val intent = Intent(requireContext(), S3Activity::class.java).apply {
            putExtra("playlist_name", playlist.name)
            putExtra("playlist_desc", playlist.description)
            putExtra("playlist_image", playlist.imageResId)
        }
        startActivity(intent)
    }

    private fun goToSongDetail(song: Song) {
        val intent = Intent(requireContext(), S4Activity::class.java).apply {
            putExtra("song_title", song.title)
            putExtra("song_artist", song.artist)
            putExtra("song_image", song.imageResId)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
