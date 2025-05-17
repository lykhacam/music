package com.example.myapplication.fragment

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.adapter.UserPlaylistAdapter
import com.example.myapplication.databinding.FragmentPlaylistBinding
import com.example.myapplication.model.UserPlaylist
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class PlaylistFragment : Fragment() {

    private var _binding: FragmentPlaylistBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: UserPlaylistAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = UserPlaylistAdapter(emptyList())
        binding.recyclerPlaylists.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerPlaylists.adapter = adapter

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase
            .getInstance("https://appmusicrealtime-default-rtdb.asia-southeast1.firebasedatabase.app")
            .reference

        val uid = auth.currentUser?.uid ?: return
        database.child("users").child(uid).child("playlists")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val result = mutableListOf<UserPlaylist>()
                    for (child in snapshot.children) {
                        val name = child.child("name").getValue(String::class.java) ?: continue
                        val songIds = child.child("songIds").children.mapNotNull { it.key }
                        result.add(UserPlaylist(name, songIds))
                    }
                    adapter.updatePlaylists(result)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
