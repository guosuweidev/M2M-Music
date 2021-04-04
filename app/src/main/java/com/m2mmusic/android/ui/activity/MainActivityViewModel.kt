package com.m2mmusic.android.ui.activity

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.os.IBinder
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.m2mmusic.android.application.M2MMusicApplication
import com.m2mmusic.android.application.SharedPreferencesUtil
import com.m2mmusic.android.application.UserResDao
import com.m2mmusic.android.logic.Repository
import com.m2mmusic.android.logic.model.LoginResponse
import com.m2mmusic.android.logic.model.Music
import com.m2mmusic.android.logic.model.PlaylistEvent
import com.m2mmusic.android.logic.model.UserLevelResponse
import com.m2mmusic.android.ui.service.PlayService
import com.m2mmusic.android.utils.TimeUtil
import com.m2mmusic.android.utils.bindService
import com.m2mmusic.android.utils.encode

class MainActivityViewModel : ViewModel() {

    private val _currentTab = MutableLiveData<Int>()             // 指定当前Fragment
    val currentTab: LiveData<Int>
        get() = _currentTab
    private var mBinder: PlayService.PlayBinder? = null         // Binder对象

    @SuppressLint("StaticFieldLeak")
    private var playService: PlayService? = null              // PlayService对象
    val currentMusic: LiveData<Repository.CurrentMusic>              // 当前Music及播放状态
    val isLogin: LiveData<Boolean>           // 当前登录状态
    var currentPlayListEvent = PlaylistEvent()
    var profile: LoginResponse.Profile? = null
    var userLevel: UserLevelResponse.Data? = null
    private val onlineMusicIdLiveData = MutableLiveData<Long>()
    private val userLevelLiveData = MutableLiveData<Long>()
    var music: Repository.CurrentMusic? = null
    var progress = 0
    var duration = 0
    private lateinit var sharedPreferences: SharedPreferences
    private val loginByPhoneLiveData = MutableLiveData<Int>()

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

    /**
     * 初始化
     * 获取播放器播放状态信息
     * 重置持有的参数及对象
     */
    init {
        _currentTab.value = 0
        mBinder = null
        currentMusic = Repository.currentMusic
        isLogin = Repository.isLogin
    }

    /**
     * 向MainActivity提供
     * 来获取当前的Music
     */
    fun getCurrentMusic() = currentMusic.value

    /**
     * 向MainActivity提供
     * 来获取Music资源
     */
    fun getOnlineMusic(musicId: Long) {
        onlineMusicIdLiveData.value = musicId
    }

    /**
     * 向MainActivity提供
     * 来绑定PlayService
     */
    fun bindPlayService(context: Context) {
        bindService<PlayService>(context, mConnection) {
            type = context.packageName
        }
    }

    /**
     * 向MainActivity提供
     * 来设置currentTab
     */
    fun setCurrentTab(position: Int) {
        _currentTab.value = position
    }

    /**
     * stopService()
     */
    fun stopService() {
        playService?.stop()
    }

    /**
     * setPlaylistEvent
     */
    fun setPlaylistEvent(ple: PlaylistEvent) {
        playService?.setPlaylistEvent(ple)
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

    fun getUserProfile() {
        profile = Repository.getUserProfile()
    }

    fun getUserLevel() {
        userLevel = Repository.getUserLevel()
    }

    fun clearUserProfile() {
        profile = null
    }

    fun clearUserLevel() {
        userLevel = null
    }

    fun getUserLevelResponse() {
        userLevelLiveData.value = TimeUtil.getTimestamp()
    }

    val userLevelResponse = Transformations.switchMap(userLevelLiveData) {
        Repository.getUserLevel(it)
    }

    val loginResponse = Transformations.switchMap(loginByPhoneLiveData) {
        sharedPreferences =
            SharedPreferencesUtil.sharedPreferences("user_info", M2MMusicApplication.context)
        Repository.loginByPhone(
            UserResDao.getUserPhone(sharedPreferences)!!,
            encode(UserResDao.getUserPassword(sharedPreferences)!!)
        )
    }

    /**
     * 自动登录
     */
    fun login() {
        loginByPhoneLiveData.value = loginByPhoneLiveData.value
    }

    /**
     * 退出登录
     */
    fun logout() {
        Repository.apply {
            logout(TimeUtil.getTimestamp())
            setCookie("")
            setLoginState(false)
            setUserLevel(null)
            setUserProfile(null)
        }
        sharedPreferences =
            SharedPreferencesUtil.sharedPreferences("user_info", M2MMusicApplication.context)
//        UserResDao.clear(sharedPreferences)
        UserResDao.cancelAutoLogin(sharedPreferences)
    }

    fun setCookie(cookie:String){
        Repository.setCookie(cookie)
    }
    fun setUserProfile(profile: LoginResponse.Profile) {
        Repository.setUserProfile(profile)
    }
    fun setLoginState(s: Boolean) {
        Repository.setLoginState(s)
    }

}