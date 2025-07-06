package com.example.myapplication.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.example.myapplication.databinding.FragmentContactBinding

class ContactFragment : Fragment() {

    private var _binding: FragmentContactBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // 📧 Email
        binding.layoutEmail.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:lykhacam65@gmail.com") // Thay email bạn
            }
            startActivity(Intent.createChooser(intent, "Chọn ứng dụng email"))
        }

        // 📞 Gọi điện
        binding.layoutPhone.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:0362394289") // Thay số của bạn
            }
            startActivity(intent)
        }

        // 🌐 Facebook
        binding.layoutFacebook.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://www.facebook.com/khac.uong.455167/about") // Thay link
            }
            startActivity(intent)
        }

        // 💬 Zalo
        binding.layoutZalo.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://zalo.me/0362394289") // Thay số hoặc link
            }
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
