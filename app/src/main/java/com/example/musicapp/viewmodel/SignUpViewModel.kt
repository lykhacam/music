package com.example.myapplication.viewmodel

import android.app.Application
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignUpViewModel(private val app: Application) : AndroidViewModel(app) {

    private val auth = FirebaseAuth.getInstance()

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private val _success = MutableLiveData<Boolean>()
    val success: LiveData<Boolean> get() = _success

    fun validateAndSignUp(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _error.value = "Email and password cannot be empty"
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _error.value = "Invalid email format"
            return
        }

        if (password.length < 6) {
            _error.value = "Password must be at least 6 characters"
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    val userEmail = auth.currentUser?.email

                    if (uid != null && userEmail != null) {
                        val userRef = FirebaseDatabase.getInstance(
                            "https://appmusicrealtime-default-rtdb.asia-southeast1.firebasedatabase.app"
                        ).getReference("users").child(uid)


                        val userData = mapOf(
                            "email" to userEmail,
                            "role" to "user", // thêm phân quyền
                            "favorites" to mapOf("placeholder" to false),
                            "recentlyPlayed" to mapOf("placeholder" to 0L),
                            "playlists" to mapOf("placeholder" to mapOf("name" to "init", "songIds" to mapOf<String, Boolean>())),
                            "downloads" to mapOf("placeholder" to false)
                        )



                        userRef.setValue(userData)
                            .addOnSuccessListener {
                                Log.d("SignUpViewModel", "User data saved for UID: $uid")
                                _success.value = true
                            }
                            .addOnFailureListener {
                                Log.e("SignUpViewModel", "Failed to save user data: ${it.message}")
                                _error.value = "Failed to save user data: ${it.message}"
                            }
                    } else {
                        _error.value = "User creation failed: UID or Email is null"
                    }

                } else {
                    _error.value = task.exception?.message ?: "Registration failed"
                }
            }
    }
}
