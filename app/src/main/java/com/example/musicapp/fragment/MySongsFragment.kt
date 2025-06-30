package com.example.myapplication.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.View.S4Activity
import com.example.myapplication.adapter.SongAdapter
import com.example.myapplication.databinding.FragmentMySongBinding
import com.example.myapplication.model.Song
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MySongsFragment : Fragment() {

    private var _binding: FragmentMySongBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: SongAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private val songList = mutableListOf<Song>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMySongBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = SongAdapter(
            songList
        ) { song ->
            val index = songList.indexOfFirst { it.id == song.id }
            if (index != -1) {
                val intent = Intent(requireContext(), S4Activity::class.java).apply {
                    putParcelableArrayListExtra("song_list", ArrayList(songList))
                    putExtra("current_index", index)
                    putExtra("source", "my_songs")
                }
                startActivity(intent)
            }
        }

// 👉 Gán callback xoá bài hát bằng setOnDeleteListener()
        adapter.setOnDeleteListener { song ->
            val uid = auth.currentUser?.uid ?: return@setOnDeleteListener
            database.child("users").child(uid).child("mySongs").child(song.id)
                .removeValue()
                .addOnSuccessListener {
                    songList.remove(song)
                    adapter.notifyDataSetChanged()
                    Toast.makeText(context, "Đã xoá bài hát", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Lỗi khi xoá bài hát", Toast.LENGTH_SHORT).show()
                }
        }


        binding.recommendationRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recommendationRecycler.adapter = adapter

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://appmusicrealtime-default-rtdb.asia-southeast1.firebasedatabase.app").reference

        val uid = auth.currentUser?.uid ?: return
        val mySongsRef = database.child("users").child(uid).child("mySongs")

        mySongsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                songList.clear()
                for (child in snapshot.children) {
                    val song = child.getValue(Song::class.java)
                    if (song != null) {
                        songList.add(song)
                    }
                }
                adapter.updateList(songList)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
