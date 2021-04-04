package com.m2mmusic.android.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.m2mmusic.android.R
import com.m2mmusic.android.databinding.ActivityLocalMusicBinding
import com.m2mmusic.android.databinding.ActivityMainBinding

class LocalMusicActivity : BaseActivity() {

    private lateinit var binding: ActivityLocalMusicBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocalMusicBinding.inflate(layoutInflater)
        setContentView(binding.root)



    }
}