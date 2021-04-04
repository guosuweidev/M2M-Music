package com.m2mmusic.android.ui.fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.m2mmusic.android.logic.Repository
import com.m2mmusic.android.logic.model.LoginResponse
import com.m2mmusic.android.logic.model.Music
import com.m2mmusic.android.logic.model.PlaylistDetailResponse
import com.m2mmusic.android.logic.model.UserLevelResponse
import com.m2mmusic.android.utils.TimeUtil

class MyFragmentViewModel : ViewModel() {

    val isLogin: LiveData<Boolean>           // 当前登录状态
    private val userLikeListLiveData = MutableLiveData<Long>()
    private val userLikeListItemLiveData = MutableLiveData<ArrayList<Long>>()
    private val userMusicListLiveData = MutableLiveData<Long>()
    private val userLevelLiveData = MutableLiveData<Long>()
    var profile: LoginResponse.Profile? = null
    var userLevel: UserLevelResponse.Data? = null
    var userLikeList =  ArrayList<Music>()
    var userMusicList =  ArrayList<PlaylistDetailResponse.Playlist>()
    var userSubscribedMusicList =  ArrayList<PlaylistDetailResponse.Playlist>()
    var userCreatedMusicList =  ArrayList<PlaylistDetailResponse.Playlist>()

    /**
     * 初始化
     * 获取播放器播放状态信息
     * 重置持有的参数及对象
     */
    init {
        isLogin = Repository.isLogin
    }

    val userLikeListResponse = Transformations.switchMap(userLikeListLiveData) {
        Repository.getUserLikeList(it, TimeUtil.getTimestamp())
    }

    val userLikeListItemResponse = Transformations.switchMap(userLikeListItemLiveData) {
        Repository.getOnlineMusic(it)
    }

    val userLevelResponse = Transformations.switchMap(userLevelLiveData) {
        Repository.getUserLevel(it)
    }

    val userMusicListResponse = Transformations.switchMap(userMusicListLiveData) {
        Repository.getUserMusicList(it, TimeUtil.getTimestamp())
    }

    fun getUserProfile() {
        profile = Repository.getUserProfile()
    }

    fun getUserLevel() {
        userLevel = Repository.getUserLevel()
    }

    fun getUserLevelResponse() {
        userLevelLiveData.value = TimeUtil.getTimestamp()
    }

    fun clearUserProfile() {
        profile = null
    }

    fun clearUserLevel() {
        userLevel = null
    }

    fun getUserLikeList() {
        userLikeListLiveData.value = profile?.userId
    }

    fun getUserLikeListItem(ids:ArrayList<Long>) {
        userLikeListItemLiveData.value = ids
    }

    fun saveUserLikeList(musicList: ArrayList<Music>) {
        userLikeList = musicList
        Repository.setUserLikeList(musicList)
    }

    fun getUserMusicList() {
        userMusicListLiveData.value = profile?.userId
    }
}