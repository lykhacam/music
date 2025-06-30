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
import com.example.myapplication.databinding.FragmentSongBinding
import com.example.myapplication.model.Song
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class RecentFragment : Fragment() {

    private var _binding: FragmentSongBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: SongAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private val songList = mutableListOf<Song>()

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
                    putParcelableArrayListExtra("song_list", ArrayList(songList))
                    putExtra("current_index", index)
                    putExtra("source", "recent")
                }
                startActivity(intent)
            }
        }

        binding.recommendationRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recommendationRecycler.adapter = adapter

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://appmusicrealtime-default-rtdb.asia-southeast1.firebasedatabase.app").reference

        val uid = auth.currentUser?.uid
        if (uid == null) {
            Log.w("RecentFragment", "⚠️ Chưa đăng nhập, không lấy được UID")
            return
        }

        Log.d("RecentFragment", "👤 UID hiện tại: $uid")

        // ✅ TEST: kiểm tra chính xác node recentlyPlayed
        val testRef = database.child("users").child(uid).child("recentlyPlayed")
        val fullPath = "users/$uid/recentlyPlayed"
        Log.d("TEST", "🧭 Đường dẫn đang đọc: $fullPath")

        testRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("TEST", "✅ snapshot.exists() = ${snapshot.exists()}")
                Log.d("TEST", "🔢 snapshot.childrenCount = ${snapshot.childrenCount}")
                snapshot.children.forEach {
                    Log.d("TEST", "🎵 ${it.key} = ${it.value}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("TEST", "❌ Lỗi đọc recentlyPlayed: ${error.message}")
            }
        })

        // ✅ Load thực tế
        val recentRef = database.child("users").child(uid).child("recentlyPlayed")
        recentRef.orderByValue().limitToLast(20).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("RecentFragment", "🔥 snapshot.childrenCount = ${snapshot.childrenCount}")
                if (!snapshot.exists()) {
                    Log.w("RecentFragment", "⚠️ Không có node recentlyPlayed")
                    adapter.updateList(emptyList())
                    return
                }

                val sortedIds = snapshot.children.mapNotNull {
                    val id = it.key
                    val value = it.getValue(Long::class.java)
                    Log.d("RecentFragment", "📍 $id = $value")
                    if (id != null && value != null && id != "placeholder") id to value else null
                }.sortedByDescending { it.second }
                    .map { it.first }

                Log.d("RecentFragment", "🔁 Đang tải các bài: $sortedIds")
                loadSongsByIds(sortedIds)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("RecentFragment", "❌ Lỗi đọc recentlyPlayed: ${error.message}")
            }
        })
    }

    private fun loadSongsByIds(ids: List<String>) {
        val songsRef = database.child("songs")
        val result = mutableListOf<Song>()
        var loadedCount = 0

        if (ids.isEmpty()) {
            adapter.updateList(emptyList())
            return
        }

        for (id in ids) {
            Log.d("RecentFragment", "📥 Đọc bài hát ID: $id")
            songsRef.child(id).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val song = snapshot.getValue(Song::class.java)
                    if (song != null) {
                        result.add(song)
                        Log.d("RecentFragment", "✅ Tìm thấy bài $id: ${song.title}")
                    } else {
                        Log.w("RecentFragment", "⚠️ Không ánh xạ được bài hát ID: $id")
                    }

                    loadedCount++
                    if (loadedCount == ids.size) {
                        songList.clear()
                        songList.addAll(ids.mapNotNull { id -> result.find { it.id == id } })
                        adapter.updateList(songList)
                        Log.d("RecentFragment", "🎧 Tổng số bài đã tải: ${songList.size}")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("RecentFragment", "❌ Lỗi đọc bài hát $id: ${error.message}")
                    loadedCount++
                    if (loadedCount == ids.size) {
                        songList.clear()
                        songList.addAll(ids.mapNotNull { id -> result.find { it.id == id } })
                        adapter.updateList(songList)
                    }
                }
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
