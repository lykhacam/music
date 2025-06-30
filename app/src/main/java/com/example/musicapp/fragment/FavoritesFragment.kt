package com.example.myapplication.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.View.S4Activity
import com.example.myapplication.adapter.SongAdapter
import com.example.myapplication.databinding.FragmentSongBinding
import com.example.myapplication.model.Song
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class FavoritesFragment : Fragment() {

    private var _binding: FragmentSongBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: SongAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private val songList = mutableListOf<Song>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSongBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = SongAdapter(songList) { song ->
            val index = songList.indexOfFirst { it.id == song.id }
            if (index != -1) {
                val intent = Intent(requireContext(), S4Activity::class.java).apply {
                    putParcelableArrayListExtra("song_list", ArrayList(songList))
                    putExtra("current_index", index)
                    putExtra("source", "favorites")
                }
                startActivity(intent)
            }
        }

        binding.recommendationRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recommendationRecycler.adapter = adapter

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://appmusicrealtime-default-rtdb.asia-southeast1.firebasedatabase.app").reference

        val uid = auth.currentUser?.uid ?: return
        val favRef = database.child("users").child(uid).child("favorites")
        favRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ids = snapshot.children.mapNotNull { it.key }.filter { it != "placeholder" }
                loadSongsByIds(ids)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun loadSongsByIds(songIds: List<String>) {
        if (songIds.isEmpty()) {
            adapter.updateList(emptyList())
            return
        }

        val songsRef = database.child("songs")
        var count = 0

        for (id in songIds) {
            songsRef.child(id).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue(Song::class.java)?.let { song ->
                        song.isLiked = true // ✅ Gán trạng thái liked cho từng bài
                        songList.add(song)
                    }
                    count++
                    if (count == songIds.size) {
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
