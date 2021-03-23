package com.m2mmusic.android.ui.fragment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.m2mmusic.android.logic.Repository
import com.m2mmusic.android.logic.model.Playlist
import com.m2mmusic.android.logic.model.Result
import com.m2mmusic.android.utils.TimeUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Created by 小小苏 at 2021/3/22
 * MainFragment的ViewModel
 * 获取「推荐歌单」数据并持有
 * 获取「新歌」数据并持有
 * 获取「新碟」数据并持有
 * 获取「数字专辑」数据并持有
 */
class MainFragmentViewModel : ViewModel() {

    private val recommendLimitLiveData = MutableLiveData<Int>()
    private val playlistIdLiveData = MutableLiveData<Long>()

    val recommendPlaylists = ArrayList<Result>()

    val recommendLiveData = Transformations.switchMap(recommendLimitLiveData) {limit ->
        Repository.getRecommendPlaylists(limit, TimeUtil.getTimestamp())
    }

    val playlistLiveData = Transformations.switchMap(playlistIdLiveData) { id ->
        Repository.getPlaylistDetails(id, TimeUtil.getTimestamp())
    }

    fun getRecommendPlaylists(limit: Int) {
        recommendLimitLiveData.value = limit
//        Repository.getRecommendPlaylists(limit, TimeUtil.getTimestamp())
    }

    fun getPlaylistDetails(id: Long) {
        playlistIdLiveData.value = id
    }

}