package com.example.myapplication.model

import com.google.firebase.database.Exclude

data class ManageItem(
    var id: String = "",
    var name: String = "",
    @get:Exclude var firebaseKey: String = "" // không lưu vào Firebase
)
