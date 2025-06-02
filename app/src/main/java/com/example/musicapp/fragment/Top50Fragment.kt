// Top50Fragment.kt
package com.example.myapplication.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.View.S4Activity
import com.example.myapplication.adapter.SongAdapter
import com.example.myapplication.databinding.FragmentSongListBinding
import com.example.myapplication.model.Song
import com.example.myapplication.viewmodel.SongViewModel

class Top50Fragment : Fragment() {

    private var _binding: FragmentSongListBinding? = null
    private val binding get() = _binding!!

    private val songViewModel: SongViewModel by viewModels()
    private lateinit var songAdapter: SongAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSongListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        songAdapter = SongAdapter(emptyList()) { song ->
            openSongDetail(song)
        }

        binding.favRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = songAdapter
        }

        songViewModel.songs.observe(viewLifecycleOwner) { songs ->
            val topSongs = songs.sortedByDescending { it.count }.take(50)
            songAdapter.updateList(topSongs)
        }
    }

    private fun openSongDetail(song: Song) {
        val songList = songViewModel.songs.value ?: return
        val index = songList.indexOfFirst { it.id == song.id }
        if (index == -1) return

        val intent = Intent(requireContext(), S4Activity::class.java).apply {
            putExtra("song_id", song.id)
            putExtra("song_title", song.title)
            putExtra("song_image", song.image)
            putExtra("song_url", song.url)
            putExtra("EXTRA_CATEGORY", song.categoryIds.firstOrNull() ?: "")
            putParcelableArrayListExtra("song_list", ArrayList(songList))
            putExtra("current_index", index)
        }
        intent.putExtra("source", "home")
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
