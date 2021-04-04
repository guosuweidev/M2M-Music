package com.m2mmusic.android.application

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.gson.Gson
import com.m2mmusic.android.logic.Repository
import com.m2mmusic.android.logic.model.LoginResponse
import com.m2mmusic.android.logic.model.Music
import com.m2mmusic.android.logic.model.PlayMode
import com.m2mmusic.android.logic.model.RecommendPlaylistsResponse.Result
import com.m2mmusic.android.logic.model.UserLevelResponse
import com.m2mmusic.android.utils.launch
import com.m2mmusic.android.utils.showToast
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

object SharedPreferencesUtil {
    val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
    val masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)

    // Get SharedPreferences
    fun sharedPreferences(name: String, context: Context) = EncryptedSharedPreferences.create(
        name,
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
}

object UserResDao {

    /**
     * 保存登录信息
     * 手机号、是否记住密码、是否自动登录
     * 记住密码则保存密码
     * 设置自动登录则记录自动登录需求true
     */
    fun saveLoginInfo(
        sp: SharedPreferences,
        phone: String,
        password: String,
        savePassword: Boolean,
        autoLogin: Boolean
    ) {
        sp.edit().apply {
            putString("user_phone", phone)
            putString("user_password", password)
            putBoolean("save_password", savePassword)
            putBoolean("auto_login", autoLogin)
            apply()
        }
    }

    /**
     * 保存用户Profile
     */
    fun saveUserProfile(sp: SharedPreferences, profile: LoginResponse.Profile) {
        sp.edit().apply {
            putLong("userId", profile.userId)
            putString("backgroundUrl", profile.backgroundUrl)
            putInt("vipType", profile.vipType)
            putInt("gender", profile.gender)
            putString("avatarUrl", profile.avatarUrl)
            putLong("birthday", profile.birthday)
            putString("nickname", profile.nickname)
            putInt("city", profile.city)
            putInt("province", profile.province)
            putString("signature", profile.signature)
            putInt("followeds", profile.followeds)
            putInt("follows", profile.follows)
            apply()
        }
    }

    /**
     * 获取用户Profile
     */
    fun getUserProfile(sp: SharedPreferences): LoginResponse.Profile {
        return with(sp) {
            val defaultBackgroundUrl =
                "https://p4.music.126.net/2zSNIqTcpHL2jIvU6hG0EA==/109951162868128395.jpg"
            val defaultavatarUrl =
                "https://p4.music.126.net/ma8NC_MpYqC-dK_L81FWXQ==/109951163250233892.jpg"
            LoginResponse.Profile(
                getLong("userId", 0),
                getString("backgroundUrl", defaultBackgroundUrl) ?: defaultBackgroundUrl,
                getInt("vipType", 0),
                getInt("gender", 0),
                getString("avatarUrl", defaultavatarUrl) ?: defaultavatarUrl,
                getLong("birthday", 0),
                getString("nickname", "未知") ?: "未知",
                getInt("city", 0),
                getInt("province", 0),
                getString("signature", "无") ?: "无",
                getInt("followeds", 0),
                getInt("follows", 0)
            )
        }
    }

    /**
     * 保存用户等级信息
     */
    fun saveUserLevel(sp: SharedPreferences, userLevel: UserLevelResponse.Data) {
        sp.edit().apply {
            putInt("level", userLevel.level)
            putString("info", userLevel.info)
            putFloat("progress", userLevel.progress.toFloat())
            putInt("nextPlayCount", userLevel.nextPlayCount)
            putInt("nextLoginCount", userLevel.nextLoginCount)
            putInt("nowPlayCount", userLevel.nowPlayCount)
            putInt("nowLoginCount", userLevel.nowLoginCount)
            apply()
        }
    }

    /**
     * 获取用户等级信息
     */
    fun getUserLevel(sp: SharedPreferences): UserLevelResponse.Data {
        return with(sp) {
            UserLevelResponse.Data(
                getInt("level", 0),
                getString("info", "无") ?: "无",
                getFloat("progress", 0F).toDouble(),
                getInt("nextPlayCount", 0),
                getInt("nextLoginCount", 0),
                getInt("nowPlayCount", 0),
                getInt("nowLoginCount", 0)
            )
        }
    }

    fun getUserPhone(sp: SharedPreferences) = sp.getString("user_phone", "")

    /**
     * 获取密码
     */
    fun getUserPassword(sp: SharedPreferences) = sp.getString("user_password", "")

    /**
     * 查看是否记住密码
     */
    fun hasSavePassword(sp: SharedPreferences) = sp.getBoolean("save_password", false)

    /**
     * 查看是否设置了自动登录
     */
    fun hasSetAutoLogin(sp: SharedPreferences) = sp.getBoolean("auto_login", false)

    /**
     * 取消设置自动登录
     */
    fun cancelAutoLogin(sp: SharedPreferences) {
        sp.edit().apply() {
            putBoolean("auto_login", false)
            apply()
        }
    }

    /**
     * 缓存Cookie
     */
    fun saveCookie(sp: SharedPreferences) {
        sp.edit().putString("user_cookie", Repository.getCookie()).apply()
    }

    /**
     * 获取Cookie
     */
    fun getCookie(sp: SharedPreferences) = sp.getString("user_cookie", "")

    /**
     * 清空数据
     */
    fun clear(sp: SharedPreferences) {
        val editor = sp.edit()
        editor.clear()
        editor.apply()
    }
}

/*object PlaylistDao {
    private val musicDao = AppDatabase.getDatabase(M2MMusicApplication.context).musicDao()

    fun savePlaylist(playlist: ArrayList<Long>) {
        launch({
            val result = ArrayList<Deferred<Long>>()
            withContext(Dispatchers.IO) {
                musicDao.clearPlaylist()
            }
            withContext(Dispatchers.IO) {
                playlist.forEach { musicId ->
                    if (musicId in 1..10000) {
                        result.add(async {
                            musicDao.insertMusic(
//                                Repository.getLocalMusic(musicId) ?: Music()
                                Music()
                            )
                        })
                    } else {
                        result.add(async {
                            musicDao.insertMusic(
                                Repository.getOnlineMusic(musicId) ?: Music()
                            )
                        })
                    }
                }
                result.forEach { r ->
                    r.await()
                }
            }
        }, {
            it.message?.showToast(M2MMusicApplication.context)
        })

    }

    fun getPlaylist(): ArrayList<Long> {
        val playlist = ArrayList<Long>()
        launch({
            val result = ArrayList<Music>()

            withContext(Dispatchers.IO) {
                musicDao.loadPlaylist() as ArrayList<Music>
            }
            for (music in result) {
                playlist.add(music.songId)
            }

        }, {
            it.message?.showToast(M2MMusicApplication.context)
        })
        return playlist
    }

}*/
