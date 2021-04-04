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
import com.m2mmusic.android.logic.Repository
import com.m2mmusic.android.logic.model.Music
import com.m2mmusic.android.logic.model.PlaylistEvent
import com.m2mmusic.android.ui.service.PlayService
import com.m2mmusic.android.utils.TimeUtil
import com.m2mmusic.android.utils.bindService

class AlbumActivityViewModel :ViewModel() {

    private val onlineMusicIdLiveData = MutableLiveData<ArrayList<Long>>()
    private val albumIdLiveData = MutableLiveData<Long>()
    private var mBinder: PlayService.PlayBinder? = null            // IBinder对象
    @SuppressLint("StaticFieldLeak")
    private var playService: PlayService? = null
    val currentMusic: LiveData<Repository.CurrentMusic>            // 当前Music及播放状态
    val musicList = ArrayList<Music>()
    var music: Repository.CurrentMusic? = null
    var currentPlayListEvent = PlaylistEvent()
    var progress = 0
    var duration = 0

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
        currentMusic = Repository.currentMusic
        music = currentMusic.value
    }

    val albumDetail = Transformations.switchMap(albumIdLiveData) {
        Repository.getAlbumDetails(it, TimeUtil.getTimestamp())
    }

    val onlineMusic = Transformations.switchMap(onlineMusicIdLiveData) {
        Repository.getOnlineMusic(it)
    }

    /**
     * 向AlbumActivity提供
     * 来获取Album详细信息
     */
    fun getAlbumDetail(id: Long) {
        albumIdLiveData.value = id
    }

    /**
     * 向AlbumActivity提供
     * 来获取Music资源
     */
    fun getOnlineMusic(musicIds: ArrayList<Long>) {
        onlineMusicIdLiveData.value = musicIds
    }

    /**
     * 向AlbumActivity提供
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
}