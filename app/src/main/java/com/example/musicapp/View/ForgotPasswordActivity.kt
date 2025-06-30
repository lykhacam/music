package com.example.myapplication.View

import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityForgotPasswordBinding
import com.example.myapplication.viewmodel.ForgotPasswordViewModel

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private val viewModel: ForgotPasswordViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.resetButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.resetButton.isEnabled = false
            viewModel.resetPassword(email)
        }
    }

    private fun observeViewModel() {
        viewModel.success.observe(this) { success ->
            if (success == true) {
                Toast.makeText(this, "Đã gửi email đặt lại mật khẩu!", Toast.LENGTH_LONG).show()
                finish()
            }
            binding.resetButton.isEnabled = true
        }

        viewModel.error.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            binding.resetButton.isEnabled = true
        }
    }
}
