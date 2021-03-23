package com.m2mmusic.android.logic

import androidx.lifecycle.liveData
import com.m2mmusic.android.logic.model.Playlist
import com.m2mmusic.android.logic.network.M2MMusicNetwork
import kotlinx.coroutines.Dispatchers
import java.lang.Exception
import java.lang.RuntimeException

object Repository {

    const val RECOMMENDLIMIT = 18

    /**
     * 获取推荐歌单
     */
    fun getRecommendPlaylists(limit: Int, timestamp: Long) = liveData(Dispatchers.IO) {
        val result = try {
            val recommendPlaylistsResponse = M2MMusicNetwork.getRecommendPlaylists(limit, timestamp)
            if (recommendPlaylistsResponse.code == 200) {
                val results = recommendPlaylistsResponse.result
                Result.success(results)
            } else {
                Result.failure(RuntimeException("response code is ${recommendPlaylistsResponse.code}"))
            }
        } catch (e: Exception) {
            Result.failure<List<com.m2mmusic.android.logic.model.Result>>(e)
        }
        emit(result)
    }

    /**
     * 获取歌单详细信息
     */
    fun getPlaylistDetails(id:Long, timestamp: Long) = liveData(Dispatchers.IO) {
        val result = try {
            val playlistDetailResponse = M2MMusicNetwork.getPlaylistDetails(id, timestamp)
            if (playlistDetailResponse.code == 200) {
                val playlist = playlistDetailResponse.playlist
                Result.success(playlist)
            } else {
                Result.failure(RuntimeException("response code is ${playlistDetailResponse.code}"))
            }
        } catch (e: Exception) {
            Result.failure<List<Playlist>>(e)
        }
        emit(result)
    }

}