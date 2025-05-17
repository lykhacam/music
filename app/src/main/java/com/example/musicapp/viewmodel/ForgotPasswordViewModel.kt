package com.example.myapplication.viewmodel

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()

    val success = MutableLiveData<Boolean>()
    val error = MutableLiveData<String>()

    fun resetPassword(email: String) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            error.value = "Invalid email format"
            return
        }

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    success.value = true
                } else {
                    error.value = task.exception?.message ?: "Failed to send email"
                }
            }
    }
}
