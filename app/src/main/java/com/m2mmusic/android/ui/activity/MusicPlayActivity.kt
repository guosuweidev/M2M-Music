package com.m2mmusic.android.ui.activity

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.widget.SeekBar
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.m2mmusic.android.R
import com.m2mmusic.android.databinding.ActivityMusicPlayBinding
import com.m2mmusic.android.logic.Repository
import com.m2mmusic.android.logic.model.Music
import com.m2mmusic.android.logic.model.PlayMode
import com.m2mmusic.android.ui.service.PlayService
import com.m2mmusic.android.utils.LogUtil
import com.m2mmusic.android.utils.showToast
import com.m2mmusic.android.utils.startActivity
import java.util.*

/**
 * Created by 小小苏
 * M2M Music的音乐播放页
 */
class MusicPlayActivity : BaseActivity() {

    private lateinit var binding: ActivityMusicPlayBinding                  // 注册binding对象
    private lateinit var viewModel: MusicPlayActivityViewModel              // 注册viewModel对象
    private var currentMusic: Repository.CurrentMusic? = null               // 注册当前播放的Music对象
    private var playService: PlayService.PlayBinder? = null        // 注册PlayService的IBinder对象
    // 消息处理
    private val mHandler = Handler()
    // 播放进度消息回调接收
    private var mProgressCallback = object : Runnable {
        override fun run() {
            if (viewModel.isPlaying()) {
                val p = 100 * viewModel.getPlayProgress() / viewModel.duration
                updateProgress()
                if (p in 0..100) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        binding.playSeekbar.setProgress(p, true)
                    } else {
                        binding.playSeekbar.progress = p
                    }
                    mHandler.postDelayed(this, UPDATE_PROGRESS_INTERVAL)
                }
            }
        }
    }

    /**
     * onCreate()
     * 1. 利用生成的ViewBinding对象绑定视图
     * 2. 设置对一些LiveData的观察
     * 3. 绑定服务PlayService
     * 4. 获取当前播放歌曲
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogUtil.e(TAG, "onCreate")
        // 1
        setViewBinding()
        // 2
        viewModel.apply {
            initPlayListEvent(intent.getParcelableExtra("play_list_event"))
            initProgress(intent.getIntExtra("progress", 0))
            initDuration(intent.getIntExtra("duration", 0))
        }
        initView(
            intent.getBooleanExtra("is_playing", false),
            intent.getParcelableExtra("now_playing")
        )

        viewModel.currentMusic.observe(this) {
            if (it.music != null) {
                onMusicUpdated(it)
                viewModel.music = it
            }
        }

        viewModel.currentDuration.observe(this) {
            viewModel.duration = it
            updateDuration()
        }

        viewModel.bindPlayService(this)
        setClickListener()
    }

    /**
     * 利用生成的ViewBinding对象绑定视图
     */
    private fun setViewBinding() {
        binding = ActivityMusicPlayBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[MusicPlayActivityViewModel::class.java]
    }

    private fun initView(isPlaying: Boolean, nowPlaying: Music?) {
        setSupportActionBar(binding.playToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = nowPlaying?.title ?: "未知"
        binding.apply {
            playTitle.text = nowPlaying?.title ?: "未知"
            playArtists.text = nowPlaying?.artist ?: "未知"
            playOrPause.setImageResource(
                if (isPlaying) {
                    mHandler.post(mProgressCallback)
                    Glide.with(this@MusicPlayActivity).load(nowPlaying?.album)
                        .into(binding.playCover)
                    R.drawable.ic_play
                } else {
                    mHandler.removeCallbacks(mProgressCallback)
                    R.drawable.ic_pause
                }
            )
            updatePlayMode()
            updateProgress()
            updateDuration()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setClickListener() {
        // 播放、暂停
        binding.playOrPause.setOnClickListener {
            viewModel.playOrPause()
            if (viewModel.isPlaying()) {
                mHandler.removeCallbacks(mProgressCallback)
                mHandler.post(mProgressCallback)
            } else {
                mHandler.removeCallbacks(mProgressCallback)
            }
        }
        // 上一首
        binding.playPrv.setOnClickListener {
            viewModel.playPrv()
        }
        // 下一首
        binding.playNext.setOnClickListener {
            viewModel.playNext()
        }
        // 切换播放顺序
        binding.playMode.setOnClickListener {
            viewModel.switchPlayMode()
            updatePlayMode()
        }
        // 播放列表
        binding.nowPlayingList.setOnClickListener {
            viewModel.getPlayListEvent()
            startActivity<PlayListActivity>(this) {
                putExtra("play_list_event", viewModel.currentPlayListEvent)
                putExtra("is_play_activity", true)
            }
        }
        binding.playSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    viewModel.getPlayProgress()
                    updateProgress()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                mHandler.removeCallbacks(mProgressCallback)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                viewModel.seekTo(viewModel.duration/100*seekBar.progress)
                if (viewModel.isPlaying()) {
                    mHandler.removeCallbacks(mProgressCallback)
                    mHandler.post(mProgressCallback)
                }
            }

        })
        binding.playLike.setOnClickListener {
            "「喜爱歌曲」待开发，敬请期待".showToast(this)
        }
    }

    private fun updatePlayMode() {
        when (viewModel.currentPlayMode) {
            PlayMode.LIST -> {
                binding.playMode.setImageResource(R.drawable.ic_repeat_list)
            }
            PlayMode.SINGLE -> {
                binding.playMode.setImageResource(R.drawable.ic_repeat_one)
            }
            PlayMode.SHUFFLE -> {
                binding.playMode.setImageResource(R.drawable.ic_shuffle)
            }
        }
    }

    private fun updateProgress() {
        binding.playProgress.text =
            if (viewModel.duration > 600000) {
                if (viewModel.progress % 60000 < 10000)
                    "${viewModel.progress / 60000}:0${viewModel.progress % 60000 / 1000}"
                else
                    "${viewModel.progress / 60000}:${viewModel.progress % 60000 / 1000}"
            } else {
                if (viewModel.progress % 60000 < 10000)
                    "0${viewModel.progress / 60000}:0${viewModel.progress % 60000 / 1000}"
                else
                    "0${viewModel.progress / 60000}:${viewModel.progress % 60000 / 1000}"
            }
//        binding.playSeekbar.progress = viewModel.progress / (viewModel.duration / 100)
    }

    private fun updateDuration() {
        binding.playDuration.text =
            if (viewModel.duration > 600000) {
                if (viewModel.duration % 60000 < 10000)
                    "${viewModel.duration / 60000}:0${viewModel.duration % 60000 / 1000}"
                else
                    "${viewModel.duration / 60000}:${viewModel.duration % 60000 / 1000}"
            } else {
                if (viewModel.duration % 60000 < 10000)
                    "0${viewModel.duration / 60000}:0${viewModel.duration % 60000 / 1000}"
                else
                    "0${viewModel.duration / 60000}:${viewModel.duration % 60000 / 1000}"
            }
    }

    private fun onMusicUpdated(cm: Repository.CurrentMusic) {
        LogUtil.e(TAG, "onMusicUpdated")
        mHandler.removeCallbacks(mProgressCallback)
        updateProgress()
        if (cm.music == null) {
            return
        }
        if (cm.isPlaying) {
            Glide.with(this).load(cm.music?.album).into(binding.playCover)
            binding.apply {
                playOrPause.setImageResource(R.drawable.ic_play)
                playTitle.text = cm.music?.title ?: "未知"
                playArtists.text = cm.music?.artist ?: "未知"
                mHandler.post(mProgressCallback)
            }
            supportActionBar?.title = cm.music?.title ?: "未知"
        }
        else {
            binding.playOrPause.setImageResource(R.drawable.ic_pause)
        }
    }

    companion object {
        // 切换播放进度延迟
        const val UPDATE_PROGRESS_INTERVAL: Long = 1000
        private const val TAG = "MusicPlayActivity"
    }
}