package com.m2mmusic.android.logic.network.service

import com.m2mmusic.android.logic.model.SongResponse
import com.m2mmusic.android.logic.model.UrlOfSongResponse
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Call

interface SongService {

    /**
     * 获取歌曲URL
     */
    @GET("song/url")
    fun getUrlOfSong(
        @Query("id")ids: String,
        @Query("timestamp") timestamp: Long
    ): Call<UrlOfSongResponse>

    /**
     * 获取歌曲详情
     */
    @GET("song/detail")
    fun getDetailOfSong(
        @Query("ids")ids: String,
        @Query("timestamp") timestamp: Long
    ): Call<SongResponse>

    /**
     * 获取歌曲URL
     */
    @GET("song/url")
    fun getUrlOfSong(
        @Query("id")ids: String,
        @Query("cookie") cookie: String,
        @Query("timestamp") timestamp: Long
    ): Call<UrlOfSongResponse>

    /**
     * 获取歌曲详情
     */
    @GET("song/detail")
    fun getDetailOfSong(
        @Query("ids")ids: String,
        @Query("cookie") cookie: String,
        @Query("timestamp") timestamp: Long
    ): Call<SongResponse>

}