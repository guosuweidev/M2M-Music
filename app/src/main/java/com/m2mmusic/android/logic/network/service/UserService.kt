package com.m2mmusic.android.logic.network.service

import com.m2mmusic.android.logic.model.*
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface UserService {

    /**
     * 登录
     */
    @POST("login/cellphone")
    fun loginByPhone(
        @Query("countrycode") countrycode: String,
        @Query("phone") phone: String,
        @Query("md5_password") password: String,
        @Query("timestamp") timestamp: Long
    ): Call<LoginResponse>

    /**
     * 退出登录
     */
    @GET("logout")
    fun logout(
        @Query("timestamp") timestamp: Long
    ): Call<LogoutResponse>

    /**
     * 刷新登录状态
     */
    @GET("login/refresh")
    fun refreshLogin(
        @Query("cookie") cookie: String,
        @Query("timestamp") timestamp: Long
    ): Call<RefreshLoginResponse>

    /**
     * 获取用户等级信息
     */
    @GET("user/level")
    fun getUserLevel(
        @Query("cookie") cookie: String,
        @Query("timestamp") timestamp: Long
    ): Call<UserLevelResponse>

    /**
     * 获取用户歌单，收藏数量
     */
    @GET("user/subcount")
    fun getUserSubCount(
        @Query("cookie") cookie: String,
        @Query("timestamp") timestamp: Long
    ): Call<UserSubCountResponse>

    /**
     * 获取用户播放记录统计
     */
    @GET("user/record")
    fun getUserRecord(
        @Query("uid") uid: Long,
        @Query("type") type: Int,
        @Query("cookie") cookie: String,
        @Query("timestamp") timestamp: Long
    ): Call<UserRecordResponse>

    /**
     * 用户喜欢音乐列表
     */
    @GET("likelist")
    fun getUserLikeList(
        @Query("uid") uid: Long,
        @Query("cookie") cookie: String,
        @Query("timestamp") timestamp: Long
    ): Call<UserLikeListResponse>

    /**
     * 喜欢音乐
     */
    @GET("like")
    fun likeSong(
        @Query("id") id: Long,
        @Query("like") like: Boolean,
        @Query("cookie") cookie: String,
        @Query("timestamp") timestamp: Long
    ): Call<LikeSongResponse>

    /**
     * 获取用户歌单
     */
    @GET("user/playlist")
    fun getUserPlayList(
        @Query("uid") uid: Long,
        @Query("cookie") cookie: String,
        @Query("timestamp") timestamp: Long
    ): Call<UserPlayListResponse>

    /**
     * 收藏/取消收藏歌单
     */
    @GET("playlist/subscribe")
    fun subscribePlayList(
        @Query("t") t: Int,
        @Query("id") id: Long,
        @Query("cookie") cookie: String,
        @Query("timestamp") timestamp: Long
    ): Call<SubscribePlayListResponse>
}