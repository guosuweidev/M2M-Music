package com.m2mmusic.android.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.m2mmusic.android.R
import com.m2mmusic.android.databinding.ActivitySplashBinding
import com.m2mmusic.android.databinding.ViewToolbarBinding
import com.m2mmusic.android.utils.isDarkTheme
import com.m2mmusic.android.utils.setFullScreen
import kotlinx.coroutines.*
import com.m2mmusic.android.utils.startActivity

/**
 * 闪屏页
 */
class SplashActivity : BaseActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var toolbarBinding: ViewToolbarBinding

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ViewBinding加载布局
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setFullScreen(this)
        if (isDarkTheme(this)) {
            binding.apply {
                made.setImageResource(R.drawable.m2m_made_dark)
                to.setImageResource(R.drawable.m2m_to_dark)
                moving1.setImageResource(R.drawable.m2m_movi_dark)
                moving2.setImageResource(R.drawable.m2m_ng_dark)

            }
        }

        CoroutineScope(Dispatchers.Main).launch {
            delay(2000)
            startActivity<MainActivity>(this@SplashActivity) {}
        }

    }
}