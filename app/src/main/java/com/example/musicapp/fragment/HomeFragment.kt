package com.example.myapplication.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.View.S4Activity
import com.example.myapplication.adapter.HomePagerAdapter
import com.example.myapplication.adapter.SongAdapter
import com.example.myapplication.databinding.FragmentHomeBinding
import com.example.myapplication.model.Song
import com.google.android.material.tabs.TabLayoutMediator
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val tabTitles = listOf("Gợi ý", "Top 100", "Khám phá")
    private lateinit var searchAdapter: SongAdapter
    private var isViewActive = false
    private var currentSearchResults: List<Song> = emptyList()

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

        setupSearch()
    }

    private fun setupSearch() {
        searchAdapter = SongAdapter(emptyList()) { song ->
            val intent = Intent(requireContext(), S4Activity::class.java).apply {
                putParcelableArrayListExtra("song_list", ArrayList(currentSearchResults))
                putExtra("current_index", currentSearchResults.indexOf(song))
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
                    searchFromCloudFunction(query)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.searchBar.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || event?.keyCode == KeyEvent.KEYCODE_ENTER) {
                // Ẩn bàn phím
                val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(v.windowToken, 0)
                true
            } else {
                false
            }
        }

    }

    private fun searchFromCloudFunction(query: String) {
        val url = "https://asia-southeast1-appmusicrealtime.cloudfunctions.net/searchSongs?q=$query"

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("SearchAPI", "❌ Failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { json ->
                    val jsonArray = JSONArray(json)
                    val results = mutableListOf<Song>()
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val artist = when (val artists = obj.opt("artistNames")) {
                            is JSONArray -> List(artists.length()) { artists.getString(it) }
                            is String -> listOf(artists)
                            else -> emptyList()
                        }
                        val song = Song(
                            id = obj.getString("id"),
                            title = obj.optString("title"),
                            artistNames = artist,
                            url = obj.optString("url"),
                            image = obj.optString("image"),
                            categoryIds = obj.optJSONArray("categoryIds")?.let { arr ->
                                List(arr.length()) { arr.getString(it) }
                            } ?: emptyList(),
                            playlistIds = obj.optJSONArray("playlistIds")?.let { arr ->
                                List(arr.length()) { arr.getString(it) }
                            } ?: emptyList(),
                            moodIds = obj.optJSONArray("moodIds")?.let { arr ->
                                List(arr.length()) { arr.getString(it) }
                            } ?: emptyList(),
                            suitableTimeIds = obj.optJSONArray("suitableTimeIds")?.let { arr ->
                                List(arr.length()) { arr.getString(it) }
                            } ?: emptyList(),
                            count = obj.optInt("count"),
                            duration = obj.optInt("duration")
                        )
                        results.add(song)
                    }
                    activity?.runOnUiThread {
                        currentSearchResults = results
                        searchAdapter.updateList(results)
                    }
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        isViewActive = false
    }
}
