package com.univalle.dogapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.univalle.dogapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(ActivityMainBinding.inflate(layoutInflater).root)
    }
}
