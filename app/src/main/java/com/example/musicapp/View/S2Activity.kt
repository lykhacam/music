package com.example.myapplication.View

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityScreen2Binding
import com.example.myapplication.fragment.RecentFragment
import com.google.android.material.tabs.TabLayoutMediator
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.fragment.app.Fragment
import com.example.myapplication.fragment.*

class S2Activity : AppCompatActivity() {
    private lateinit var binding: ActivityScreen2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScreen2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        val fragments = listOf(
            RecentFragment(),
            TopFragment(),
            ChillFragment(),
            RnbFragment(),
            FestivalFragment()
        )

        val titles = listOf("Recent", "Top 50", "Chill", "R&B", "Festival")

        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = fragments.size
            override fun createFragment(position: Int): Fragment = fragments[position]
        }

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = titles[position]
        }.attach()
    }
}
