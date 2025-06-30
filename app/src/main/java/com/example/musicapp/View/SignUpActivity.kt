package com.example.myapplication.View

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivitySignUpBinding
import com.example.myapplication.viewmodel.SignUpViewModel
import com.example.musicapp.viewmodel.SignUpViewModelFactory
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private val viewModel: SignUpViewModel by viewModels {
        SignUpViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Xử lý nút Sign Up
        binding.btnSignUp.setOnClickListener {
            val email = binding.edtEmail.text.toString().trim()
            val password = binding.edtPassword.text.toString().trim()
            val fullName = binding.edtFullName.text.toString().trim()
            val birthYear = binding.edtBirthYear.text.toString().trim()

            val favoriteGenres = mutableListOf<String>()
            if (binding.cbPop.isChecked) favoriteGenres.add("pop")
            if (binding.cbLofi.isChecked) favoriteGenres.add("lofi")
            if (binding.cbNhacTre.isChecked) favoriteGenres.add("nhac_tre")
            if (binding.cbRap.isChecked) favoriteGenres.add("rap")
            if (binding.cbBolero.isChecked) favoriteGenres.add("bolero")

            viewModel.validateAndSignUp(
                email = email,
                password = password,
                fullName = fullName,
                birthYear = birthYear,
                favoriteGenres = favoriteGenres
            )
        }

        // Khi nhấn "Sign in" -> chuyển sang màn đăng nhập
        binding.tvGoToSignIn.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Quan sát lỗi
        viewModel.error.observe(this) { message ->
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                showInputError()
            } else {
                clearInputError()
            }
        }

        // Quan sát thành công
        viewModel.success.observe(this) { ok ->
            if (ok) {
                val currentUser = FirebaseAuth.getInstance().currentUser
                Log.d("FIREBASE_TEST", "Current user after sign up: ${currentUser?.email}")
                Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }

    private fun showInputError() {
        binding.edtEmail.background = ContextCompat.getDrawable(this, R.drawable.edit_text_bg_error)
        binding.edtPassword.background = ContextCompat.getDrawable(this, R.drawable.edit_text_bg_error)
    }

    private fun clearInputError() {
        binding.edtEmail.background = ContextCompat.getDrawable(this, R.drawable.edit_text_bg)
        binding.edtPassword.background = ContextCompat.getDrawable(this, R.drawable.edit_text_bg)
    }
}
