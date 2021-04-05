package com.m2mmusic.android.ui.fragment

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

class SearchFragmentViewModel: ViewModel() {

    private val searchMusicLiveData = MutableLiveData<String>()
    private val searchAlbumLiveData = MutableLiveData<String>()
    private val searchArtistLiveData = MutableLiveData<String>()
    private val searchMusicListLiveData = MutableLiveData<String>()
    private val musicToPlayLiveData = MutableLiveData<Long>()
    val currentMusic: LiveData<Repository.CurrentMusic>            // 当前Music及播放状态
    private var mBinder: PlayService.PlayBinder? = null            // IBinder对象
    @SuppressLint("StaticFieldLeak")
    private var playService: PlayService? = null
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
        mBinder = null
        currentMusic = Repository.currentMusic
        progress = 0
        duration = 0
    }

    val searchMusicResponse = Transformations.switchMap(searchMusicLiveData) {
        Repository.searchMusic(it, TimeUtil.getTimestamp())
    }
    val searchAlbumResponse = Transformations.switchMap(searchAlbumLiveData) {
        Repository.searchAlbum(it, TimeUtil.getTimestamp())
    }
    val searchArtistResponse = Transformations.switchMap(searchArtistLiveData) {
        Repository.searchArtist(it, TimeUtil.getTimestamp())
    }
    val searchMusicListResponse = Transformations.switchMap(searchMusicListLiveData) {
        Repository.searchMusicList(it, TimeUtil.getTimestamp())
    }
    val musicToPlayResponse = Transformations.switchMap(musicToPlayLiveData) {
        val ids = ArrayList<Long>()
        ids.add(it)
        Repository.getOnlineMusic(ids)
    }

    fun searchMusic(keyWords: String) {
        searchMusicLiveData.value = keyWords
    }

    fun searchAlbum(keyWords: String) {
        searchAlbumLiveData.value = keyWords
    }

    fun searchArtist(keyWords: String) {
        searchArtistLiveData.value = keyWords
    }

    fun searchMusicList(keyWords: String){
        searchMusicListLiveData.value = keyWords
    }

    fun getMusicToPlay(id: Long) {
        musicToPlayLiveData.value = id
    }

    /**
     * 向SearchFragment提供
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

    fun getProgress() {
        progress = playService?.getProgress() ?: 0
    }

    fun getDuration() {
        duration = playService?.getDuration() ?: 0
    }
}