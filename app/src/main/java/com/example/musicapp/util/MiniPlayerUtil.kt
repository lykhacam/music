package com.example.myapplication.util

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.myapplication.R
import com.example.myapplication.fragment.MiniPlayerFragment

fun AppCompatActivity.attachMiniPlayer(containerId: Int) {
    val fragmentManager = supportFragmentManager
    val existingFragment = fragmentManager.findFragmentById(containerId)
    if (existingFragment == null) {
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(containerId, MiniPlayerFragment())
        transaction.commit()
    }
}


fun Fragment.attachMiniPlayer(containerId: Int = R.id.miniPlayerContainer) {
    childFragmentManager.beginTransaction()
        .replace(containerId, MiniPlayerFragment())
        .commitAllowingStateLoss()
}
