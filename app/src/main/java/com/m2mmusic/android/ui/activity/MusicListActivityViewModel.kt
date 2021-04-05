package com.m2mmusic.android.ui.activity

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.m2mmusic.android.application.M2MMusicApplication
import com.m2mmusic.android.logic.Repository
import com.m2mmusic.android.logic.model.Music
import com.m2mmusic.android.logic.model.PlayMode
import com.m2mmusic.android.logic.model.PlaylistEvent
import com.m2mmusic.android.ui.service.PlayService
import com.m2mmusic.android.utils.TimeUtil
import com.m2mmusic.android.utils.bindService

class MusicListActivityViewModel : ViewModel() {

    private val musicListIdLiveData = MutableLiveData<Long>()
    private val onlineMusicIdLiveData = MutableLiveData<ArrayList<Long>>()
    private val isSubscribedLiveData = MutableLiveData<Long>()
    private val subscribedLiveData = MutableLiveData<Long>()
    private var mBinder: PlayService.PlayBinder? = null            // IBinder对象
    val currentMusic: LiveData<Repository.CurrentMusic>            // 当前Music及播放状态
    var music: Repository.CurrentMusic? = null
    @SuppressLint("StaticFieldLeak")
    private var playService: PlayService? = null
    val musicList = ArrayList<Music>()
    var currentPlayListEvent = PlaylistEvent()
    var progress = 0
    var duration = 0
    var hasSubscribed = false
    var musicListId: Long = 0

    // ServiceConnection
    private val mConnection: ServiceConnection = object : ServiceConnection {
        /**
         * Service成功绑定时运行
         */
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            // 获取Binder对象
            if (service != null) {
                mBinder = service as PlayService.PlayBinder
                playService = mBinder!!.getService()
            }
        }

        /**
         * Service进程崩溃或被kill时运行
         */
        override fun onServiceDisconnected(name: ComponentName?) {
            // 释放Binder对象
            mBinder = null
        }
    }

    init {
        mBinder = null
        currentMusic = Repository.currentMusic
        music = currentMusic.value
        hasSubscribed = false
        progress = 0
        duration = 0
    }

    val musicListDetail = Transformations.switchMap(musicListIdLiveData) {
        Repository.getPlaylistDetails(it, TimeUtil.getTimestamp())
    }

    val onlineMusic = Transformations.switchMap(onlineMusicIdLiveData) {
        Repository.getOnlineMusic(it)
    }

    val isSubscribed = Transformations.switchMap(isSubscribedLiveData) {
        Repository.getPlaylistDetailDynamic(it, TimeUtil.getTimestamp())
    }

    val subscribed = Transformations.switchMap(subscribedLiveData) {
        Repository.subscribePlayList(if (hasSubscribed) 2 else 1, it, TimeUtil.getTimestamp())
    }

    fun getMusicListDetail(id: Long) {
        musicListIdLiveData.value = id
    }

    /**
     * 向MusicListActivity提供
     * 来获取Music资源
     */
    fun getOnlineMusic(musicIds: ArrayList<Long>) {
        onlineMusicIdLiveData.value = musicIds
    }

    /**
     * 向MusicListActivity提供
     * 来绑定PlayService
     */
    fun bindPlayService(context: Context) {
        bindService<PlayService>(context, mConnection) {
            type = context.packageName
        }
    }

    /**
     * setPlaylistEvent
     */
    fun setPlaylistEvent(ple: PlaylistEvent) {
        playService?.setPlaylistEvent(ple)
    }

    fun playPre() {
        playService?.playLast()
    }

    fun playOrPause() {
        if (playService?.isPlaying() == true) {
            playService?.pause()
        } else {
            playService?.play()
        }
    }

    fun playNext() {
        playService?.playNext()
    }

    fun getPlayListEvent() {
        currentPlayListEvent = playService?.getPlayListEvent()!!
    }

    fun getProgress() {
        progress = playService?.getProgress() ?: 0
    }

    fun getDuration() {
        duration = playService?.getDuration() ?: 0
    }

    fun isSubscribed(id: Long) {
        isSubscribedLiveData.value = id
    }

    fun subscribeMusicList() {
        subscribedLiveData.value = musicListId
    }

    fun isLogin() = Repository.isLogin.value

}