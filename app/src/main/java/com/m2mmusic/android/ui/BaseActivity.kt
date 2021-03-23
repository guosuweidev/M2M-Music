package com.m2mmusic.android.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.m2mmusic.android.utils.ActivityCollector
import com.m2mmusic.android.utils.NetworkLiveData
import com.m2mmusic.android.utils.NetworkState

/**
 * Created by 小小苏 at 2021/3/22
 * BaseActivity
 * 作为该项目所有Activity的父类
 */
open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityCollector.addActivity(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        ActivityCollector.removeActivity(this)
    }

}