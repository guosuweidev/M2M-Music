package com.m2mmusic.android.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.m2mmusic.android.databinding.ActivitySplashBinding
import com.m2mmusic.android.databinding.ViewToolbarBinding
import com.m2mmusic.android.utils.setFullScreen
import kotlinx.coroutines.*
import com.m2mmusic.android.utils.startActivity

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
        setFullScreen(this)

        CoroutineScope(Dispatchers.Main).launch {
            delay(2000)
            startActivity<MainActivity>(this@SplashActivity) {}
        }

    }
}