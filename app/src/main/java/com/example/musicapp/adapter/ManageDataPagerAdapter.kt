// ManageDataPagerAdapter.kt
package com.example.myapplication.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.myapplication.fragment.ManageCategoryFragment
import com.example.myapplication.fragment.ManageMoodFragment
import com.example.myapplication.fragment.ManageTimeFragment

class ManageDataPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 3 // số lượng tab

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ManageCategoryFragment()
            1 -> ManageMoodFragment()
            2 -> ManageTimeFragment()
            else -> throw IllegalArgumentException("Invalid tab position: $position")
        }
    }
}
