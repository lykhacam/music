package com.example.myapplication.fragment

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentFeedbackBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class FeedbackFragment : Fragment() {

    private var _binding: FragmentFeedbackBinding? = null
    private val binding get() = _binding!!
    private val feedbackRef = FirebaseDatabase.getInstance("https://appmusicrealtime-default-rtdb.asia-southeast1.firebasedatabase.app")
        .getReference("feedbacks")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedbackBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnSend.setOnClickListener {
            val message = binding.edtMessage.text.toString().trim()
            if (message.isEmpty()) {
                Toast.makeText(context, "Vui lòng nhập nội dung.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"
            val feedbackId = feedbackRef.push().key ?: return@setOnClickListener

            val feedback = mapOf(
                "uid" to uid,
                "message" to message,
                "timestamp" to System.currentTimeMillis()
            )

            feedbackRef.child(feedbackId).setValue(feedback)
                .addOnSuccessListener {
                    Toast.makeText(context, "Cảm ơn bạn đã góp ý!", Toast.LENGTH_SHORT).show()

                    // Xoá nội dung sau khi gửi
                    binding.edtMessage.text.clear()

                    // Chuyển về HomeFragment
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, HomeFragment())
                        .commit()
                }

                .addOnFailureListener {
                    Toast.makeText(context, "Gửi thất bại. Thử lại sau.", Toast.LENGTH_SHORT).show()
                }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
