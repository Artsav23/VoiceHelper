package com.example.voicehelper

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlin.concurrent.thread

class StartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        thread {
            Thread.sleep(3000)
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}