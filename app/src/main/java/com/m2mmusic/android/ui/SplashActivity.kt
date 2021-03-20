package com.m2mmusic.android.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.m2mmusic.android.databinding.ActivitySplashBinding
import kotlinx.coroutines.*

/**
 * 闪屏页
 */
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ViewBinding加载布局
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.splashText.text = "M2M Music"

        CoroutineScope(Dispatchers.Main).launch {
            val intent = Intent(this@SplashActivity, MainActivity::class.java)
            delay(2000)
            startActivity(intent)
            finish()
        }

    }
}