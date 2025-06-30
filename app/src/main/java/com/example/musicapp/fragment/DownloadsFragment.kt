package com.example.myapplication.fragment

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.View.S4Activity
import com.example.myapplication.adapter.SongAdapter
import com.example.myapplication.databinding.FragmentSongBinding
import com.example.myapplication.model.Song
import com.example.myapplication.utils.RecyclerItemClickListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.io.File

class DownloadsFragment : Fragment() {

    private var _binding: FragmentSongBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: SongAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private var songList: MutableList<Song> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSongBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = SongAdapter(songList) { song ->
            val index = songList.indexOfFirst { it.id == song.id }
            if (index != -1) {
                val intent = Intent(requireContext(), S4Activity::class.java).apply {
                    putExtra("song_id", song.id)
                    putExtra("song_title", song.title)
                    putExtra("song_image", song.image)
                    putExtra("song_url", song.url)
                    putParcelableArrayListExtra("song_list", ArrayList(songList))
                    putExtra("current_index", index)
                    putExtra("source", "downloads")
                }
                startActivity(intent)
            }
        }

        binding.recommendationRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recommendationRecycler.adapter = adapter

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://appmusicrealtime-default-rtdb.asia-southeast1.firebasedatabase.app").reference

        loadDownloadedSongs()

        binding.recommendationRecycler.addOnItemTouchListener(
            RecyclerItemClickListener(requireContext(), binding.recommendationRecycler,
                object : RecyclerItemClickListener.OnItemClickListener {
                    override fun onItemClick(view: View?, position: Int) {
                        val song = songList[position]
                        val intent = Intent(requireContext(), S4Activity::class.java).apply {
                            putExtra("song_list", ArrayList(songList))
                            putExtra("current_index", position)
                            putExtra("source", "downloads")
                        }
                        startActivity(intent)
                    }

                    override fun onLongItemClick(view: View?, position: Int) {
                        showDeleteConfirmation(songList[position])
                    }
                })
        )
    }

    private fun loadDownloadedSongs() {
        val uid = auth.currentUser?.uid ?: return
        val downRef = database.child("users").child(uid).child("downloads")

        downRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Song>()
                for (child in snapshot.children) {
                    val id = child.key ?: continue
                    val title = child.child("title").getValue(String::class.java) ?: continue
                    val image = child.child("image").getValue(String::class.java) ?: ""
                    val localPath = child.child("localPath").getValue(String::class.java) ?: ""
                    val duration = child.child("duration").getValue(Int::class.java) ?: 0

                    val artistNames = child.child("artistNames").children.mapNotNull {
                        it.getValue(String::class.java)
                    }

                    val file = File(requireContext().getExternalFilesDir(null), "DownloadedSongs/${id}.mp3")
                    if (file.exists()) {
                        val song = Song(
                            id = id,
                            title = title,
                            image = image,
                            url = file.absolutePath,
                            duration = duration,
                            artistNames = artistNames,
                            isDownloaded = true
                        )
                        list.add(song)
                    }
                }

                songList.clear()
                songList.addAll(list)
                adapter.updateList(songList)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Lỗi khi tải dữ liệu", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showDeleteConfirmation(song: Song) {
        AlertDialog.Builder(requireContext())
            .setTitle("Xoá bài hát")
            .setMessage("Bạn có chắc muốn xoá bài hát này khỏi danh sách tải?")
            .setPositiveButton("Xoá") { _, _ -> removeDownloadedSong(song) }
            .setNegativeButton("Huỷ", null)
            .show()
    }

    private fun removeDownloadedSong(song: Song) {
        val uid = auth.currentUser?.uid ?: return

        val file = File(requireContext().getExternalFilesDir(null), "DownloadedSongs/${song.id}.mp3")
        if (file.exists()) file.delete()

        database.child("users").child(uid).child("downloads").child(song.id).removeValue()

        songList.remove(song)
        adapter.updateList(songList)

        Toast.makeText(requireContext(), "Đã xoá khỏi danh sách tải", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}