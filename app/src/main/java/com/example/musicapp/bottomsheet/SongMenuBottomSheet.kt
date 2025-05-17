package com.example.myapplication.bottomsheet

import android.Manifest
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.databinding.BottomSheetSongMenuBinding
import com.example.myapplication.model.Song
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class SongMenuBottomSheet(
    private val context: Context,
    private val song: Song,
    private val onLike: () -> Unit,
    private val onDownload: () -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetSongMenuBinding? = null
    private val binding get() = _binding!!

    private var isDownloaded = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetSongMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkIfSongDownloaded()

        binding.btnLike.setOnClickListener {
            onLike()
            dismiss()
        }

        binding.btnDownload.setOnClickListener {
            val activity = requireActivity()
            if (checkAndRequestPermission(activity)) {
                downloadSongToDevice(context, song.url, song.title)
                song.isDownloaded = true
                addToDownloads(song.id)
                onDownload()
                Toast.makeText(context, "Đang tải bài hát...", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(activity, "Vui lòng cấp quyền để tải xuống", Toast.LENGTH_SHORT).show()
            }
            dismiss()
        }

        binding.btnShare.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Nghe thử bài hát này!")
                putExtra(Intent.EXTRA_TEXT, "${song.title} - ${song.artistNames.joinToString()}\n${song.url}")
            }
            startActivity(Intent.createChooser(intent, "Chia sẻ qua"))
        }

        binding.btnAddToPlaylist.setOnClickListener {
            Toast.makeText(context, "Đã thêm vào danh sách phát", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }

    private fun checkIfSongDownloaded() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance("https://appmusicrealtime-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("users").child(uid).child("downloads").child(song.id)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                isDownloaded = snapshot.exists()
                updateDownloadButton()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun updateDownloadButton() {
        val tvLabel = binding.tvDownloadLabel
        val btn = binding.btnDownload

        if (isDownloaded) {
            tvLabel.text = "Đã tải"
            btn.isEnabled = false
            btn.alpha = 0.5f
        } else {
            tvLabel.text = "Tải xuống"
            btn.isEnabled = true
            btn.alpha = 1.0f
        }
    }


    private fun downloadSongToDevice(context: Context, songUrl: String, songTitle: String) {
        val request = DownloadManager.Request(Uri.parse(songUrl)).apply {
            setTitle("Đang tải: $songTitle")
            setDescription("Tải bài hát xuống thiết bị")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "$songTitle.mp3")
            setAllowedOverMetered(true)
            setAllowedOverRoaming(true)
        }
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
    }

    private fun checkAndRequestPermission(activity: Activity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            true
        } else {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1001
                )
                false
            } else {
                true
            }
        }
    }

    private fun addToDownloads(songId: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance("https://appmusicrealtime-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("users").child(uid).child("downloads")
        ref.child(songId).setValue(true)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
