package com.example.myapplication.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.View.S4Activity
import com.example.myapplication.adapter.SongAdapter
import com.example.myapplication.databinding.FragmentSongListBinding
import com.example.myapplication.model.Song
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class RecentFragment : Fragment() {

    private var _binding: FragmentSongListBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: SongAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private var songList: List<Song> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSongListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = SongAdapter(emptyList()) { song ->
            val index = songList.indexOfFirst { it.id == song.id }
            if (index != -1) {
                val intent = Intent(requireContext(), S4Activity::class.java).apply {
                    putExtra("song_id", song.id)
                    putExtra("song_title", song.title)
                    putExtra("song_image", song.image)
                    putExtra("song_url", song.url)
                    putParcelableArrayListExtra("song_list", ArrayList(songList))
                    putExtra("current_index", index)
                    putExtra("source", "recent")
                }
                startActivity(intent)
            }
        }

        binding.favRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.favRecycler.adapter = adapter

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://appmusicrealtime-default-rtdb.asia-southeast1.firebasedatabase.app").reference

        val uid = auth.currentUser?.uid ?: return
        val recentRef = database.child("users").child(uid).child("recentlyPlayed")

        recentRef.orderByValue().limitToLast(20).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val sortedIds = snapshot.children.mapNotNull {
                    val id = it.key
                    val time = it.getValue(Long::class.java)
                    if (id != null && time != null && id != "placeholder") id to time else null
                }.sortedByDescending { it.second }.map { it.first }

                loadSongsByIds(sortedIds)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun loadSongsByIds(ids: List<String>) {
        val songsRef = database.child("songs")
        val result = mutableListOf<Song>()
        var count = 0

        for (id in ids) {
            songsRef.child(id).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue(Song::class.java)?.let { result.add(it) }
                    count++
                    if (count == ids.size) {
                        songList = result
                        adapter.updateList(songList)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    count++
                }
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}