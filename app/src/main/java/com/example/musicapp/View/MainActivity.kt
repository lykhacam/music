package com.example.myapplication.View

import android.content.*
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.fragment.FavoritesFragment
import com.example.myapplication.fragment.HomeFragment
import com.example.myapplication.fragment.LibraryFragment
import com.example.myapplication.fragment.MiniPlayerFragment
import com.example.myapplication.viewmodel.MiniPlayerViewModel
import com.example.myapplication.viewmodel.MiniPlayerViewModelFactory
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var miniPlayerViewModel: MiniPlayerViewModel
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toolbar: MaterialToolbar
    private lateinit var navigationView: NavigationView

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                "ACTION_MINIPLAYER_START" -> {
                    Log.d("MainActivity", "Received ACTION_MINIPLAYER_START")
                    miniPlayerViewModel.setHasStartedPlaying(true)
                }
                "ACTION_STOP_MINI_PLAYER" -> {
                    Log.d("MainActivity", "Received ACTION_STOP_MINI_PLAYER")
                    miniPlayerViewModel.setHasStartedPlaying(false)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val headerView = binding.navigationView.getHeaderView(0)
        val emailTextView = headerView.findViewById<TextView>(R.id.userEmail)
        emailTextView.text = FirebaseAuth.getInstance().currentUser?.email ?: "Guest"

//        ADMIN thif có quyền kiểm duyệt bài hát upload từ user
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val userRef = FirebaseDatabase.getInstance("https://appmusicrealtime-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("users/$uid/role")

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val role = snapshot.getValue(String::class.java)
                if (role == "admin") {
                    navigationView.menu.findItem(R.id.nav_approve)?.isVisible = true
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MainActivity", "Lỗi kiểm tra role: ${error.message}")
            }
        })
//kiểm duyệt end
        drawerLayout = findViewById(R.id.drawerLayout)
        toolbar = findViewById(R.id.toolbar)
        navigationView = findViewById(R.id.navigationView)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, HomeFragment())
            .commit()

        navigationView.setNavigationItemSelectedListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, HomeFragment())
                        .commit()
                }
                R.id.nav_library -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, LibraryFragment())
                        .commit()
                }
                R.id.nav_favorite -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, FavoritesFragment())
                        .addToBackStack(null)
                        .commit()
                }

                R.id.nav_upload -> {
                    startActivity(Intent(this, UploadSongActivity::class.java))
                }
                R.id.nav_approve -> {
                    startActivity(Intent(this, AdminApproveActivity::class.java))
                }

                R.id.nav_change_password -> {
                    val intent = Intent(this, ChangePasswordActivity::class.java)
                    startActivity(intent)
                }

                R.id.nav_sign_out -> {
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(this, SignInActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // ✅ Dùng Application scope để đồng bộ với Service/Fragment
        miniPlayerViewModel = ViewModelProvider(
            applicationContext as ViewModelStoreOwner,
            MiniPlayerViewModelFactory(application)
        )[MiniPlayerViewModel::class.java]

        supportFragmentManager.beginTransaction()
            .replace(binding.miniPlayerContainer.id, MiniPlayerFragment())
            .commit()

        miniPlayerViewModel.hasStartedPlaying.observe(this) { started ->
            updateMiniPlayerVisibility(started)
        }

        val initialStarted = miniPlayerViewModel.hasStartedPlaying.value ?: false
        updateMiniPlayerVisibility(initialStarted)

        registerMiniPlayerBroadcasts()
        requestNotificationPermissionIfNeeded()
    }

    private fun updateMiniPlayerVisibility(started: Boolean) {
        binding.miniPlayerContainer.visibility = if (started) View.VISIBLE else View.GONE
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
    }

    private fun registerMiniPlayerBroadcasts() {
        val filter = IntentFilter().apply {
            addAction("ACTION_MINIPLAYER_START")
            addAction("ACTION_STOP_MINI_PLAYER")
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(broadcastReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(broadcastReceiver, filter)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcastReceiver)
    }
}
