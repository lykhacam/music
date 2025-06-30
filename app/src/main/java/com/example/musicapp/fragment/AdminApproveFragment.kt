package com.example.myapplication.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.adapter.SongApproveAdapter
import com.example.myapplication.model.Song
import com.google.firebase.database.*

class AdminApproveFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SongApproveAdapter
    private val pendingList = mutableListOf<Pair<String, Song>>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_approve, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerPendingSongs)
        adapter = SongApproveAdapter(
            pendingList,
            onApprove = { songId, song -> approveSong(songId, song) },
            onReject = { songId -> rejectSong(songId) }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        loadPendingSongs()
    }

    private fun loadPendingSongs() {
        val ref = FirebaseDatabase.getInstance("https://appmusicrealtime-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("pendingUploads")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                pendingList.clear()
                for (child in snapshot.children) {
                    val id = child.key ?: continue
                    val title = child.child("title").getValue(String::class.java) ?: ""
                    val artistNames = child.child("artistNames").children.mapNotNull { it.getValue(String::class.java) }
                    val categoryIds = child.child("categoryIds").children.mapNotNull { it.getValue(String::class.java) }
                    val moodIds = child.child("moodIds").children.mapNotNull { it.getValue(String::class.java) }
                    val suitableTimeIds = child.child("suitableTimeIds").children.mapNotNull { it.getValue(String::class.java) }
                    val image = child.child("image").getValue(String::class.java) ?: ""
                    val url = child.child("url").getValue(String::class.java) ?: ""
                    val duration = child.child("duration").getValue(Long::class.java)?.toInt() ?: 0
                    val count = child.child("count").getValue(Long::class.java)?.toInt() ?: 0

                    val song = Song(
                        id = id,
                        title = title,
                        artistNames = artistNames,
                        categoryIds = categoryIds,
                        moodIds = moodIds,
                        suitableTimeIds = suitableTimeIds,
                        image = image,
                        url = url,
                        duration = duration,
                        count = count
                    )
                    pendingList.add(id to song)
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun approveSong(songId: String, song: Song) {
        val db = FirebaseDatabase.getInstance("https://appmusicrealtime-default-rtdb.asia-southeast1.firebasedatabase.app")
        val songsRef = db.getReference("songs/$songId")
        val pendingRef = db.getReference("pendingUploads/$songId")

        val fullSongData = mapOf(
            "id" to songId,
            "title" to song.title,
            "artistNames" to (song.artistNames ?: listOf()),
            "categoryIds" to (song.categoryIds ?: listOf()),
            "moodIds" to (song.moodIds ?: listOf()),
            "suitableTimeIds" to (song.suitableTimeIds ?: listOf()),
            "duration" to (song.duration ?: 0),
            "image" to song.image.orEmpty(),
            "url" to song.url.orEmpty(),
            "count" to 0,
        )


        songsRef.setValue(fullSongData)
            .addOnSuccessListener {
                pendingRef.removeValue()
                Toast.makeText(requireContext(), "✅ Đã duyệt bài: ${song.title}", Toast.LENGTH_SHORT).show()
                loadPendingSongs()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "❌ Lỗi khi duyệt bài: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun rejectSong(songId: String) {
        val db = FirebaseDatabase.getInstance("https://appmusicrealtime-default-rtdb.asia-southeast1.firebasedatabase.app")
        db.getReference("pendingUploads/$songId").removeValue()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "❌ Đã từ chối bài hát", Toast.LENGTH_SHORT).show()
                loadPendingSongs()
            }
    }
}
