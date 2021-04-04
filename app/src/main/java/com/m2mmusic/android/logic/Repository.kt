package com.m2mmusic.android.logic

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import com.m2mmusic.android.application.*
import com.m2mmusic.android.logic.model.*
import com.m2mmusic.android.logic.network.M2MMusicNetwork
import com.m2mmusic.android.utils.LogUtil
import com.m2mmusic.android.utils.TimeUtil
import com.m2mmusic.android.utils.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlin.coroutines.CoroutineContext
import kotlin.text.StringBuilder

private const val TAG = "Repository"

object Repository {

    /**
     * 服务器返回code
     */
    const val SUCCESS_CODE = 200
    const val USER_NOT_EXIST_CODE = 501
    const val PASSWORD_WRONG_CODE = 502
    const val NEED_LOGIN_CODE = 301

    const val RECOMMENDLIMIT = 18       // 「推荐歌单」数量

    private var _isLogin = MutableLiveData<Boolean>()
    val isLogin: LiveData<Boolean>
        get() = _isLogin
    private var user: LoginResponse.Profile? = null     // 用户信息
    private var userLevel: UserLevelResponse.Data? = null     // 用户等级信息
    private var cookie: String = ""  // 登录后保存的用户cookie
    private val sharedPreferences = SharedPreferencesUtil.sharedPreferences(
        "user_info",
        M2MMusicApplication.context
    )
    private var userLikeList =  ArrayList<Music>()
//    private var userLikeListMap =  HashMap<Long, Music>()
    private var userLikeListSet =  HashSet<Long>()

    // 用于传递当前歌曲的总时长
    private val _upToDateDuration = MutableLiveData<Int>()
    val upToDateDuration: LiveData<Int>
        get() = _upToDateDuration

    // 用于传递当前音乐、播放状态
    private val _currentMusic = MutableLiveData<CurrentMusic>()
    val currentMusic: LiveData<CurrentMusic>
        get() = _currentMusic

    /**
     * 初始化
     */
    init {
        // 重置一些数据
        _currentMusic.value = CurrentMusic(false, null)
        _upToDateDuration.value = 0
        _isLogin.value = false
        cookie = ""
    }

    /**
     * 获取和记录登录状态标记
     */
    fun getLoginState() = _isLogin.value
    fun setLoginState(s: Boolean) {
        _isLogin.value = s
    }

    /**
     * 获取和保存当前登录的用户信息
     */
    fun getUserProfile() = user
    fun setUserProfile(userProfile: LoginResponse.Profile?) {
        user = userProfile
    }

    /**
     * 获取和保存当前登录的用户等级信息
     */
    fun getUserLevel() = userLevel
    fun setUserLevel(userLevel: UserLevelResponse.Data?) {
        this.userLevel = userLevel
    }

    /**
     * 获取和保存cookie信息
     */
    fun getCookie() = cookie
    fun setCookie(cookie: String) {
        this.cookie = cookie
    }

    /**
     * 获取和设置用户喜爱音乐列表
     */
    fun setUserLikeList(musicList: ArrayList<Music>) {
        userLikeList = musicList
        for (music in musicList) {
//            userLikeListMap[music.songId] = music
            userLikeListSet.add(music.songId)
        }
    }
    fun getUserLikeList() = userLikeList
    fun canMusicBeLiked(id:Long) = userLikeListSet.contains(id)

    /**
     * 设置UpToDateDuration
     */
    fun setUpToDateDuration(utd: Int) {
        _upToDateDuration.value = utd
    }

    /**
     * 设置CurrentMusic
     */
    fun setCurrentMusic(cm: CurrentMusic) {
        _currentMusic.value = cm
    }

    /**
     * 手机号登录
     */
    fun loginByPhone(phone: String, password: String) = fire(Dispatchers.IO) {
        val loginResponse =
            M2MMusicNetwork.loginByPhone("86", phone, password, TimeUtil.getTimestamp())
        when (loginResponse.code) {
            SUCCESS_CODE -> {
                getUserLevel(TimeUtil.getTimestamp())
                Result.success(loginResponse)
            }
            USER_NOT_EXIST_CODE -> {
                "登录失败，手机号不正确或未注册".showToast(M2MMusicApplication.context)
                Result.failure(RuntimeException("response code is ${loginResponse.code}"))
            }
            PASSWORD_WRONG_CODE -> {
                "登录失败，密码不正确".showToast(M2MMusicApplication.context)
                Result.failure(RuntimeException("response code is ${loginResponse.code}"))
            }
            else ->
                Result.failure(RuntimeException("response code is ${loginResponse.code}"))
        }
    }

