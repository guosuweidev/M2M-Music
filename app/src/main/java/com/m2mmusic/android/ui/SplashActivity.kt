package com.m2mmusic.android.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.m2mmusic.android.databinding.ActivitySplashBinding
import com.m2mmusic.android.databinding.ViewToolbarBinding
import kotlinx.coroutines.*

/**
 * 闪屏页
 */
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var toolbarBinding: ViewToolbarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ViewBinding加载布局
        binding = ActivitySplashBinding.inflate(layoutInflater)
        toolbarBinding = ViewToolbarBinding.bind(binding.root)
        setContentView(binding.root)
        setSupportActionBar(toolbarBinding.toolbar)

        binding.splashText.text = "M2M Music"

        CoroutineScope(Dispatchers.Main).launch {
            delay(2000)
            MainActivity.startThisActivity(this@SplashActivity)
        }

    }
}