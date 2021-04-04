package com.m2mmusic.android.logic.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PlaylistEvent(

    // 播放列表
    var playlist: ArrayList<Music> = ArrayList(),
    // 起始播放索引
    var playIndex: Int = 0,
    // 播放模式
    var playmode: PlayMode = PlayMode.LIST,
    // 播放的歌曲
//    var playingSong: Music? = null

) : Parcelable
