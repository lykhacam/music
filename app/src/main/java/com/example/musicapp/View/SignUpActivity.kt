package com.example.myapplication.View

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.musicapp.viewmodel.SignUpViewModelFactory
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivitySignUpBinding
import com.example.myapplication.viewmodel.SignUpViewModel
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

        binding.btnSignUp.setOnClickListener {
            val email = binding.edtEmail.text.toString().trim()
            val password = binding.edtPassword.text.toString().trim()
            viewModel.validateAndSignUp(email, password)
        }

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
                Log.d("FIREBASE_TEST", "Current user after sign up: ${currentUser?.email}")
                Toast.makeText(this, "Register success!", Toast.LENGTH_SHORT).show()
                finish() // hoặc chuyển sang SignInActivity
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
