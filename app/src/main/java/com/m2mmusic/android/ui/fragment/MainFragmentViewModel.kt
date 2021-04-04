package com.m2mmusic.android.ui.fragment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.m2mmusic.android.logic.Repository
import com.m2mmusic.android.logic.model.NewResourcesResponse.Creative
import com.m2mmusic.android.logic.model.RecommendPlaylistsResponse.Result
import com.m2mmusic.android.utils.TimeUtil

/**
 * Created by 小小苏 at 2021/3/22
 * MainFragment的ViewModel
 * 获取「推荐歌单」数据并持有
 * 获取「新歌」数据并持有
 * 获取「新碟」数据并持有
 * 获取「数字专辑」数据并持有
 */
class MainFragmentViewModel() : ViewModel() {

    private val newResourcesLiveData = MutableLiveData<Long>()
    private val recommendLimitLiveData = MutableLiveData<Int>()
    private val playlistIdLiveData = MutableLiveData<Long>()
    private val onlineMusicIdLiveData = MutableLiveData<ArrayList<Long>>()

    // 缓存新歌、新碟、数字专辑
    val newResourcesLists = ArrayList<Creative>()

    // 缓存推荐歌单的结果
    val recommendPlayLists = ArrayList<Result>()

    // 监听「新歌、新碟、数字专辑」
    val newResLiveData = Transformations.switchMap(newResourcesLiveData) { timestamp ->
        Repository.getNewResources(timestamp)
    }

    // 监听「推荐歌单」
    val recommendLiveData = Transformations.switchMap(recommendLimitLiveData) { limit ->
        Repository.getRecommendPlaylists(limit, TimeUtil.getTimestamp())
    }

    // 监听「歌单详情」
    val playlistLiveData = Transformations.switchMap(playlistIdLiveData) { id ->
        Repository.getPlaylistDetails(id, TimeUtil.getTimestamp())
    }

    /**
     * 获取「新歌、新碟、数字专辑」入口
     */
    fun getNewResources() {
        newResourcesLiveData.value = TimeUtil.getTimestamp()
    }

    /**
     * 获取「推荐歌单」入口
     */
    fun getRecommendPlaylists(limit: Int) {
        recommendLimitLiveData.value = limit
    }

    /**
     * 获取「歌单详情」入口
     */
    fun getPlaylistDetails(id: Long) {
        playlistIdLiveData.value = id
    }

    /**
     * 向MainFragment提供
     * 来获取Music资源
     */
    fun getOnlineMusic(musicIds: ArrayList<Long>) {
        onlineMusicIdLiveData.value = musicIds
    }

    val onlineMusic = Transformations.switchMap(onlineMusicIdLiveData) {
        Repository.getOnlineMusic(it)
    }

    /**
     * 缓存
     */
    /*fun saveRecPlaylists() = Repository.saveRecPlaylists(recommendPlayLists)

    fun getRecPlaylists() = Repository.getRecPlaylists()

    fun isRecPlaylistsSaved() = Repository.isRecPlaylistsSaved()*/

}