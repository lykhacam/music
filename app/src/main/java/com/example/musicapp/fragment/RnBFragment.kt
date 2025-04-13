package com.example.myapplication.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.ViewModel.PlaylistViewModel
import com.example.myapplication.ViewModel.SongViewModel
import com.example.myapplication.adapter.PlaylistAdapter
import com.example.myapplication.adapter.SongAdapter
import com.example.myapplication.databinding.FragmentRnbBinding
import com.example.myapplication.model.Playlist
import com.example.myapplication.model.Song
import com.example.myapplication.View.S3Activity
import com.example.myapplication.View.S4Activity

class RnbFragment : Fragment() {

    // ViewBinding để thao tác với các view trong fragment_Rnb.xml
    private var _binding: FragmentRnbBinding? = null
    private val binding get() = _binding!!

    // Adapter để hiển thị danh sách playlist và bài hát
    private lateinit var playlistAdapter: PlaylistAdapter
    private lateinit var songAdapter: SongAdapter

    // ViewModel chứa dữ liệu Playlist và Song
    private val playlistViewModel: PlaylistViewModel by viewModels()
    private val songViewModel: SongViewModel by viewModels()

    // Inflate layout Fragment và gán binding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRnbBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Khi view đã được tạo xong
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Gọi hàm setup RecyclerView + Adapters
        setupAdapters()

        // Gọi hàm quan sát dữ liệu từ ViewModel
        observeData()
    }

    // Khởi tạo adapters và gắn vào RecyclerView
    private fun setupAdapters() {
        // Adapter hiển thị danh sách playlist (horizontal)
        playlistAdapter = PlaylistAdapter(emptyList()) { playlist ->
            navigateToScreen3(playlist) // Khi click vào 1 item playlist
        }

        binding.playListRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = playlistAdapter
        }

        // Adapter hiển thị danh sách bài hát yêu thích (vertical)
        songAdapter = SongAdapter(emptyList()) { song ->
            navigateToScreen4(song) // Khi click vào 1 item bài hát
        }

        binding.favRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = songAdapter
        }
    }

    // Lắng nghe thay đổi dữ liệu từ ViewModel và cập nhật adapter
    private fun observeData() {
        playlistViewModel.playlists.observe(viewLifecycleOwner) { playlists ->
            playlistAdapter.setData(playlists) // Cập nhật danh sách playlist
        }

        songViewModel.songs.observe(viewLifecycleOwner) { songs ->
            songAdapter.setData(songs) // Cập nhật danh sách bài hát
        }
    }

    // Hàm điều hướng sang Screen3Activity khi click vào playlist
    private fun navigateToScreen3(playlist: Playlist) {
        val intent = Intent(requireContext(), S3Activity::class.java).apply {
            putExtra("playlist_name", playlist.name)
            putExtra("playlist_desc", playlist.description)
            putExtra("playlist_image", playlist.imageResId)
        }
        startActivity(intent)
    }

    // Hàm điều hướng sang Screen4Activity khi click vào bài hát
    private fun navigateToScreen4(song: Song) {
        val intent = Intent(requireContext(), S4Activity::class.java).apply {
            putExtra("song_title", song.title)
            putExtra("song_artist", song.artist)
            putExtra("song_image", song.imageResId)
        }
        startActivity(intent)
    }

    // Hủy binding khi view bị destroy để tránh memory leak
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
