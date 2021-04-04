package com.m2mmusic.android.ui.activity

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.m2mmusic.android.application.M2MMusicApplication
import com.m2mmusic.android.logic.Repository
import com.m2mmusic.android.logic.model.Music
import com.m2mmusic.android.logic.model.PlayMode
import com.m2mmusic.android.logic.model.PlaylistEvent
import com.m2mmusic.android.ui.service.PlayService
import com.m2mmusic.android.utils.bindService

class PlayListViewModel : ViewModel() {

    var progress = 0
    var duration = 0
    var currentPlaylist = ArrayList<Music>()
    var currentPlayPosition = -1
    var currentPlayMode = PlayMode.LIST
    val currentMusic: LiveData<Repository.CurrentMusic> =
        Repository.currentMusic            // 当前Music及播放状态
    private var mBinder: PlayService.PlayBinder? = null            // IBinder对象
    @SuppressLint("StaticFieldLeak")
    private var playService: PlayService? = null

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

    fun initPlayListEvent(ple: PlaylistEvent) {
        currentPlaylist = ple.playlist
        currentPlayPosition = ple.playIndex
        currentPlayMode = ple.playmode
    }

    /**
     * 向PlayListActivity提供
     * 来绑定PlayService
     */
    fun bindPlayService(context: Context){
        bindService<PlayService>(context, mConnection) {
            type = context.packageName
        }
    }

    fun getPlayListEvent() {
        with(playService?.getPlayListEvent()!!) {
            currentPlaylist = this.playlist
            currentPlayPosition = this.playIndex
            currentPlayMode = this.playmode
        }
    }
    fun getPlayPosition() {
        currentPlayPosition = playService?.getPlayPosition()!!
    }

    fun getPlayMode() {
        currentPlayMode = playService?.getPlayMode()!!
    }

    fun playIndex(position: Int) {
        playService?.play(position)
    }

    fun isServiceBounded() : Boolean {
        return playService != null
    }

    fun switchPlayMode() {
        currentPlayMode = playService?.switchPlayMode() ?: currentPlayMode
    }

    fun getProgress() {
        progress = playService?.getProgress() ?: 0
    }

    fun getDuration() {
        duration = playService?.getDuration() ?: 0
    }
}