    /**
     * 退出登录
     */
    fun logout(timestamp: Long) = fire(Dispatchers.IO) {
        val logoutResponse = M2MMusicNetwork.logout(timestamp)
        when (logoutResponse.code) {
            SUCCESS_CODE -> Result.success(true)
            else -> Result.failure(RuntimeException("response code is ${logoutResponse.code}"))
        }
    }

    /**
     * 获取用户等级信息
     */
    fun getUserLevel(timestamp: Long) = fire(Dispatchers.IO) {
        val levelResponse = M2MMusicNetwork.getUserLevel(timestamp)
        when (levelResponse.code) {
            SUCCESS_CODE -> {
                setUserLevel(levelResponse.data)
                if (UserResDao.hasSetAutoLogin(sharedPreferences))
                    UserResDao.saveUserLevel(sharedPreferences, levelResponse.data)
                Result.success(true)
            }
            NEED_LOGIN_CODE -> {
                "无法获取等级信息，需要登录".showToast(M2MMusicApplication.context)
                Result.failure(RuntimeException("response code is ${levelResponse.code}"))
            }
            else -> Result.failure(RuntimeException("response code is ${levelResponse.code}"))
        }
    }

    /**
     * 收藏/取消收藏歌单
     */
    fun subscribePlayList(t: Int, id: Long, timestamp: Long) = fire(Dispatchers.IO) {
        val subscribePlayListResponse = M2MMusicNetwork.subscribePlayList(t, id, timestamp)
        when (subscribePlayListResponse.code) {
            SUCCESS_CODE -> Result.success(true)
            401 -> {
                "请先登录".showToast(M2MMusicApplication.context)
                Result.failure(RuntimeException("response code is ${subscribePlayListResponse.code}"))
            }
            501 -> {
                "您已收藏过该歌单".showToast(M2MMusicApplication.context)
                Result.failure(RuntimeException("response code is ${subscribePlayListResponse.code}"))
            }
            else -> Result.failure(RuntimeException("response code is ${subscribePlayListResponse.code}"))
        }
    }

    /**
     * 获取用户喜欢的音乐列表
     */
    fun getUserLikeList(uid:Long, timestamp: Long) = fire(Dispatchers.IO) {
        val userLikeListResponse = M2MMusicNetwork.getUserLikeList(uid, timestamp)
        when (userLikeListResponse.code) {
            SUCCESS_CODE -> {
                Result.success(userLikeListResponse.ids)
            }
            else -> {
                Result.failure(RuntimeException("response code is ${userLikeListResponse.code}"))
            }
        }
    }

    /**
     * 获取用户的歌单
     */
    fun getUserMusicList(uid: Long, timestamp: Long) = fire(Dispatchers.IO) {
        val userPlayListResponse = M2MMusicNetwork.getUserMusicList(uid, timestamp)
        when (userPlayListResponse.code) {
            SUCCESS_CODE -> {
                val result = userPlayListResponse.playlist
                Result.success(result)
            }
            else -> {
                Result.failure(RuntimeException("response code is ${userPlayListResponse.code}"))
            }
        }
    }

    /**
     * 获取本地Music资源
     */
    /*fun getLocalMusic(id: Long): Music?{
        获取本地音乐
    }*/

    /**
     * 获取网络Music资源
     * 获取歌曲信息、歌曲资源URL，并整合成Music对象
     */
    fun getOnlineMusic(id: ArrayList<Long>) = fire(Dispatchers.IO) {
        coroutineScope {
            val timestamp = TimeUtil.getTimestamp()
            val musiclist = ArrayList<Music>()
            val ids = id.toString().replace(" ", "")
                .replace("[", "").replace("]", "")
            val musicDetailResponse = async { getDetailOfSong(ids, timestamp) }
            val musicUrlResponse = async { getUrlOfSong(ids, timestamp) }
            val detail = musicDetailResponse.await()
            LogUtil.e(TAG, detail.toString())
            val url = musicUrlResponse.await()
            LogUtil.e(TAG, url.toString())
            if (detail.code == SUCCESS_CODE) {
                for (i in (0 until (detail.songs.size))) {
                    val music = Music(
                        0,
                        id[i],
                        detail.songs[i].name,
                        detail.songs[i].ar.run {
                            var a = this.size - 1
                            val artists: StringBuilder = StringBuilder(this[a].name)
                            for (artist in this) {
                                if (a > 0) {
                                    artists.append("/").append(artist.name)
                                    a--
                                }
                            }
                            artists.toString()
                        },
                        detail.songs[i].al.picUrl,
                        0L,
                        detail.songs[i].al.name,
                        0,
                        url.data[i].url
                    )
                    musiclist.add(music)
                }
                LogUtil.e(TAG, "Musiclist: $musiclist")
                Result.success(musiclist)
            } else {
                Result.failure(RuntimeException("response code is ${detail.code}"))
            }
        }
    }

