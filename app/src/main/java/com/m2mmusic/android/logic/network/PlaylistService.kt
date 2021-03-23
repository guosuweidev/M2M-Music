package com.m2mmusic.android.logic.network

import com.m2mmusic.android.logic.model.PlaylistDetailResponse
import com.m2mmusic.android.logic.model.RecommendPlaylistsResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface PlaylistService {

    /**
     * 获取推荐歌单
     * 非必须登录
     */
    @GET("personalized")
    fun getRecommendPlaylist(
        @Query("limit") limit: Int = 6,
        @Query("timestamp") timestamp: Long
    ): Call<RecommendPlaylistsResponse>

    /**
     * 获取歌单详细信息
     * 非必须登录
     */
    @GET("playlist/detail")
    fun getPlaylistDetails(
        @Query("id") id: Long,
        @Query("timestamp") timestamp: Long
    ): Call<PlaylistDetailResponse>

}