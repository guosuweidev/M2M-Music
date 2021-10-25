package com.m2mmusic.android.application

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.m2mmusic.android.weex.extend.ImageLoaderAdapter
import com.taobao.weex.InitConfig
import com.taobao.weex.WXSDKEngine

/**
 * 获取全局Context
 */
class M2MMusicApplication : Application() {

    companion object {
        // 将Context设置成全局变量容易造成内存泄漏
        // 但Application的Context全局只有一份，且与程序生命周期相同，不会被回收
        // 故添加此注解忽略"内存泄漏"警告
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext

        val config: InitConfig = InitConfig.Builder()
            .setImgAdapter(ImageLoaderAdapter())
            .build()
        WXSDKEngine.initialize(this, config);
    }

}