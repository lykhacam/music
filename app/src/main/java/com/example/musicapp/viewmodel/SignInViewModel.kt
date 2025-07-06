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
            _error.value = "Vui lòng nhập đầy đủ email và mật khẩu"
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _error.value = "Email không đúng định dạng"
            return
        }

        if (password.length < 6) {
            _error.value = "Mật khẩu quá ngắn (tối thiểu 6 ký tự)"
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    _success.value = true
                } else {
                    _error.value = task.exception?.message ?: "Đăng nhập thất bại"
                }
            }
    }
}
