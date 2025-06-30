package com.example.myapplication.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.myapplication.fragment.DownloadsFragment
import com.example.myapplication.fragment.FavoritesFragment
import com.example.myapplication.fragment.RecentFragment

class LibraryPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 3
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FavoritesFragment()
            1 -> RecentFragment()
            2 -> DownloadsFragment()
            else -> throw IllegalArgumentException("Invalid position")
        }
    }
}
