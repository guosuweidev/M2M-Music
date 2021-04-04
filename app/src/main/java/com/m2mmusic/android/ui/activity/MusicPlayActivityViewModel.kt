package com.m2mmusic.android.ui.activity

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.m2mmusic.android.logic.Repository
import com.m2mmusic.android.logic.model.Music
import com.m2mmusic.android.logic.model.PlayMode
import com.m2mmusic.android.logic.model.PlaylistEvent
import com.m2mmusic.android.ui.service.PlayService
import com.m2mmusic.android.utils.bindService
import java.util.*
import kotlin.collections.ArrayList

class MusicPlayActivityViewModel : ViewModel() {

    var currentPlaylist = ArrayList<Music>()
    var currentPlayPosition = -1
    var currentPlayMode = PlayMode.LIST
    var progress = 0
    var duration = 0
    val currentMusic: LiveData<Repository.CurrentMusic>              // 当前Music及播放状态
    val currentDuration: LiveData<Int>              // 当前Music及播放状态
    var music: Repository.CurrentMusic? = null
    private var mBinder: PlayService.PlayBinder? = null         // Binder对象
    @SuppressLint("StaticFieldLeak")
    private var playService: PlayService? = null           // PlayService对象
    var currentPlayListEvent = PlaylistEvent()

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
        currentDuration = Repository.upToDateDuration
    }

    fun initPlayListEvent(ple: PlaylistEvent) {
        currentPlaylist = ple.playlist
        currentPlayPosition = ple.playIndex
        currentPlayMode = ple.playmode
    }

    fun initProgress(p: Int) {
        progress = p
    }

    fun initDuration(d: Int) {
        duration = d
    }

    /**
     * 向MusicPlayActivity提供
     * 来绑定PlayService
     */
    fun bindPlayService(context: Context){
        bindService<PlayService>(context, mConnection) {
            type = context.packageName
        }
    }

    fun isPlaying() = playService?.isPlaying() ?: false

    fun getPlayProgress() : Int {
        progress = playService?.getProgress() ?: 0
        return progress
    }

    fun getPlayDuration() {
        duration = playService?.getDuration() ?: 0
    }

    fun seekTo(progress: Int) {
        playService?.seekTo(progress)
    }

    fun switchPlayMode() {
        currentPlayMode = playService?.switchPlayMode() ?: currentPlayMode
    }

    fun playPrv() {
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
}