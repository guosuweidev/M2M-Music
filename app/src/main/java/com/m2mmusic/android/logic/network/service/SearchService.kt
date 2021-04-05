package com.m2mmusic.android.logic.network.service

import com.m2mmusic.android.logic.model.*
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface SearchService {

    /**
     * 获取搜索建议
     */
    @GET("search/suggest")
    fun getSearchSuggest(
        @Query("keywords") keywords: String,
        @Query("type") type: String = "mobile",
        @Query("timestamp") timestamp: Long
    ): Call<SearchSuggestResponse>

    /**
     * 搜索单曲
     */
    @GET("cloudsearch")
    fun searchMusic(
        @Query("keywords") keywords: String,
        @Query("type") type: Int = 1,
        @Query("timestamp") timestamp: Long
    ): Call<SearchMusicResponse>

    /**
     * 搜索专辑
     */
    @GET("cloudsearch")
    fun searchAlbum(
        @Query("keywords") keywords: String,
        @Query("type") type: Int = 10,
        @Query("timestamp") timestamp: Long
    ): Call<SearchAlbumResponse>

    /**
     * 搜索歌手
     */
    @GET("cloudsearch")
    fun searchArtist(
        @Query("keywords") keywords: String,
        @Query("type") type: Int = 100,
        @Query("timestamp") timestamp: Long
    ): Call<SearchArtistResponse>

    /**
     * 搜索歌单
     */
    @GET("cloudsearch")
    fun searchMusicList(
        @Query("keywords") keywords: String,
        @Query("type") type: Int = 1000,
        @Query("timestamp") timestamp: Long
    ): Call<SearchMusicListResponse>

}