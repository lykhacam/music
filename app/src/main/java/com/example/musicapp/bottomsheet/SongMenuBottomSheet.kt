package com.example.myapplication.bottomsheet

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.R
import com.example.myapplication.databinding.BottomSheetSongMenuBinding
import com.example.myapplication.model.Song
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

class SongMenuBottomSheet(
    private val context: Context,
    private val song: Song,
    private val onLike: () -> Unit,
    private val onDownload: () -> Unit,
    private val onRemove: () -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetSongMenuBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetSongMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkDownloadStatusFromFirebase()
        checkFavoriteStatusFromFirebase()

        binding.btnLike.setOnClickListener {
            onLike()
            dismiss()
        }

        binding.btnDownload.setOnClickListener {
            val activity = requireActivity()
            if (checkAndRequestPermission(activity)) {
                if (song.isDownloaded) {
                    deleteDownloadedSong(song)
                } else {
                    downloadAndSave(song)
                }
            } else {
                Toast.makeText(activity, "Vui lòng cấp quyền để tải xuống", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkDownloadStatusFromFirebase() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance("https://appmusicrealtime-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("users/$uid/downloads/${song.id}")

        ref.get().addOnSuccessListener { snapshot ->
            song.isDownloaded = snapshot.exists()
            updateDownloadButtonUI()
        }
    }

    private fun updateDownloadButtonUI() {
        binding.tvDownloadLabel.text = if (song.isDownloaded) "Xoá khỏi danh sách tải" else "Tải xuống"
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

    private fun downloadAndSave(song: Song) {
        activity?.runOnUiThread {
            binding.tvDownloadLabel.text = "Đang tải..."
            binding.tvDownloadLabel.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))
            binding.btnDownload.isEnabled = false
        }

        Thread {
            try {
                val client = OkHttpClient()
                val request = Request.Builder().url(song.url).build()
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) throw Exception("Lỗi tải file")

                val inputStream = response.body?.byteStream() ?: throw Exception("Stream null")
                val dir = File(context.getExternalFilesDir(null), "DownloadedSongs")
                if (!dir.exists()) dir.mkdirs()

                val file = File(dir, "${song.id}.mp3")
                val output = FileOutputStream(file)
                inputStream.copyTo(output)
                output.close()

                saveDownloadToFirebase(song, file.absolutePath)

                activity?.runOnUiThread {
                    Toast.makeText(context, "Đã tải xong bài hát", Toast.LENGTH_SHORT).show()
                    song.isDownloaded = true
                    updateDownloadButtonUI()
                    binding.tvDownloadLabel.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                    binding.btnDownload.isEnabled = true
                    onDownload()
                    dismiss()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                activity?.runOnUiThread {
                    Toast.makeText(context, "Tải bài hát thất bại", Toast.LENGTH_SHORT).show()
                    binding.tvDownloadLabel.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                    binding.btnDownload.isEnabled = true
                }
            }
        }.start()
    }

    private fun deleteDownloadedSong(song: Song) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val file = File(context.getExternalFilesDir(null), "DownloadedSongs/${song.id}.mp3")
        if (file.exists()) file.delete()

        val ref = FirebaseDatabase.getInstance("https://appmusicrealtime-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("users/$uid/downloads/${song.id}")
        ref.removeValue()

        song.isDownloaded = false
        updateDownloadButtonUI()
        onRemove()

        Toast.makeText(context, "Đã xoá khỏi danh sách tải", Toast.LENGTH_SHORT).show()
        dismiss()
    }

    private fun saveDownloadToFirebase(song: Song, localPath: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance("https://appmusicrealtime-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("users/$uid/downloads/${song.id}")

        val map = mapOf(
            "title" to song.title,
            "image" to song.image,
            "artistNames" to song.artistNames,
            "duration" to song.duration,
            "localPath" to localPath
        )

        ref.setValue(map)
    }

    private fun checkFavoriteStatusFromFirebase() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance("https://appmusicrealtime-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("users/$uid/favorites/${song.id}")

        ref.get().addOnSuccessListener { snapshot ->
            song.isLiked = snapshot.exists()
            updateLikeButtonUI()
        }
    }

    private fun updateLikeButtonUI() {
        binding.tvLikeLabel.text = if (song.isLiked) "Bỏ thích bài hát" else "Thích bài hát"
        binding.tvLikeLabel.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                android.R.color.white
            )
        )
        binding.ivLikeIcon.setImageResource(
            if (song.isLiked) R.drawable.ic_heart_full else R.drawable.ic_heart_image
        )
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}