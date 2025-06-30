package com.example.myapplication.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.CheckBox
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentUploadSongBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.FirebaseDatabase

class UploadSongFragment : Fragment() {

    private var _binding: FragmentUploadSongBinding? = null
    private val binding get() = _binding!!

    private var selectedCategoryIds = mutableListOf<String>()
    private var selectedMoodIds = mutableListOf<String>()
    private var selectedTimeIds = mutableListOf<String>()
    private var selectedAudioUri: Uri? = null
    private var selectedImageUri: Uri? = null

    private val dbRef = FirebaseDatabase.getInstance(
        "https://appmusicrealtime-default-rtdb.asia-southeast1.firebasedatabase.app"
    ).reference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUploadSongBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvSelectCategory.setOnClickListener {
            showMultiSelectBottomSheet("Chọn thể loại", "categories") { ids, names ->
                selectedCategoryIds = ids.toMutableList()
                binding.tvSelectCategory.text = names.joinToString()
            }
        }

        binding.tvSelectMood.setOnClickListener {
            showMultiSelectBottomSheet("Chọn tâm trạng", "moods") { ids, names ->
                selectedMoodIds = ids.toMutableList()
                binding.tvSelectMood.text = names.joinToString()
            }
        }

        binding.tvSelectTime.setOnClickListener {
            showMultiSelectBottomSheet("Chọn thời điểm", "suitableTimes") { ids, names ->
                selectedTimeIds = ids.toMutableList()
                binding.tvSelectTime.text = names.joinToString()
            }
        }

