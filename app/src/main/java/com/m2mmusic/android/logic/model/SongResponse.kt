package com.m2mmusic.android.logic.model

/**
 * 获取歌曲详情
 * 但不包括URL
 * 可传多个id
 */
data class SongResponse(
    val code: Int,
    val songs: List<Song>
) {
    data class Song(
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
}

data class UrlOfSongResponse(
    val code: Int,
    val data: List<Data>
) {
    data class Data(
        val url: String
    )
}
