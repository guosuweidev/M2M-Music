package com.m2mmusic.android.logic.model

/**
 * 推荐歌单 Response
 */
data class RecommendPlaylistsResponse(
    val code: Int,              // 回应code
    val result: List<Result>
)

/**
 * 歌单详情 Response
 * 推荐的歌单、我的歌单通用
 */
data class PlaylistDetailResponse(
    val code: Int,              // 回应code
    val playlist: Playlist      // 歌单
)

data class Result(
    val id: Long,                // 歌单id
    val name: String,           // 歌单名称
    val copywriter: String,     // 推荐理由
    val picUrl: String,         // 封面链接
    val canDislike: Boolean,    // 是否已收藏，false表示未收藏
    val playCount: Long,        // 播放次数
    val trackCount: Int         // 歌曲数量
)

data class Playlist(
    val id: Int,                // 歌单id
    val name: String,           // 歌单名称
    val coverImgUrl: String,    // 封面链接
    val createTime: Long,       // 创建时间，毫秒值
    val updateTime: Long,       // 修改时间，毫秒值
    val trackCount: Int,        // 歌曲数量
    val playCount: Long,         // 播放次数
    val description: String,    // 歌单描述
    val creator: Creator,       // 创建者
    val tracks: List<Track>     // 歌曲列表
)

data class Creator(
    val nickname: String,       // 创建者昵称
    val avatarUrl: String       // 创建者头像链接
)

data class Track(
    val id: Int,                // 歌曲id
    val name: String,           // 歌曲名称
    val ar: List<Ar>,           // 演唱者列表（可能有多个）
    val al: Al                  // 所属专辑
)

data class Ar(
    val id: Int,                // 演唱者id
    val name: String            // 演唱者名称
)

data class Al(
    val id: Int,                // 专辑id
    val name: String,           // 专辑名称
    val picUrl: String          // 专辑封面链接
)
