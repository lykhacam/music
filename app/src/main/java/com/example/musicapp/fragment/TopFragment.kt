package com.example.myapplication.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.View.S3Activity
import com.example.myapplication.View.S4Activity
import com.example.myapplication.adapter.CategoryAdapter
import com.example.myapplication.adapter.SongAdapter
import com.example.myapplication.databinding.FragmentTopBinding
import com.example.myapplication.model.Artist
import com.example.myapplication.model.Category
import com.example.myapplication.model.Song
import com.example.myapplication.viewmodel.ArtistViewModel
import com.example.myapplication.viewmodel.CategoryViewModel
import com.example.myapplication.viewmodel.SongViewModel

class TopFragment : Fragment() {

    private var _binding: FragmentTopBinding? = null
    private val binding get() = _binding!!

    private val songViewModel: SongViewModel by viewModels()
    private val categoryViewModel: CategoryViewModel by viewModels()
    private val artistViewModel: ArtistViewModel by viewModels()

    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var songAdapter: SongAdapter

    private var songsData: List<Song> = emptyList()
    private var artistsData: List<com.example.myapplication.model.Artist> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
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
        categoryAdapter = CategoryAdapter(emptyList()) { category ->
            goToCategoryDetail(category)
        }
        binding.playListRecycler.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryAdapter
        }

        songAdapter = SongAdapter(emptyList()) { song ->
            goToSongDetail(song)
        }

        binding.favRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = songAdapter
        }

    }

    private var allArtists: List<Artist> = emptyList()
    private var allSongs: List<Song> = emptyList()

    private fun observeViewModels() {
        Log.d("TopFragment", "observeViewModels() called")

        categoryViewModel.categories.observe(viewLifecycleOwner) {
            categoryAdapter.setData(it)
        }
//        categoryViewModel.categories.observe(viewLifecycleOwner) { categories ->
//            categoryAdapter.setData(categories)
//        }


        artistViewModel.artists.observe(viewLifecycleOwner) { artists ->
            allArtists = artists
            Log.d("TopFragment", "Đã load artists: $artists")
            tryUpdateSongList()
        }
        songViewModel.songs.observe(viewLifecycleOwner) { songs ->
            allSongs = songs
            Log.d("TopFragment", "Đã load songs: $songs")
            tryUpdateSongList()
        }

    }

    private fun tryUpdateSongList() {
        if (allArtists.isNotEmpty() && allSongs.isNotEmpty()) {
            songAdapter.updateList(allSongs)
        }
    }



    private fun goToCategoryDetail(category: Category) {
        val intent = Intent(requireContext(), S3Activity::class.java).apply {
            putExtra("category_id", category.id)
            putExtra("category_name", category.name)
            putExtra("category_image", category.image)
        }
        startActivity(intent)
    }

    private fun goToSongDetail(song: Song) {
        val currentSongList = allSongs.toCollection(ArrayList())
        val index = currentSongList.indexOfFirst { it.id == song.id }
        if (index != -1) {
            val intent = Intent(requireContext(), S4Activity::class.java).apply {
                putExtra("song_id", song.id)
                putExtra("song_title", song.title)
                putExtra("song_image", song.image)
                putExtra("song_url", song.url)
                putParcelableArrayListExtra("song_list", currentSongList)
                putExtra("current_index", index)
            }
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
