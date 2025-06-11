package com.example.myapplication.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

    private val topSongsFull = mutableListOf<Song>()
    private val displayedSongs = mutableListOf<Song>()
    private var currentIndex = 0
    private val batchSize = 10
    private var isLoading = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSongListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        songAdapter = SongAdapter(displayedSongs) { song ->
            openSongDetail(song)
        }

        binding.favRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = songAdapter

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val lastVisible = layoutManager.findLastVisibleItemPosition()
                    if (!isLoading && lastVisible >= displayedSongs.size - 2) {
                        loadMoreSongs()
                    }
                }
            })
        }

        songViewModel.songs.observe(viewLifecycleOwner) { songs ->
            topSongsFull.clear()
            topSongsFull.addAll(songs.sortedByDescending { it.count }.take(50))

            displayedSongs.clear()
            currentIndex = 0
            songAdapter.updateList(emptyList())
            loadMoreSongs()
        }
    }

    private fun loadMoreSongs() {
        isLoading = true

        val nextIndex = (currentIndex + batchSize).coerceAtMost(topSongsFull.size)
        val nextBatch = topSongsFull.subList(currentIndex, nextIndex)
        displayedSongs.addAll(nextBatch)
        songAdapter.updateList(displayedSongs.toList())
        currentIndex = nextIndex

        isLoading = false
    }

    private fun openSongDetail(song: Song) {
        val index = displayedSongs.indexOfFirst { it.id == song.id }
        if (index == -1) return

        val intent = Intent(requireContext(), S4Activity::class.java).apply {
            putExtra("song_id", song.id)
            putExtra("song_title", song.title)
            putExtra("song_image", song.image)
            putExtra("song_url", song.url)
            putExtra("EXTRA_CATEGORY", song.categoryIds.firstOrNull() ?: "")
            putParcelableArrayListExtra("song_list", ArrayList(displayedSongs))
            putExtra("current_index", index)
            putExtra("source", "home")
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
