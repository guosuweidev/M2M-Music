package com.m2mmusic.android.logic.network.service

import com.m2mmusic.android.logic.model.*
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

    /**
     * 获取Album详细信息
     * 非必须登录
     */
    @GET("album")
    fun getAlbumDetails(
        @Query("id") id: Long,
        @Query("timestamp") timestamp: Long
    ): Call<AlbumDetailResponse>

    /**
     * 获取推荐歌单
     * 非必须登录
     * 登录，带cookie
     */
    @GET("personalized")
    fun getRecommendPlaylist(
        @Query("limit") limit: Int = 6,
        @Query("cookie") cookie: String,
        @Query("timestamp") timestamp: Long
    ): Call<RecommendPlaylistsResponse>

    /**
     * 获取歌单详细信息
     * 非必须登录
     * 登录，带cookie
     */
    @GET("playlist/detail")
    fun getPlaylistDetails(
        @Query("id") id: Long,
        @Query("cookie") cookie: String,
        @Query("timestamp") timestamp: Long
    ): Call<PlaylistDetailResponse>

    /**
     * 获取Album详细信息
     * 非必须登录
     * 登录，带cookie
     */
    @GET("album")
    fun getAlbumDetails(
        @Query("id") id: Long,
        @Query("cookie") cookie: String,
        @Query("timestamp") timestamp: Long
    ): Call<AlbumDetailResponse>

    /**
     * 获取歌单详情（动态信息）
     * 评论、收藏、播放数等
     * 还可用于判断该歌单是否已经收藏(需要登录)
     */
    @GET("playlist/detail/dynamic")
    fun getPlaylistDetailDynamic(
        @Query("id") id: Long,
        @Query("cookie") cookie: String,
        @Query("timestamp") timestamp: Long
    ): Call<PlaylistDetailDynamicResponse>

    /**
     * 获取歌单详情（动态信息）
     * 评论、收藏、播放数等
     * 还可用于判断该歌单是否已经收藏(需要登录)
     */
    @GET("playlist/detail/dynamic")
    fun getPlaylistDetailDynamic(
        @Query("id") id: Long,
        @Query("timestamp") timestamp: Long
    ): Call<PlaylistDetailDynamicResponse>

    /**
     * 每日推荐
     */
    @GET("recommend/songs")
    fun getDailyRecommend(
        @Query("cookie") cookie: String,
        @Query("timestamp") timestamp: Long
    ):Call<DailyRecommendResponse>
}