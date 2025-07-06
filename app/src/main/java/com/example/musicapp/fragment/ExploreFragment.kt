package com.example.myapplication.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.View.S3Activity
import com.example.myapplication.adapter.PlaylistAdapter
import com.example.myapplication.databinding.FragmentExploreBinding
import com.example.myapplication.model.Playlist
import com.example.myapplication.viewmodel.PlaylistViewModel

class ExploreFragment : Fragment() {

    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!

    private val playlistViewModel: PlaylistViewModel by viewModels()
    private lateinit var playlistAdapter: PlaylistAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExploreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playlistAdapter = PlaylistAdapter(emptyList()) { playlist ->
            openPlaylist(playlist)
        }

        binding.playListRecycler.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = playlistAdapter
            addItemDecoration(GridSpacingItemDecoration(2, 32))
        }

        playlistViewModel.playlists.observe(viewLifecycleOwner) { playlists ->
            playlistAdapter.setData(playlists)
        }
    }

    private fun openPlaylist(playlist: Playlist) {
        val intent = Intent(requireContext(), S3Activity::class.java).apply {
            putExtra("playlist_id", playlist.id)
            putExtra("playlist_image", playlist.image)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Custom class để tạo khoảng cách giữa các item
    class GridSpacingItemDecoration(
        private val spanCount: Int,
        private val spacing: Int
    ) : RecyclerView.ItemDecoration() {

        override fun getItemOffsets(
            outRect: android.graphics.Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            val position = parent.getChildAdapterPosition(view)
            val column = position % spanCount

            outRect.left = spacing - column * spacing / spanCount
            outRect.right = (column + 1) * spacing / spanCount
            if (position >= spanCount) {
                outRect.top = spacing
            }
        }
    }
}
