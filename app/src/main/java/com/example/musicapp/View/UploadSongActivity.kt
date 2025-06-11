package com.example.myapplication.View

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*

class UploadSongActivity : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var etArtist: EditText
    private lateinit var etDuration: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var tvFileName: TextView
    private lateinit var btnSelectFile: Button
    private lateinit var btnSelectImage: Button
    private lateinit var btnUpload: Button

    private var selectedAudioUri: Uri? = null
    private var selectedImageUri: Uri? = null

    private lateinit var selectMp3Launcher: ActivityResultLauncher<Intent>
    private lateinit var selectImageLauncher: ActivityResultLauncher<Intent>

    private val categoryMap = mapOf(
        "Nhạc trẻ" to "nhac_tre",
        "Ballad" to "ballad",
        "Nhạc Trịnh" to "nhac_trinh",
        "EDM" to "edm",
        "Nhạc Nhật" to "nhac_nhat"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_song)

        etTitle = findViewById(R.id.etTitle)
        etArtist = findViewById(R.id.etArtist)
        etDuration = findViewById(R.id.etDuration)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        tvFileName = findViewById(R.id.tvFileName)
        btnSelectFile = findViewById(R.id.btnSelectFile)
        btnSelectImage = findViewById(R.id.btnSelectImage)
        btnUpload = findViewById(R.id.btnUpload)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryMap.keys.toList())
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter

        selectMp3Launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                selectedAudioUri = result.data?.data
                contentResolver.takePersistableUriPermission(
                    selectedAudioUri!!, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                tvFileName.text = selectedAudioUri?.lastPathSegment ?: "Đã chọn file"
            }
        }

        btnSelectFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "audio/mpeg"
            }
            selectMp3Launcher.launch(intent)
        }

        selectImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                selectedImageUri = result.data?.data
                contentResolver.takePersistableUriPermission(
                    selectedImageUri!!, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                Toast.makeText(this, "✅ Đã chọn ảnh", Toast.LENGTH_SHORT).show()
            }
        }

        btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
            }
            selectImageLauncher.launch(intent)
        }

        btnUpload.setOnClickListener {
            if (selectedAudioUri == null || etTitle.text.isBlank() || etArtist.text.isBlank()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin và chọn file", Toast.LENGTH_SHORT).show()
            } else {
                getNextSongId { newSongId ->
                    uploadAllAssets(newSongId)
                }
            }
        }
    }

    private fun uploadAllAssets(songId: String) {
        val storage = FirebaseStorage.getInstance()

        val imageFileName = "image_${UUID.randomUUID()}.jpg"
        val audioFileName = "audio_${UUID.randomUUID()}.mp3"

        val imageRef = storage.getReference("songs/$songId/$imageFileName")
        val audioRef = storage.getReference("songs/$songId/$audioFileName")

        if (selectedImageUri != null) {
            imageRef.putFile(selectedImageUri!!)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                        uploadAudioFile(songId, audioRef, imageUrl.toString())
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "❌ Lỗi upload ảnh", Toast.LENGTH_SHORT).show()
                }
        } else {
            uploadAudioFile(songId, audioRef, "")
        }
    }


    private fun uploadAudioFile(songId: String, audioRef: StorageReference, imageUrl: String) {
        audioRef.putFile(selectedAudioUri!!)
            .addOnSuccessListener {
                audioRef.downloadUrl.addOnSuccessListener { audioUrl ->
                    saveMetadata(songId, audioUrl.toString(), imageUrl)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "❌ Lỗi upload nhạc", Toast.LENGTH_SHORT).show()
            }
    }


    private fun saveMetadata(songId: String, audioUrl: String, imageUrl: String) {
        val db = FirebaseDatabase.getInstance("https://appmusicrealtime-default-rtdb.asia-southeast1.firebasedatabase.app")
        val ref = db.getReference("pendingUploads/$songId")
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val selectedCategoryName = spinnerCategory.selectedItem.toString()
        val categoryId = categoryMap[selectedCategoryName] ?: "unknown"
        val duration = etDuration.text.toString().toIntOrNull() ?: 0

        val songData = mapOf(
            "id" to songId,
            "title" to etTitle.text.toString(),
            "artistNames" to listOf(etArtist.text.toString()),
            "categoryIds" to listOf(categoryId), // hoặc sửa thành dynamic nếu cần
            "count" to 0,
            "duration" to duration,
            "image" to imageUrl,
            "url" to audioUrl
        )

        ref.setValue(songData).addOnSuccessListener {
            Toast.makeText(this, "✅ Tải lên thành công! Chờ admin phê duyệt.", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }.addOnFailureListener {
            Toast.makeText(this, "❌ Lưu metadata thất bại", Toast.LENGTH_SHORT).show()
        }
    }


    private fun getNextSongId(callback: (String) -> Unit) {
        val db = FirebaseDatabase.getInstance("https://appmusicrealtime-default-rtdb.asia-southeast1.firebasedatabase.app")
        val songsRef = db.getReference("songs")
        val pendingRef = db.getReference("pendingUploads")

        var maxId = 0
        var count = 0

        val checkDone = {
            count++
            if (count == 2) {
                callback("s" + (maxId + 1).toString().padStart(4, '0'))
            }
        }

        songsRef.orderByKey().addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    val key = child.key ?: continue
                    if (key.startsWith("s") && key.length == 5) {
                        val num = key.substring(1).toIntOrNull() ?: continue
                        if (num > maxId) maxId = num
                    }
                }
                checkDone()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Upload", "Lỗi đọc songs/: ${error.message}")
                checkDone()
            }
        })

        pendingRef.orderByKey().addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    val key = child.key ?: continue
                    if (key.startsWith("s") && key.length == 5) {
                        val num = key.substring(1).toIntOrNull() ?: continue
                        if (num > maxId) maxId = num
                    }
                }
                checkDone()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Upload", "Lỗi đọc pendingUploads/: ${error.message}")
                checkDone()
            }
        })
    }

}
