package com.m2mmusic.android.logic.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * 获取专辑内容
 * 不需要登录
 */
data class AlbumDetailResponse(
    val code: Int,
    val album: Album,
    val songs: List<Songs>
) {
    data class Album(
        val name: String,                    // 专辑名称
        val blurPicUrl: String,              // 专辑封面URL
        val artists: List<NewResourcesResponse.Artist>,     // 专辑艺术家
        val publishTime: Long,              // 发行时间
        val company: String,                // 发行公司
        val description: String,            // 简介
        val subType: String,               // 专辑类别
        val size: Int                       // 包含歌曲数
    )

    data class Songs(
        val id: Long         // 歌曲id
    )
}

/**
 * 获取歌单详情（动态信息）
 * 评论、收藏、播放数等
 * 还可用于判断该歌单是否已经收藏(需要登录)
 */
data class PlaylistDetailDynamicResponse(
    val code: Int,              // 回应code
    val commentCount: Int,      // 评论数
    val shareCount: Int,        // 分享次数
    val playCount: Int,         // 播放次数
    val bookedCount: Int,       // 被收藏次数
    val subscribed: Boolean     // 是否收藏
)

/**
 * 心动模式
 * 需要登录
 * 必须传当前歌曲、歌单id
 */
data class IntelligenceModeResponse(
    val code: Int,
    val data: List<IntelligenceData>
) {
    data class IntelligenceData(
        val songInfo: SongResponse.Song
    )
}

/**
 * 推荐歌单 Response
 * 登不登录都可
 */
data class RecommendPlaylistsResponse(
    val code: Int,              // 回应code
    val result: List<Result>
) {
    @Parcelize
    data class Result(
        val id: Long,               // 歌单id
        val name: String,           // 歌单名称
        val copywriter: String,             // 推荐理由
        val picUrl: String,                 // 封面链接
//        val canDislike: Boolean,            // 是否已收藏，false表示未收藏
        val playCount: Long,                // 播放次数
        val trackCount: Long                 // 歌曲数量
    ) : Parcelable
}

/**
 * 歌单详情 Response
 * 推荐的歌单、我的歌单通用
 * 不登录只显示部分歌单内容
 */
data class PlaylistDetailResponse(
    val code: Int,              // 回应code
    val playlist: Playlist      // 歌单
) {
    //    @Entity(tableName = "PlaylistDetail")
    data class Playlist(
//        @PrimaryKey(autoGenerate = true)
        val id: Long,                // 歌单id
        val name: String,           // 歌单名称
        val coverImgUrl: String,    // 封面链接
        val createTime: Long,       // 创建时间，毫秒值
        val updateTime: Long,       // 修改时间，毫秒值
        val trackCount: Long,        // 歌曲数量
        val playCount: Long,         // 播放次数
        val description: String,    // 歌单描述
//        @Ignore
        val creator: Creator,       // 创建者
//        @Ignore
//        val tracks: List<Track>     // 歌曲列表
        val trackIds: List<TrackIds>        // 歌曲id列表
    ) {
        /*val createrName: String = "未知"
        val createrAvatarUrl: String? = null
        val songs : ArrayList<Long>
            get() {
                tracks.forEach { track ->
                    songs.add(track.id)
                }
                return songs
            }*/

        data class Creator(
            val nickname: String,       // 创建者昵称
            val avatarUrl: String       // 创建者头像链接
        )

        data class Track(
            val id: Long,                // 歌曲id
            val name: String,           // 歌曲名称
            val ar: List<Ar>,           // 演唱者列表（可能有多个）
            val al: Al                  // 所属专辑
        ) {
            data class Ar(
                val id: Long,                // 演唱者id
                val name: String            // 演唱者名称
            )

            data class Al(
                val id: Long,                // 专辑id
                val name: String,           // 专辑名称
                val picUrl: String          // 专辑封面链接
            )
        }

        data class TrackIds(
            val id: Long                // 歌曲id
        )
    }
}








