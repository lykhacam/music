package com.example.myapplication.View

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityScreen1Binding
import com.google.firebase.database.FirebaseDatabase
import android.util.Log
import com.example.myapplication.View.S2Activity

class S1Activity : AppCompatActivity() {

    private lateinit var binding: ActivityScreen1Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScreen1Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ghi dữ liệu test lên Realtime Database
        writeTestDataToFirebase()

        binding.continueButton.setOnClickListener {
            startActivity(Intent(this, S2Activity::class.java))
        }
    }

    private fun writeTestDataToFirebase() {
        val database = FirebaseDatabase.getInstance("https://appmusicrealtime-default-rtdb.asia-southeast1.firebasedatabase.app")
    }
}
