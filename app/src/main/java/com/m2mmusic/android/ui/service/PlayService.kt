package com.m2mmusic.android.ui.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.m2mmusic.android.R
import com.m2mmusic.android.application.M2MMusicApplication
import com.m2mmusic.android.logic.Repository
import com.m2mmusic.android.logic.model.Music
import com.m2mmusic.android.logic.model.PlayMode
import com.m2mmusic.android.logic.model.PlaylistEvent
import com.m2mmusic.android.ui.activity.MainActivity
import com.m2mmusic.android.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import wseemann.media.FFmpegMediaMetadataRetriever
import java.lang.Exception
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.concurrent.thread

class PlayService : Service(), AudioFocusManager.OnAudioFocusChangeListener,
    MediaPlayer.OnCompletionListener, MediaPlayer.OnBufferingUpdateListener {

    private val context = M2MMusicApplication.context
    private val notificationManager: NotificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
    private val mediaPlayer: MediaPlayer by lazy {
        MediaPlayer()
    }
    private val audioFocusManager: AudioFocusManager by lazy {
        AudioFocusManager(context)
    }
    private var playOnAudioFocus = false
    private var isPlayingBeforeLoseFocuse = false                        // 播放器失去音乐焦点前是否正在播放
    private var isVolumeDownBeforeLoseFocuse = false                        // 播放器失去音乐焦点时是否降低了音量
    private var playlist: ArrayList<Music> = ArrayList()
    private var music: Music? = null
    private var mode: PlayMode = PlayMode.LIST
    private var position: Int = 0
    private var duration: Int = 0
    private lateinit var remoteView: RemoteViews
    private val mBinder = PlayBinder()

    inner class PlayBinder : Binder() {
        fun getService() = this@PlayService
    }

    override fun onCreate() {
        super.onCreate()
        LogUtil.e(TAG, "onCreate executed")
        // 创建前台Service
        launch({
            updateNotification()
        }, {
            it.message?.showToast(context)
        })
        // 注册Broadcast
        IntentFilter().apply {
            addAction(ACTION_PLAY)
            addAction(ACTION_PAUSE)
            addAction(ACTION_NEXT)
            addAction(ACTION_PRV)
            addAction(ACTION_CLOSE)
            registerReceiver(playMusicReceiver, this)
        }
        // MediaPlayer状态监听及相关设置
        mediaPlayer.let {
            it.setOnCompletionListener(this)
            it.setOnBufferingUpdateListener(this)
            it.setWakeMode(
                M2MMusicApplication.context,
                PowerManager.PARTIAL_WAKE_LOCK
            )
        }
        audioFocusManager.setOnAudioFocusChangeListener(this)
    }

    override fun onBind(intent: Intent?): IBinder {
        LogUtil.e(TAG, "Bind Service")
        return mBinder
    }

    override fun onDestroy() {
        LogUtil.e(TAG, "Destroy Service")
        unregisterReceiver(playMusicReceiver)
        super.onDestroy()
    }

    fun stop() {
        playlist.clear()
        position = 0
        mode = PlayMode.LIST
        releasePlayer()
        ActivityCollector.finishAll()
        unregisterReceiver(playMusicReceiver)
        stopForeground(true)
        android.os.Process.killProcess(android.os.Process.myPid())
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            // 短暂丢失焦点，如来电
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                playOnAudioFocus = false
                isPlayingBeforeLoseFocuse = mediaPlayer.isPlaying
                pause()
            }
            // 瞬间丢失焦点，如通知
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                isVolumeDownBeforeLoseFocuse = true
                mediaPlayer.setVolume(0.3F, 0.3F)
            }
            // 获得焦点
            AudioManager.AUDIOFOCUS_GAIN -> {
                playOnAudioFocus = true
                if (isPlayingBeforeLoseFocuse) {
                    mediaPlayer.start()
                    isPlayingBeforeLoseFocuse = false
                }
                if (isVolumeDownBeforeLoseFocuse) {
                    mediaPlayer.setVolume(1F, 1F)
                    isVolumeDownBeforeLoseFocuse = false
                }
            }
            // 永久丢失焦点，如被其他播放器抢占
            AudioManager.AUDIOFOCUS_LOSS -> {
                audioFocusManager.releaseAudioFocus()
                mediaPlayer.stop()
            }
        }
    }

    override fun onCompletion(mp: MediaPlayer?) {
        when (mode) {
            PlayMode.LIST -> playNext()
            PlayMode.SHUFFLE -> {
                position = (0 until playlist.size).random()
                play(position)
            }
            PlayMode.SINGLE -> play(position)
        }
    }

    override fun onBufferingUpdate(mp: MediaPlayer?, percent: Int) {
        if (percent != 100) {
            LogUtil.e(TAG, "正在缓冲网络音乐")
        }
    }

    /**
     * 设置播放任务
     */
    fun setPlaylistEvent(ple: PlaylistEvent) {
        playlist = ple.playlist
        position = ple.playIndex
        mode = ple.playmode
        if (!play(position)) {
            "播放失败".showToast(context)
        }
    }

    /**
     * 播放（设定位置）
     */
    fun play(startIndex: Int): Boolean {
        LogUtil.e(TAG, "准备播放")
        position = startIndex
        while (!playOnAudioFocus) {
            // 确保获取音乐焦点
            playOnAudioFocus = requestFocus()
            Thread.sleep(3000)
        }
        LogUtil.e(TAG, "取得焦点")
        music = playlist[position]
        if (music == null) {
            LogUtil.e(TAG, "获取资源失败")
            return false
        } else if (music!!.path == null) {
            if (music!!.type == 0)
                "付费音乐，已自动播放下一首".showToast(context)
            else
                "本地音乐资源损毁！".showToast(context)
            playNext()
        } else {
            try {
                LogUtil.e(TAG, "开始播放")
                mediaPlayer.apply {
                    reset()
                    setDataSource(music!!.path)
                    prepare()
                    start()
                }
                initDuration()
                Repository.setCurrentMusic(Repository.CurrentMusic(true, music))
                launch({
                    updateNotification()
                }, {
                    it.message?.showToast(context)
                })
            } catch (e: Exception) {
                "Play:$e".showToast(context)
                Repository.setCurrentMusic(Repository.CurrentMusic(false, null))
                return false
            }
        }
        return true
    }

    /**
     * 播放上一首
     */
    fun playLast(): Boolean {
        when (mode) {
            PlayMode.LIST -> {
                if (position == 0) {
                    position = playlist.size - 1
                    play(position)
                    return true
                } else {
                    play(--position)
                    return true
                }
            }
            PlayMode.SINGLE -> {
                play(position)
                return true
            }
            PlayMode.SHUFFLE -> {
                position = (0 until playlist.size).random()
                play(position)
                return true
            }
        }
    }

    /**
     * 播放下一首
     */
    fun playNext(): Boolean {
        when (mode) {
            PlayMode.LIST -> {
                if (position == playlist.size - 1) {
                    position = 0
                    play(position)
                    return true
                } else {
                    play(++position)
                    return true
                }
            }
            PlayMode.SINGLE -> {
                play(position)
                return true
            }
            PlayMode.SHUFFLE -> {
                position = (0 until playlist.size).random()
                play(position)
                return true
            }
        }
    }

    /**
     * 暂停
     */
    fun pause(): Boolean {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            Repository.setCurrentMusic(Repository.CurrentMusic(false, music))
        }
        return true
    }

    /**
     * 继续播放
     */
    fun play(): Boolean {
        if (music == null) {
            return false
        }
        while (!playOnAudioFocus) {
            // 确保获取音乐焦点
            playOnAudioFocus = requestFocus()
            Thread.sleep(3000)
        }
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.start()
            Repository.setCurrentMusic(Repository.CurrentMusic(true, music))
        }
        return true
    }

    /**
     * 跳转到某个播放进度
     */
    fun seekTo(progress: Int): Boolean {
        return if (mediaPlayer.isPlaying) {
            if (mediaPlayer.duration <= progress) {
                onCompletion(mediaPlayer)
            } else {
                mediaPlayer.seekTo(progress)
            }
            true
        } else {
            play()
            "跳转失败，请在开始播放后重试".showToast(context)
            false
        }
    }

    /**
     * 获取播放进度
     */
    fun getProgress(): Int {
        return mediaPlayer.currentPosition
    }

    fun initDuration() {
        val mmr = FFmpegMediaMetadataRetriever()
        mmr.setDataSource(music?.path)
        val duration = mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION).toInt()
        mmr.release()
        this.duration = duration
        Repository.setUpToDateDuration(duration)
    }

    fun getDuration(): Int = duration

    /**
     * 设置播放顺序模式
     */
    fun setPlayMode(playMode: PlayMode) {
        mode = playMode
    }

    /**
     * 切换播放顺序模式
     */
    fun switchPlayMode() : PlayMode {
        mode = PlayMode.switchNextMode(mode)
        return mode
    }

    /**
     * 获取播放状态
     */
    fun isPlaying() = mediaPlayer.isPlaying

    /**
     * 获取当前播放列表
     */
    fun getPlaylist() = playlist

    /**
     * 获取当前播放歌曲位置
     */
    fun getPlayPosition() = position

    /**
     * 获取当前播放规则
     */
    fun getPlayMode() = mode

    /**
     * 获取当前完整播放状态信息
     */
    fun getPlayListEvent() = PlaylistEvent(playlist, position, mode)

    /**
     * 释放播放器
     */
    fun releasePlayer() {
        mediaPlayer.release()
    }

    /**
     * 更新通知栏
     */
    private suspend fun updateNotification() {
        remoteView = RemoteViews(packageName, R.layout.view_notification)
        if (music != null) {
            with(music!!) {
                // 加载Album Cover
                if (this.album == null) {
                    // 加载默认的Album Cover
                    remoteView.setImageViewResource(
                        R.id.widget_album,
                        R.drawable.default_cover
                    )
                } else if (this.type == 0) {
                    // 加载在线音乐的Album Cover
                    remoteView.setImageViewBitmap(
                        R.id.widget_album,
                        with(this) {
                            val bitmap =
                                withContext(Dispatchers.IO) { loadingOnlineCover(music!!.album + "?param=200y200") }
                            bitmap
                        }
                    )
                } else {
                    // 加载本地音乐的Album Cover
                }
                // 加载Title
                remoteView.setTextViewText(
                    R.id.notification_title,
                    this.title
                )
                // 加载Artists
                remoteView.setTextViewText(
                    R.id.notification_artist,
                    this.artist
                )
                // 加载Album Title
                remoteView.setTextViewText(
                    R.id.notification_album,
                    this.albumTitle
                )
                // 设置 播放/暂停按钮
                remoteView.setImageViewResource(
                    R.id.notification_play_or_pause,
                    if (mediaPlayer.isPlaying) R.drawable.ic_play else R.drawable.ic_pause
                )
            }
        }
        createNotificationChannel()
    }

    /**
     * 创建前台Service
     */
    private suspend fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = getString(R.string.app_name)
            val importance = NotificationManager.IMPORTANCE_MIN
            val channel = NotificationChannel(CHANNEL_ID, channelName, importance)
            val builder = NotificationCompat.Builder(this, CHANNEL_ID)

            // 点击通知跳转首页
            val intent = Intent(this, MainActivity::class.java)
            val intentGo =
                PendingIntent.getActivity(
                    this,
                    CODE_MAIN,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            remoteView.setOnClickPendingIntent(R.id.notification, intentGo)

            // 退出
            val close = Intent()
            close.action = ACTION_CLOSE
            val intentClose =
                PendingIntent.getBroadcast(
                    this,
                    CODE_CLOSE,
                    close,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            remoteView.setOnClickPendingIntent(R.id.notification_close, intentClose)

            // 上一首
            val prv = Intent()
            prv.action = ACTION_PRV
            val intentPrv =
                PendingIntent.getBroadcast(
                    this,
                    CODE_PRV,
                    prv,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            remoteView.setOnClickPendingIntent(R.id.notification_pre, intentPrv)

            // 播放
            if (mediaPlayer.isPlaying) {
                val playorpause = Intent()
                playorpause.action = ACTION_PAUSE
                val intent_play = PendingIntent.getBroadcast(
                    this,
                    CODE_PAUSE,
                    playorpause,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
                remoteView.setOnClickPendingIntent(R.id.notification_play_or_pause, intent_play)
            }

            // 暂停
            if (!mediaPlayer.isPlaying) {
                val playorpause = Intent()
                playorpause.action = ACTION_PLAY
                val intent_play = PendingIntent.getBroadcast(
                    this,
                    CODE_PLAY,
                    playorpause,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
                remoteView.setOnClickPendingIntent(R.id.notification_play_or_pause, intent_play)
            }

            // 下一首
            val next = Intent()
            next.action = ACTION_NEXT
            val intent_next =
                PendingIntent.getBroadcast(
                    this,
                    CODE_NEXT,
                    next,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            remoteView.setOnClickPendingIntent(R.id.notification_next, intent_next)
            builder.setCustomContentView(remoteView)
                .setSmallIcon(R.drawable.ic_album)
                .setContentTitle("M2M音乐")
            notificationManager.createNotificationChannel(channel)
            startForeground(NOTIFICATION_ID, builder.build())
        }
    }

    /**
     * 处理通知栏播放器点击事件
     */
    private val playMusicReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            if (!action.isNullOrEmpty()) {
                when (action) {
                    ACTION_CLOSE -> {
                        LogUtil.e(TAG, "通知栏——退出")
                        stop()
                    }
                    ACTION_PRV -> {
                        LogUtil.e(TAG, "通知栏——上一首")
                        playLast()
                        launch({
                            updateNotification()
                        }, {
                            if (context != null) {
                                it.message?.showToast(context)
                            }
                        })
                    }
                    ACTION_NEXT -> {
                        LogUtil.e(TAG, "通知栏——下一首")
                        playNext()
                        launch({
                            updateNotification()
                        }, {
                            if (context != null) {
                                it.message?.showToast(context)
                            }
                        })
                    }
                    ACTION_PAUSE -> {
                        LogUtil.e(TAG, "通知栏——暂停")
                        pause()
                        launch({
                            updateNotification()
                        }, {
                            if (context != null) {
                                it.message?.showToast(context)
                            }
                        })
                    }
                    ACTION_PLAY -> {
                        LogUtil.e(TAG, "通知栏——播放")
                        play()
                        launch({
                            updateNotification()
                        }, {
                            if (context != null) {
                                it.message?.showToast(context)
                            }
                        })
                    }
                }
            }
        }

    }

    /**
     * 请求焦点
     */
    private fun requestFocus(): Boolean {
        // Request audio focus for playService
        return audioFocusManager.requestFocus() == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    companion object {
        private const val TAG = "PlayService"
        private const val CHANNEL_ID = "com.m2mmusic.android.notification.channel"
        private const val NOTIFICATION_ID = 1
        private const val ACTION_CLOSE = "com.m2mmusic.android.ACTION.CLOSE_APP"
        private const val ACTION_PRV = "com.m2mmusic.android.ACTION.PLAY_LAST"
        private const val ACTION_NEXT = "com.m2mmusic.android.ACTION.PLAY_NEXT"
        private const val ACTION_PAUSE = "com.m2mmusic.android.ACTION.PAUSE"
        private const val ACTION_PLAY = "com.m2mmusic.android.ACTION.PLAY"

        private const val CODE_MAIN = 0
        private const val CODE_CLOSE = 1
        private const val CODE_PRV = 2
        private const val CODE_PAUSE = 3
        private const val CODE_PLAY = 4
        private const val CODE_NEXT = 5
    }

}