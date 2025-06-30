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
import com.example.myapplication.fragment.*
import com.example.myapplication.global.GlobalStorage
import com.example.myapplication.service.MusicService
import com.example.myapplication.viewmodel.MiniPlayerViewModel
import com.example.myapplication.viewmodel.MiniPlayerViewModelFactory
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var miniPlayerViewModel: MiniPlayerViewModel
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toolbar: MaterialToolbar
    private lateinit var navigationView: NavigationView

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                "ACTION_MINIPLAYER_START" -> miniPlayerViewModel.setHasStartedPlaying(true)
                "ACTION_STOP_MINI_PLAYER" -> miniPlayerViewModel.setHasStartedPlaying(false)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        drawerLayout = binding.drawerLayout
        toolbar = binding.toolbar
        navigationView = binding.navigationView

        val headerView = navigationView.getHeaderView(0)
        val emailTextView = headerView.findViewById<TextView>(R.id.userEmail)
        emailTextView.text = FirebaseAuth.getInstance().currentUser?.email ?: "Guest"

        val isGuest = FirebaseAuth.getInstance().currentUser == null

        if (isGuest) {
            // Nếu là khách, dùng menu_guest.xml
            navigationView.menu.clear()
            navigationView.inflateMenu(R.menu.menu_guest)
        } else {
            // Nếu đã đăng nhập, giữ nguyên menu_user.xml (mặc định bạn đã gắn trong layout)
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            val userRef = FirebaseDatabase.getInstance("https://appmusicrealtime-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users/$uid/role")

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val role = snapshot.getValue(String::class.java)
                    val menu = navigationView.menu
                    menu.setGroupVisible(R.id.group_admin, role == "admin")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("MainActivity", "Lỗi role: ${error.message}")
                }
            })
        }


        // Kiểm tra role
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val userRef = FirebaseDatabase.getInstance("https://appmusicrealtime-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("users/$uid/role")

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val role = snapshot.getValue(String::class.java)
                val menu = navigationView.menu
                menu.setGroupVisible(R.id.group_admin, role == "admin")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MainActivity", "Lỗi role: ${error.message}")
            }
        })

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, HomeFragment())
            .commit()

        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> replaceFragment(HomeFragment())
                R.id.nav_library -> replaceFragment(LibraryFragment())
                R.id.nav_upload -> replaceFragment(UploadSongFragment())
                R.id.nav_contact -> replaceFragment(ContactFragment())
                R.id.nav_change_password -> replaceFragment(ChangePasswordFragment())
                R.id.nav_approve -> replaceFragment(AdminApproveFragment())
                R.id.nav_manage_data -> replaceFragment(ManageDataFragment())
                R.id.nav_sign_out -> showSignOutConfirmation()
                R.id.nav_my_songs -> replaceFragment(MySongsFragment())

                // 👇 Dành cho khách chưa đăng nhập
                R.id.nav_login -> {
                    startActivity(Intent(this, SignInActivity::class.java))
                    finish()
                }
                R.id.nav_register -> {
                    startActivity(Intent(this, SignUpActivity::class.java))
                    finish()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }


        miniPlayerViewModel = ViewModelProvider(
            applicationContext as ViewModelStoreOwner,
            MiniPlayerViewModelFactory(application)
        )[MiniPlayerViewModel::class.java]

        supportFragmentManager.beginTransaction()
            .replace(binding.miniPlayerContainer.id, MiniPlayerFragment())
            .commit()

        miniPlayerViewModel.hasStartedPlaying.observe(this) { started ->
            binding.miniPlayerContainer.visibility = if (started) View.VISIBLE else View.GONE
        }

        registerMiniPlayerBroadcasts()
        requestNotificationPermissionIfNeeded()
    }

    private fun replaceFragment(fragment: androidx.fragment.app.Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun signOut() {
        // Gửi Broadcast để dừng MiniPlayer và MusicService
        sendBroadcast(Intent("ACTION_STOP_MINI_PLAYER"))

        // Dừng hẳn MusicService nếu đang chạy
        stopService(Intent(this, MusicService::class.java))

        // Reset GlobalStorage (tuỳ chọn nếu bạn dùng biến toàn cục để lưu trạng thái nhạc)
        GlobalStorage.currentSongList = emptyList()
        GlobalStorage.currentSongIndex = -1

        // Thoát Firebase
        FirebaseAuth.getInstance().signOut()

        // Chuyển về màn hình đăng nhập và xoá back stack
        startActivity(Intent(this, SignInActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }


    private fun requestNotificationPermissionIfNeeded() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
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

    override fun onStart() {
        super.onStart()
        val hasSongData = GlobalStorage.currentSongList.isNotEmpty() &&
                GlobalStorage.currentSongIndex in GlobalStorage.currentSongList.indices
        if (!hasSongData) {
            miniPlayerViewModel.setHasStartedPlaying(false)
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun showSignOutConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Xác nhận đăng xuất")
            .setMessage("Bạn có chắc chắn muốn đăng xuất không?")
            .setPositiveButton("Đăng xuất") { _, _ ->
                signOut()  // Gọi hàm signOut thực sự
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcastReceiver)
    }
}
