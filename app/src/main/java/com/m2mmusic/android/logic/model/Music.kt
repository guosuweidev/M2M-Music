package com.m2mmusic.android.logic.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

//@Entity(tableName = "PlayList")
@Parcelize
data class Music(
    var type        // 歌曲类型（0网络1本地）
    : Int = 0,
    var songId      // 歌曲ID(网络)
    : Long = 0L,
    var title       // 音乐标题（通用）
    : String = "未知",
    var artist      // 艺术家（通用）
    : String = "未知",
    var album       // 专辑地址（网络）
    : String? = null,
    var albumId     // 专辑ID（本地）
    : Long = 0L,
    var albumTitle  // 专辑名称（通用）
    : String = "未知",
    var duration    // 持续时间（本地）
    : Long = 0L,
    var path        // 播放地址（通用）
    : String? = null
) : Parcelable {
    /*@PrimaryKey(autoGenerate = true)
    var id: Long = 0L*/
}