    /**
     * 获取歌曲详情
     * 用于整合Music对象
     */
    private suspend fun getDetailOfSong(ids: String, timestamp: Long) =
        M2MMusicNetwork.getDetailOfSong(ids, timestamp)

    /**
     * 获取歌曲资源URL
     * 用于整合Music对象
     */
    private suspend fun getUrlOfSong(ids: String, timestamp: Long) =
        M2MMusicNetwork.getUrlOfSong(ids, timestamp)

    /**
     * 获取新歌、新碟、数字专辑
     */
    fun getNewResources(timestamp: Long) = fire(Dispatchers.IO) {
        val newResourcesResponse = M2MMusicNetwork.getNewResources(timestamp)
        if (newResourcesResponse.code == SUCCESS_CODE) {
            val result = newResourcesResponse.data.blocks[7].creatives
            Result.success(result)
        } else {
            Result.failure(RuntimeException("response code is ${newResourcesResponse.code}"))
        }
    }

    /**
     * 获取推荐歌单
     */
    fun getRecommendPlaylists(limit: Int, timestamp: Long) = fire(Dispatchers.IO) {
        val recommendPlaylistsResponse = M2MMusicNetwork.getRecommendPlaylists(limit, timestamp)
        if (recommendPlaylistsResponse.code == SUCCESS_CODE) {
            val results = recommendPlaylistsResponse.result
            Result.success(results)
        } else {
            Result.failure(RuntimeException("response code is ${recommendPlaylistsResponse.code}"))
        }
    }

    /**
     * 获取歌单详细信息
     */
    fun getPlaylistDetails(id: Long, timestamp: Long) = fire(Dispatchers.IO) {
        val playlistDetailResponse = M2MMusicNetwork.getPlaylistDetails(id, timestamp)
        if (playlistDetailResponse.code == SUCCESS_CODE) {
            val playlist = playlistDetailResponse.playlist
            Result.success(playlist)
        } else {
            Result.failure(RuntimeException("response code is ${playlistDetailResponse.code}"))
        }
    }

    /**
     * 获取歌单详细信息
     */
    fun getAlbumDetails(id: Long, timestamp: Long) = fire(Dispatchers.IO) {
        val albumDetailResponse = M2MMusicNetwork.getAlbumDetails(id, timestamp)
        if (albumDetailResponse.code == SUCCESS_CODE) {
            Result.success(albumDetailResponse)
        } else {
            Result.failure(RuntimeException("response code is ${albumDetailResponse.code}"))
        }
    }

    /**
     * 获取歌单详情（动态信息）
     */
    fun getPlaylistDetailDynamic(id: Long, timestamp: Long) = fire(Dispatchers.IO) {
        val playlistDetailDynamicResponse = M2MMusicNetwork.getPlaylistDetailDynamic(id, timestamp)
        if (playlistDetailDynamicResponse.code == SUCCESS_CODE) {
            Result.success(playlistDetailDynamicResponse)
        } else {
            Result.failure(RuntimeException("response code is ${playlistDetailDynamicResponse.code}"))
        }
    }

    /**
     * 按照liveData()函数的参数接收标准定义的一个高阶函数
     * 简化网络回调
     */
    private fun <T> fire(context: CoroutineContext, block: suspend () -> Result<T>) =
        liveData<Result<T>>(context) {
            val result = try {
                block()
            } catch (e: Exception) {
                Result.failure<T>(e)
            }
            emit(result)
        }

    /**
     * 缓存
     */
    /*fun saveRecPlaylists(playlists: List<RecommendPlaylistsResponse.Result>) =
        MainPageResDao.saveRecPlaylists(
            playlists
        )

    fun getRecPlaylists() = MainPageResDao.getRecPlaylists()

    fun isRecPlaylistsSaved() = MainPageResDao.isRecPlaylistsSaved()

    fun savePlaylistEvent() {
        if (playlistEvent.value?.playlist == null) return
        _playlistEvent.value?.let {
            it.playlist?.let { pl -> PlaylistDao.savePlaylist(pl) }
            PlayerDao.savePlaylistEvent(
                it.playIndex, when (it.playmode) {
                    PlayMode.SINGLE -> 0
                    PlayMode.LIST -> 1
                    PlayMode.SHUFFLE -> 2
                    else -> 1
                }
            )
        }
    }

    fun saveCurrentMusic() {
        if (_currentMusic.value?.music == null) return
        _currentMusic.value?.let {
            PlayerDao.saveCurrentMusic(
                it.isPlaying, it.music!!
            )
        }
    }*/

    /**
     * 当前音乐、播放状态集成的数据类
     */
    data class CurrentMusic(
        var isPlaying: Boolean,
        var music: Music? = null
    )

}