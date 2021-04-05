package com.m2mmusic.android.logic.network

import com.m2mmusic.android.logic.Repository
import com.m2mmusic.android.logic.model.UserLevelResponse
import com.m2mmusic.android.logic.network.service.*
import com.m2mmusic.android.utils.LogUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.RuntimeException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object M2MMusicNetwork {

    private val userService = ServiceCreator.create<UserService>()
    private val songService = ServiceCreator.create<SongService>()
    private val newResourcesService = ServiceCreator.create<NewResourcesService>()
    private val playlistService = ServiceCreator.create<PlaylistService>()
    private val searchService = ServiceCreator.create<SearchService>()

    /**
     * 手机号登录
     */
    suspend fun loginByPhone(
        countrycode: String,
        phone: String,
        password: String,
        timestamp: Long
    ) =
        userService.loginByPhone(countrycode, phone, password, timestamp).await()

    /**
     * 退出登录
     */
    suspend fun logout(timestamp: Long) = userService.logout(timestamp).await()

    /**
     * 获取用户等级信息
     */
    suspend fun getUserLevel(timestamp: Long) =
        userService.getUserLevel(Repository.getCookie(), timestamp).await()

    /**
     * 收藏/取消收藏歌单
     */
    suspend fun subscribePlayList(t: Int, id: Long, timestamp: Long) =
        userService.subscribePlayList(t, id, Repository.getCookie(), timestamp).await()

    /**
     * 获取用户喜欢的音乐列表
     */
    suspend fun getUserLikeList(uid: Long, timestamp: Long) =
        userService.getUserLikeList(uid, Repository.getCookie(), timestamp).await()

    /**
     * 获取用户歌单
     */
    suspend fun getUserMusicList(uid: Long, timestamp: Long) =
        userService.getUserPlayList(uid, Repository.getCookie(), timestamp).await()

    /**
     * 私人FM
     */
    suspend fun getPersonalFM(timestamp: Long) =
        userService.getPersonalFM(Repository.getCookie(), timestamp).await()

    /**
     * 获取歌曲详情
     */
    suspend fun getDetailOfSong(ids: String, timestamp: Long) =
        if (Repository.getLoginState() == true)
            songService.getDetailOfSong(ids, Repository.getCookie(), timestamp).await()
        else
            songService.getDetailOfSong(ids, timestamp).await()

    /**
     * 获取歌曲URL
     */
    suspend fun getUrlOfSong(ids: String, timestamp: Long) =
        if (Repository.getLoginState() == true)
            songService.getUrlOfSong(ids, Repository.getCookie(), timestamp).await()
        else
            songService.getUrlOfSong(ids, timestamp).await()

    /**
     * 获取新歌、新碟、数字专辑
     */
    suspend fun getNewResources(timestamp: Long) =
        if (Repository.getLoginState() == true)
            newResourcesService.getNewResources(Repository.getCookie(), timestamp).await()
        else
            newResourcesService.getNewResources(timestamp).await()

    /**
     * 获取推荐歌单
     */
    suspend fun getRecommendPlaylists(limit: Int, timestamp: Long) =
        if (Repository.getLoginState() == true)
            playlistService.getRecommendPlaylist(limit, Repository.getCookie(), timestamp).await()
        else
            playlistService.getRecommendPlaylist(limit, timestamp).await()

    /**
     * 获取歌单详细信息
     */
    suspend fun getPlaylistDetails(id: Long, timestamp: Long) =
        if (Repository.getLoginState() == true)
            playlistService.getPlaylistDetails(id, Repository.getCookie(), timestamp).await()
        else
            playlistService.getPlaylistDetails(id, timestamp).await()

    /**
     * 获取Album详细信息
     */
    suspend fun getAlbumDetails(id: Long, timestamp: Long) =
        if (Repository.getLoginState() == true)
            playlistService.getAlbumDetails(id, Repository.getCookie(), timestamp).await()
        else
            playlistService.getAlbumDetails(id, timestamp).await()

    /**
     * 获取歌单详情（动态信息）
     */
    suspend fun getPlaylistDetailDynamic(id: Long, timestamp: Long) =
        if (Repository.getLoginState() == true)
            playlistService.getPlaylistDetailDynamic(id, Repository.getCookie(), timestamp).await()
        else
            playlistService.getPlaylistDetailDynamic(id, timestamp).await()

    /**
     * 每日推荐
     */
    suspend fun getDailyRecommend(timestamp: Long) =
        playlistService.getDailyRecommend(Repository.getCookie(), timestamp).await()

    /**
     * 获取搜索建议
     */
    suspend fun getSearchSuggest(keywords:String, timestamp: Long) =
        searchService.getSearchSuggest(keywords, "mobile", timestamp).await()

    /**
     * 搜索专辑
     */
    suspend fun searchAlbum(keywords: String, timestamp: Long) =
        searchService.searchAlbum(keywords, 10, timestamp).await()

    /**
     * 搜索歌手
     */
    suspend fun searchArtist(keywords: String, timestamp: Long) =
        searchService.searchArtist(keywords, 100, timestamp).await()

    /**
     * 搜索歌单
     */
    suspend fun searchMusicList(keywords: String, timestamp: Long) =
        searchService.searchMusicList(keywords, 1000, timestamp).await()

    /**
     * 搜索单曲
     */
    suspend fun searchMusic(keywords: String, timestamp: Long) =
        searchService.searchMusic(keywords, 1, timestamp).await()

    /**
     * 自定义await()函数
     */
    private suspend fun <T> Call<T>.await(): T {
        return suspendCoroutine { continuation ->
            enqueue(object : Callback<T> {
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    val body = response.body()
                    if (body != null) continuation.resume(body)
                    else continuation.resumeWithException(
                        RuntimeException("response body is null")
                    )
                }

                override fun onFailure(call: Call<T>, t: Throwable) {
                    continuation.resumeWithException(t)
                }
            })
        }
    }

}