        binding.btnSelectFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "audio/*"
            startActivityForResult(intent, 1001)
        }

        binding.btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, 1002)
        }

        binding.btnUpload.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val artist = binding.etArtist.text.toString().trim()
            val duration = binding.etDuration.text.toString().trim()
            val isPrivate = binding.cbPrivateUpload.isChecked
            val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid

            if (uid == null) {
                Toast.makeText(context, "Chưa đăng nhập", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (title.isEmpty() || artist.isEmpty() || duration.isEmpty() ||
                selectedAudioUri == null || selectedImageUri == null || selectedCategoryIds.isEmpty()
            ) {
                Toast.makeText(context, "Vui lòng điền đủ thông tin và chọn file", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Đổi trạng thái nút
            binding.btnUpload.isEnabled = false
            binding.btnUpload.text = "Đang tải lên..."

            dbRef.child("pendingUploads").get().addOnSuccessListener { snapshot ->
                val existingIds = snapshot.children.mapNotNull { it.key }
                val maxIndex = existingIds.mapNotNull { it.removePrefix("s").toIntOrNull() }.maxOrNull() ?: 0
                val nextIndex = maxIndex + 1
                val songId = "s" + String.format("%04d", nextIndex)

                val storage = com.google.firebase.storage.FirebaseStorage.getInstance().reference
                val imageRef = storage.child("images/$songId.jpg")
                val uploadImageTask = imageRef.putFile(selectedImageUri!!)

                uploadImageTask.addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                        val audioRef = storage.child("songs/$songId.mp3")
                        val uploadAudioTask = audioRef.putFile(selectedAudioUri!!)

                        uploadAudioTask.addOnSuccessListener {
                            audioRef.downloadUrl.addOnSuccessListener { audioUrl ->

                                val songMap = mapOf(
                                    "id" to songId,
                                    "title" to title,
                                    "artistNames" to listOf(artist),
                                    "categoryIds" to selectedCategoryIds,
                                    "moodIds" to selectedMoodIds,
                                    "suitableTimeIds" to selectedTimeIds,
                                    "duration" to (duration.toIntOrNull() ?: 0),
                                    "image" to imageUrl.toString(),
                                    "url" to audioUrl.toString(),
                                    "count" to 0,
                                    "uploadedBy" to uid,
                                    "uploadedAt" to System.currentTimeMillis()
                                )

                                val onSuccessNavigate: () -> Unit = {
                                    Toast.makeText(context, "Đã tải lên thành công!", Toast.LENGTH_SHORT).show()
                                    resetForm()
                                    parentFragmentManager.beginTransaction()
                                        .replace(R.id.fragment_container, MySongsFragment())
                                        .addToBackStack(null)
                                        .commit()
                                }

                                if (isPrivate) {
                                    dbRef.child("users").child(uid).child("mySongs").child(songId)
                                        .setValue(songMap)
                                        .addOnSuccessListener { onSuccessNavigate() }
                                        .addOnFailureListener {
                                            showUploadError("Lỗi khi lưu bài hát riêng tư")
                                        }
                                } else {
                                    val userRef = dbRef.child("users").child(uid).child("mySongs").child(songId)
                                    val pendingRef = dbRef.child("pendingUploads").child(songId)

                                    userRef.setValue(songMap)
                                    pendingRef.setValue(songMap)
                                        .addOnSuccessListener { onSuccessNavigate() }
                                        .addOnFailureListener {
                                            showUploadError("Lỗi khi gửi bài hát")
                                        }
                                }
                            }
                        }.addOnFailureListener {
                            showUploadError("Lỗi khi upload file nhạc")
                        }
                    }
                }.addOnFailureListener {
                    showUploadError("Lỗi khi upload ảnh")
                }
            }.addOnFailureListener {
                showUploadError("Lỗi khi tạo mã bài hát")
            }
        }
    }

    private fun showUploadError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        binding.btnUpload.isEnabled = true
        binding.btnUpload.text = "Tải lên"
    }

    private fun resetForm() {
        binding.etTitle.setText("")
        binding.etArtist.setText("")
        binding.etDuration.setText("")
        binding.tvFileName.text = ""
        binding.tvSelectCategory.text = "Chọn thể loại"
        binding.tvSelectMood.text = "Chọn tâm trạng"
        binding.tvSelectTime.text = "Chọn thời điểm"
        selectedAudioUri = null
        selectedImageUri = null
        binding.cbPrivateUpload.isChecked = false
        binding.btnUpload.isEnabled = true
        binding.btnUpload.text = "Tải lên"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                1001 -> {
                    selectedAudioUri = data?.data
                    binding.tvFileName.text = selectedAudioUri?.lastPathSegment
                }
                1002 -> {
                    selectedImageUri = data?.data
                }
            }
        }
    }

    private fun showMultiSelectBottomSheet(
        title: String,
        node: String,
        onConfirm: (List<String>, List<String>) -> Unit
    ) {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(com.example.myapplication.R.layout.dialog_multi_select, null)
        val container = view.findViewById<ViewGroup>(com.example.myapplication.R.id.containerCheckboxes)
        val confirmBtn = view.findViewById<View>(com.example.myapplication.R.id.btnConfirm)

        val preselectedIds = when (node) {
            "categories" -> selectedCategoryIds
            "moods" -> selectedMoodIds
            "suitableTimes" -> selectedTimeIds
            else -> emptyList()
        }.toMutableList()

        dbRef.child(node).get().addOnSuccessListener { snapshot ->
            container.removeAllViews()
            val selectedIds = preselectedIds.toMutableList()
            val selectedNames = mutableListOf<String>()

            for (child in snapshot.children) {
                val id = child.child("id").getValue(String::class.java) ?: continue
                val name = child.child("name").getValue(String::class.java) ?: continue

                val checkBox = CheckBox(requireContext()).apply {
                    text = name
                    setTextColor(android.graphics.Color.WHITE)
                    isChecked = preselectedIds.contains(id)
                    setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            if (!selectedIds.contains(id)) selectedIds.add(id)
                            if (!selectedNames.contains(name)) selectedNames.add(name)
                        } else {
                            selectedIds.remove(id)
                            selectedNames.remove(name)
                        }
                    }
                }

                container.addView(checkBox)
            }

            confirmBtn.setOnClickListener {
                onConfirm(selectedIds, selectedNames)
                dialog.dismiss()
            }

            dialog.setContentView(view)
            dialog.show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
