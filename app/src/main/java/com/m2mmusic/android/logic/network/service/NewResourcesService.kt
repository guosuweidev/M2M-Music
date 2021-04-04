package com.m2mmusic.android.logic.network.service

import com.m2mmusic.android.logic.model.NewResourcesResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface NewResourcesService {

    /**
     * 获取新歌、新碟、数字专辑
     * 非必须登录
     * 未登录，不带cookie
     */
    @GET("homepage/block/page")
    fun getNewResources(@Query("timestamp") timestamp: Long): Call<NewResourcesResponse>

    /**
     * 获取新歌、新碟、数字专辑
     * 非必须登录
     * 登录，带cookie
     */
    @GET("homepage/block/page")
    fun getNewResources(
        @Query("cookie") cookie: String,
        @Query("timestamp") timestamp: Long
    ): Call<NewResourcesResponse>

}