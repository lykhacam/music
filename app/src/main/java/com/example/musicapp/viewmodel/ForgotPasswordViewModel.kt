package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordViewModel(application: Application) : AndroidViewModel(application) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    val success = MutableLiveData<Boolean>()
    val error = MutableLiveData<String>()

    fun resetPassword(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    success.value = true
                } else {
                    error.value = task.exception?.localizedMessage ?: "Không thể gửi email đặt lại mật khẩu"
                }
            }
    }
}
