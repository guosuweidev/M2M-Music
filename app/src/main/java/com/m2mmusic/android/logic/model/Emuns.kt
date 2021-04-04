package com.m2mmusic.android.logic.model

import com.m2mmusic.android.application.M2MMusicApplication
import com.m2mmusic.android.utils.showToast

/**
 * Created by 小小苏 at 2021/3/25
 * 播放模式
 */
enum class PlayMode {
    // 单曲循环
    SINGLE,
    // 列表循环
    LIST,
    // 随机播放
    SHUFFLE;

    companion object {
        @JvmStatic
        fun getDefault(): PlayMode {
            return LIST
        }

        @JvmStatic
        fun switchNextMode(current : PlayMode?) : PlayMode{
            if (current == null){
                return getDefault()
            }
            return when(current){
                LIST -> {
                    "切换为『随机播放』".showToast(M2MMusicApplication.context)
                    SHUFFLE
                }
                SHUFFLE -> {
                    "切换为『单曲循环』".showToast(M2MMusicApplication.context)
                    SINGLE
                }
                SINGLE -> {
                    "切换为『列表循环』".showToast(M2MMusicApplication.context)
                    LIST
                }
            }
        }
    }
}