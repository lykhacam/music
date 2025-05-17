package com.example.myapplication.fragment

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.View.S4Activity
import com.example.myapplication.View.SignInActivity
import com.example.myapplication.adapter.HomePagerAdapter
import com.example.myapplication.adapter.SongAdapter
import com.example.myapplication.databinding.FragmentHomeBinding
import com.example.myapplication.model.Song
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val tabTitles = listOf("Gợi ý", "Top 50", "Khám phá")
    private val allSongs = mutableListOf<Song>()
    private lateinit var searchAdapter: SongAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = HomePagerAdapter(this)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()

        setupSearch()
        loadSongsFromFirebase()

//        binding.welcomeText.setOnClickListener {
//            FirebaseAuth.getInstance().signOut()
//            Toast.makeText(requireContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show()
//
//            startActivity(Intent(requireContext(), SignInActivity::class.java))
//            requireActivity().finish()
//        }

    }

    private fun setupSearch() {
        searchAdapter = SongAdapter(emptyList()) { song ->
            val intent = Intent(requireContext(), S4Activity::class.java).apply {
                putParcelableArrayListExtra("song_list", ArrayList(allSongs))
                putExtra("current_index", allSongs.indexOf(song))

            }
            intent.putExtra("source", "search")
            startActivity(intent)
        }

        binding.searchResultRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.searchResultRecycler.adapter = searchAdapter

        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim().lowercase()
                Log.d("Search", "Query: $query")

                val isSearching = query.isNotEmpty()
                binding.searchResultRecycler.isVisible = isSearching
                binding.viewPager.isVisible = !isSearching
                binding.tabLayout.isVisible = !isSearching

                if (isSearching) {
                    val result = allSongs.filter {
                        it.title.lowercase().contains(query) ||
                                it.artistNames.joinToString(",").lowercase().contains(query)
                    }
                    searchAdapter.updateList(result)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun loadSongsFromFirebase() {
        val dbRef = FirebaseDatabase
            .getInstance("https://appmusicrealtime-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("songs")

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val songs = mutableListOf<Song>()
                for (child in snapshot.children) {
                    val song = child.getValue(Song::class.java)
                    if (song != null) {
                        songs.add(song)
                    }
                }
                allSongs.clear()
                allSongs.addAll(songs)
                Log.d("Firebase", "Loaded ${songs.size} songs")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Failed to load songs: ${error.message}")
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
