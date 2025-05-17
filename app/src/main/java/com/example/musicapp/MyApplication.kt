package com.example.myapplication

import android.app.Application
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.google.firebase.FirebaseApp

class MyApplication : Application(), ViewModelStoreOwner {

    private val appViewModelStore: ViewModelStore by lazy {
        ViewModelStore()
    }

    override val viewModelStore: ViewModelStore
        get() = appViewModelStore

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
