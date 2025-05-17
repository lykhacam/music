package com.example.myapplication.View

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.myapplication.databinding.ActivityScreen2Binding
import com.example.myapplication.fragment.*
import com.example.myapplication.viewmodel.MiniPlayerViewModel
import com.example.myapplication.viewmodel.MiniPlayerViewModelFactory
import com.google.android.material.tabs.TabLayoutMediator

class S2Activity : AppCompatActivity() {

    private lateinit var binding: ActivityScreen2Binding
    private lateinit var miniPlayerViewModel: MiniPlayerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScreen2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // Lấy ViewModel từ Application Scope
        miniPlayerViewModel = ViewModelProvider(
            application as androidx.lifecycle.ViewModelStoreOwner,
            MiniPlayerViewModelFactory(application)
        ).get(MiniPlayerViewModel::class.java)

        // Gắn MiniPlayerFragment vào container
        supportFragmentManager.beginTransaction()
            .replace(binding.miniPlayerContainer.id, MiniPlayerFragment())
            .commit()

        // Quan sát khi phát nhạc
        miniPlayerViewModel.hasStartedPlaying.observe(this) { started ->
            Log.d("MiniPlayer", "S2Activity observe: hasStartedPlaying = $started")
            updateMiniPlayerVisibility(started)
        }

        // Kiểm tra ngay giá trị hiện tại lúc vào màn
        val initialStarted = miniPlayerViewModel.hasStartedPlaying.value ?: false
        Log.d("MiniPlayer", "S2Activity initial: hasStartedPlaying = $initialStarted")
        updateMiniPlayerVisibility(initialStarted)

        // Setup ViewPager2 + TabLayout
        val fragments = listOf(
            RecentFragment(),
            TopFragment(),
            ChillFragment()
        )

        val titles = listOf("Recent", "Top 50", "Chill")

        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = fragments.size
            override fun createFragment(position: Int): Fragment = fragments[position]
        }

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = titles[position]
        }.attach()
    }

    private fun updateMiniPlayerVisibility(started: Boolean) {
        binding.miniPlayerContainer.visibility = if (started) {
            Log.d("MiniPlayer", "MiniPlayer hiện")
            android.view.View.VISIBLE
        } else {
            Log.d("MiniPlayer", "MiniPlayer ẩn")
            android.view.View.GONE
        }
    }
}
