package com.m2mmusic.android.ui.activity

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import com.bumptech.glide.Glide
import com.m2mmusic.android.R
import com.m2mmusic.android.databinding.ActivityPlayListBinding
import com.m2mmusic.android.logic.model.PlayMode
import com.m2mmusic.android.logic.model.PlaylistEvent
import com.m2mmusic.android.ui.adapter.PlayListAdapter
import com.m2mmusic.android.utils.LogUtil
import com.m2mmusic.android.utils.isDarkTheme
import com.m2mmusic.android.utils.showToast
import com.m2mmusic.android.utils.startActivity

class PlayListActivity : BaseActivity(), PlayListAdapter.OnItemClickListener {

    private lateinit var binding: ActivityPlayListBinding
    private lateinit var viewModel: PlayListViewModel
    private lateinit var playListAdapter: PlayListAdapter
    private var isPlayActivity = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        overridePendingTransition(R.anim.anim_slide_in_bottom, R.anim.no_anim)
        viewModel = ViewModelProvider(this)[PlayListViewModel::class.java]

        viewModel.initPlayListEvent(intent.getParcelableExtra("play_list_event"))
        isPlayActivity = intent.getBooleanExtra("is_play_activity", false)
        if (isPlayActivity) {
            binding.apply {
                playListCover.visibility = View.GONE
                playListPlaymode.visibility = View.GONE
            }
        }

        bindPlayService(this)
        initPlayList()
        darkThemeSetting()
        setOnClickListener()

        viewModel.currentMusic.observe(this) {
            if (it.isPlaying && viewModel.isServiceBounded()) {
                val old = viewModel.currentPlayPosition
                viewModel.getPlayPosition()
                updateView(viewModel.currentPlayPosition, old)
                if (!isPlayActivity){
                    Glide.with(this)
                        .load(it.music!!.album + "?param=400y400")
                        .into(binding.playListCover)
                }
            }
        }
    }

    /**
     * 绑定服务PlayService
     */
    private fun bindPlayService(context: Context) {
        viewModel.bindPlayService(context)
        LogUtil.e(TAG, "绑定服务")
    }

    private fun initPlayList() {
        val layoutManager = LinearLayoutManager(this)
        playListAdapter = PlayListAdapter(viewModel.currentPlaylist, viewModel.currentPlayPosition)
        binding.playList.apply {
            this.layoutManager = layoutManager
            this.adapter = playListAdapter
        }
        val smoothScroller = LinearSmoothScroller(this)
        smoothScroller.targetPosition = viewModel.currentPlayPosition
        layoutManager.startSmoothScroll(smoothScroller)
        if (viewModel.currentMusic.value?.music != null) {
            Glide.with(this)
                .load(viewModel.currentMusic.value!!.music!!.album + "?param=400y400")
                .into(binding.playListCover)
        }
        updatePlayMode()
    }

    private fun setOnClickListener() {
        playListAdapter.setOnItemClickListener(this)
        // 点击空白处关闭
        binding.root.setOnClickListener {
            finish()
        }
        // 点击切换播放顺序
        binding.playListPlaymode.setOnClickListener {
            viewModel.switchPlayMode()
            updatePlayMode()
        }
        // 点击Cover进入播放页
        binding.playListCover.setOnClickListener {
            if (viewModel.currentPlaylist.isNullOrEmpty()) {
                "当前无音乐在播放".showToast(this)
            } else {
                viewModel.apply {
                    getProgress()
                    getDuration()
                    startActivity<MusicPlayActivity>(this@PlayListActivity) {
                        putExtra("play_list_event", PlaylistEvent(currentPlaylist, currentPlayPosition, currentPlayMode))
                        putExtra("now_playing", viewModel.currentMusic.value?.music)
                        putExtra("is_playing", viewModel.currentMusic.value?.isPlaying)
                        putExtra("progress", progress)
                        putExtra("duration", duration)
                    }
                }
            }
        }
    }

    override fun onPlayingItemClick(position: Int) {
        viewModel.getPlayPosition()
        updateView(position, viewModel.currentPlayPosition)
        viewModel.playIndex(position)
    }

    private fun updateView(new: Int, old: Int) {
        playListAdapter.apply {
            setNowPlaying(new)
            viewModel.getPlayPosition()
            updateData(old)
            updateData(new)
        }
    }

    private fun updatePlayMode() {
        when(viewModel.currentPlayMode) {
            PlayMode.LIST -> {
                binding.playListPlaymode.setImageResource(R.drawable.ic_repeat_list)
            }
            PlayMode.SINGLE -> {
                binding.playListPlaymode.setImageResource(R.drawable.ic_repeat_one)
            }
            PlayMode.SHUFFLE -> {
                binding.playListPlaymode.setImageResource(R.drawable.ic_shuffle)
            }
        }
    }

    /**
     * 暗黑模式下bottombar显示为黑色
     */
    private fun darkThemeSetting() {
        @SuppressLint("UseCompatLoadingForDrawables")
        if (isDarkTheme(this)) {
            binding.playlistBackground.background = getDrawable(R.drawable.corner_background_dark)
        }
    }

    override fun finish() {
        overridePendingTransition(R.anim.no_anim, R.anim.anim_slide_out_bottom)
        super.finish()
    }

    companion object {
        private const val TAG = "PlayListActivity"
    }
}