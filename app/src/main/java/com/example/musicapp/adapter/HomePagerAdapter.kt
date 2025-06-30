// HomePagerAdapter.kt
package com.example.myapplication.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.myapplication.fragment.ExploreFragment
import com.example.myapplication.fragment.SuggestedFragment
import com.example.myapplication.fragment.Top50Fragment

class HomePagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 3
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> SuggestedFragment()
            1 -> Top50Fragment()
            2 -> ExploreFragment()
            else -> throw IllegalArgumentException("Invalid position")
        }
    }
}
