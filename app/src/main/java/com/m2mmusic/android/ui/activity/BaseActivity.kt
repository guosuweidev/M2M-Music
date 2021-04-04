package com.m2mmusic.android.ui.activity

import android.graphics.Color
import android.media.AudioManager
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.m2mmusic.android.utils.ActivityCollector
import com.m2mmusic.android.utils.setStatusBarTranslucent

/**
 * Created by 小小苏 at 2021/3/22
 * BaseActivity
 * 作为该项目所有Activity的父类
 */
open class BaseActivity : AppCompatActivity() {

//    var statusbarHeight = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityCollector.addActivity(this)
        // 设置状态栏透明
        setStatusBarTranslucent(this)
        // 获取媒体音量控制流
        volumeControlStream = AudioManager.STREAM_MUSIC

        /*val resourceId = applicationContext.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            statusbarHeight = applicationContext.resources.getDimensionPixelSize(resourceId)
        }*/
    }

    override fun onDestroy() {
        super.onDestroy()
        ActivityCollector.removeActivity(this)
    }

}