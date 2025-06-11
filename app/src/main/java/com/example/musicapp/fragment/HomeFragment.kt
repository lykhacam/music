package com.example.myapplication.fragment

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.View.S4Activity
import com.example.myapplication.adapter.HomePagerAdapter
import com.example.myapplication.adapter.SongAdapter
import com.example.myapplication.databinding.FragmentHomeBinding
import com.example.myapplication.model.Song
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.database.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val tabTitles = listOf("Gợi ý", "Top 50", "Khám phá")

    private val allSongs = mutableListOf<Song>()
    private lateinit var searchAdapter: SongAdapter

    private var lastKey: String? = null
    private val batchSize = 10
    private var isLoading = false
    private var hasMore = true  // ✅ Biến cờ: còn bài để load không?
    private lateinit var dbRef: DatabaseReference

    private var isViewActive = false  // Kiểm tra binding còn sống

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        isViewActive = true
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = HomePagerAdapter(this)
        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()

        dbRef = FirebaseDatabase
            .getInstance("https://appmusicrealtime-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("songs")

        setupSearch()
        loadSongsFromFirebase()
    }

    private fun setupSearch() {
        searchAdapter = SongAdapter(emptyList()) { song ->
            val intent = Intent(requireContext(), S4Activity::class.java).apply {
                putParcelableArrayListExtra("song_list", ArrayList(allSongs))
                putExtra("current_index", allSongs.indexOf(song))
                putExtra("source", "search")
            }
            startActivity(intent)
        }

        binding.searchResultRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.searchResultRecycler.adapter = searchAdapter

        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!isViewActive) return
                val query = s.toString().trim().lowercase()

                val isSearching = query.isNotEmpty()
                binding.searchResultRecycler.isVisible = isSearching
                binding.viewPager.isVisible = !isSearching
                binding.tabLayout.isVisible = !isSearching

                if (isSearching) {
                    val result = allSongs.filter {
                        it.title.lowercase().contains(query) ||
                                it.artistNames.joinToString(",").lowercase().contains(query)
                    }
                    Log.d("Search", "🎯 Tìm thấy ${result.size} bài khớp với \"$query\"")
                    searchAdapter.updateList(result)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.searchResultRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!isViewActive) return
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                val totalItemCount = layoutManager.itemCount

                if (!isLoading && hasMore && lastVisibleItem >= totalItemCount - 2) {
                    Log.d("LazyLoad", "📥 Gần cuối danh sách, bắt đầu load thêm...")
                    loadSongsFromFirebase()
                }
            }
        })
    }

    private fun loadSongsFromFirebase() {
        if (isLoading || !isViewActive || !hasMore) return
        isLoading = true
        Log.d("LazyLoad", "🚀 Bắt đầu load batch mới...")

        var query: Query = dbRef.orderByKey().limitToFirst(batchSize + 1)
        if (lastKey != null) {
            query = dbRef.orderByKey().startAfter(lastKey).limitToFirst(batchSize + 1)
        }

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isViewActive) return

                val newSongs = mutableListOf<Song>()
                val children = snapshot.children.toList()
                Log.d("LazyLoad", "📦 Firebase trả về ${children.size} bài")

                // Nếu < batchSize + 1 thì đã hết
                if (children.size <= batchSize) {
                    hasMore = false
                    Log.d("LazyLoad", "⛔ Không còn bài mới để load thêm")
                }

                var count = 0
                for (child in children) {
                    val song = child.getValue(Song::class.java)
                    if (song != null && count < batchSize) {
                        newSongs.add(song)
                        lastKey = child.key
                        count++
                    }
                }

                allSongs.addAll(newSongs)
                Log.d("LazyLoad", "✅ Đã load ${newSongs.size} bài (tổng: ${allSongs.size})")

                val currentQuery = binding.searchBar.text.toString().trim().lowercase()
                if (currentQuery.isNotEmpty()) {
                    val result = allSongs.filter {
                        it.title.lowercase().contains(currentQuery) ||
                                it.artistNames.joinToString(",").lowercase().contains(currentQuery)
                    }
                    searchAdapter.updateList(result)
                }

                isLoading = false
            }

            override fun onCancelled(error: DatabaseError) {
                isLoading = false
                Log.e("Firebase", "❌ Lỗi khi load bài: ${error.message}")
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        isViewActive = false
    }
}
