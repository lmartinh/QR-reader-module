package com.lmartinh.qrreader

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.lmartinh.qrreader.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.mainButton.setOnClickListener {

        }
    }
}