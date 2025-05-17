package com.example.myapplication.View

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.myapplication.viewmodel.SignInViewModelFactory
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivitySignInBinding
import com.example.myapplication.viewmodel.SignInViewModel
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private val viewModel: SignInViewModel by viewModels {
        SignInViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        // ✅ Khởi tạo binding đúng cách
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Xử lý đăng nhập
        binding.btnSignIn.setOnClickListener {
            val email = binding.edtEmail.text.toString().trim()
            val password = binding.edtPassword.text.toString().trim()
            viewModel.validateAndLogin(email, password)
        }

        // Điều hướng sang Đăng ký
        binding.tvGoToSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        // Điều hướng sang Quên mật khẩu
//        binding.tvForgotPassword.setOnClickListener {
//            startActivity(Intent(this, ForgotPasswordActivity::class.java))
//        }

        viewModel.error.observe(this) { message ->
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                showInputError()
            } else {
                clearInputError()
            }
        }

        viewModel.success.observe(this) { ok ->
            if (ok) {
                val currentUser = FirebaseAuth.getInstance().currentUser
                Log.d("FIREBASE_TEST", "Current user after sign in: ${currentUser?.email}")
                Toast.makeText(this, "Login success!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }

    private fun showInputError() {
        binding.edtEmail.background =
            ContextCompat.getDrawable(this, R.drawable.edit_text_bg_error)
        binding.edtPassword.background =
            ContextCompat.getDrawable(this, R.drawable.edit_text_bg_error)
    }

    private fun clearInputError() {
        binding.edtEmail.background =
            ContextCompat.getDrawable(this, R.drawable.edit_text_bg)
        binding.edtPassword.background =
            ContextCompat.getDrawable(this, R.drawable.edit_text_bg)
    }
}
