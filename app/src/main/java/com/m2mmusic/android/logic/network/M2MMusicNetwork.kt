package com.m2mmusic.android.logic.network

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.RuntimeException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object M2MMusicNetwork {

    private val playlistService = ServiceCreator.create<PlaylistService>()

    /**
     * 获取推荐歌单
     */
    suspend fun getRecommendPlaylists(limit: Int, timestamp: Long) =
        playlistService.getRecommendPlaylist(limit, timestamp).await()

    /**
     * 获取歌单详细信息
     */
    suspend fun getPlaylistDetails(id: Long, timestamp: Long) =
        playlistService.getPlaylistDetails(id, timestamp).await()

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