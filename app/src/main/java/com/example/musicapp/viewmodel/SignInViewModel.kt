package com.example.myapplication.viewmodel

import android.app.Application
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth

class SignInViewModel(private val app: Application) : AndroidViewModel(app) {

    private val auth = FirebaseAuth.getInstance()

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private val _success = MutableLiveData<Boolean>()
    val success: LiveData<Boolean> get() = _success

    fun validateAndLogin(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _error.value = "Please enter both email and password"
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _error.value = "Invalid email format"
            return
        }

        if (password.length < 6) {
            _error.value = "Password too short"
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Log.d("SignInViewModel", "Login successful for UID: ${user?.uid}, email: ${user?.email}")
                    _success.value = true
                } else {
                    _error.value = task.exception?.message ?: "Login failed"
                    Log.e("SignInViewModel", "Login error: ${task.exception?.message}")
                }
            }
    }
}
