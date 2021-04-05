package com.m2mmusic.android.ui.activity

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.m2mmusic.android.R
import com.m2mmusic.android.databinding.ActivityDailyRecommendBinding
import com.m2mmusic.android.logic.Repository
import com.m2mmusic.android.logic.model.DailyRecommendResponse
import com.m2mmusic.android.logic.model.PlayMode
import com.m2mmusic.android.logic.model.PlaylistEvent
import com.m2mmusic.android.logic.model.RecommendPlaylistsResponse
import com.m2mmusic.android.ui.adapter.MusicListAdapter
import com.m2mmusic.android.utils.*
import java.lang.Exception

class DailyRecommendActivity : BaseActivity(), MusicListAdapter.OnItemClickListener {

    private lateinit var binding: ActivityDailyRecommendBinding
    private val viewModel: DailyRecommendActivityViewModel by lazy {
        ViewModelProvider(this)[DailyRecommendActivityViewModel::class.java]
    }
    private lateinit var musicList: DailyRecommendResponse.Data

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDailyRecommendBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.musicListToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        musicList = intent.getParcelableExtra("daily_recommend") as DailyRecommendResponse.Data

        binding.collapsingToolbar.apply {
            title = "每日推荐"
            setCollapsedTitleTextAppearance(R.style.toolbar_collapsing_title_textStyle)
            setExpandedTitleTextAppearance(R.style.toolbar_expend_title_textStyle)
        }

        val layoutManager = LinearLayoutManager(this)
        binding.musicList.layoutManager = layoutManager
        val adapter = MusicListAdapter(viewModel.musicList)
        binding.musicList.adapter = adapter
        adapter.setOnItemClickListener(this)

        darkThemeSetting()

        if (viewModel.music?.music != null) {
            onMusicUpdated(viewModel.music)
        }

        val trackIds = ArrayList<Long>()
        musicList.dailySongs.forEach {
            trackIds.add(it.id)
        }
        viewModel.getOnlineMusic(trackIds)
        // 观察歌曲信息Response
        viewModel.onlineMusic.observe(this) {
            val result = it.getOrNull()
            if (result != null) {
                result.forEach {
                    viewModel.musicList.add(it)
                }
                Glide.with(this).load(result[0].album).into(binding.coverBackground)
                binding.songCount.text = "共 ${result.size.toString()} 首"
                adapter.notifyDataSetChanged()
            }
        }
        // 观察currentMusic的变化，及时更新UI
        viewModel.currentMusic.observe(this) {
            onMusicUpdated(it)
            viewModel.music = it
        }

        bindPlayService()

        onClickListener()
    }

    /**
     * 绑定服务PlayService
     */
    private fun bindPlayService() {
        viewModel.bindPlayService(this)
        LogUtil.e(TAG, "绑定服务")
    }

    override fun onMusicListItemClick(position: Int) {
        viewModel.setPlaylistEvent(PlaylistEvent(viewModel.musicList, position, PlayMode.LIST))
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

    private fun onMusicUpdated(cm: Repository.CurrentMusic?) {
        LogUtil.e(TAG, "onMusicUpdated")
        if (cm?.music == null) {
            return
        }
        if (cm.isPlaying) {
            binding.bottomContainer.bottombarPlayOrPause.setImageResource(R.drawable.ic_play)
        } else {
            binding.bottomContainer.bottombarPlayOrPause.setImageResource(R.drawable.ic_pause)
        }
        try {
            // 加载专辑图片，无专辑封面则使用默认专辑封面
            if (cm.music!!.album != null) {
                // 加载专辑封面
                if (cm.music!!.songId < 10000) {
                    binding.bottomContainer.coverContainer.setImageBitmap(
                        string2Bitmap(
                            loadingCover(
                                cm.music!!.album!!
                            )
                        )
                    )
                } else {
                    Glide.with(this).load(cm.music!!.album + "?param=200y200")
                        .into(binding.bottomContainer.coverContainer)
                }
            } else {
                binding.bottomContainer.coverContainer.setImageResource(R.drawable.default_cover)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }
    }

    private fun onClickListener() {
        // 音乐播放页
        binding.bottomContainer.coverContainer.setOnClickListener {
            // 跳转MusicPlayAcitivity
            viewModel.getPlayListEvent()
            if (viewModel.currentPlayListEvent.playlist.isNullOrEmpty()) {
                "当前无音乐在播放".showToast(this)
            } else {
                viewModel.apply {
                    getProgress()
                    getDuration()
                    startActivity<MusicPlayActivity>(this@DailyRecommendActivity) {
                        putExtra("play_list_event", viewModel.currentPlayListEvent)
                        putExtra("now_playing", viewModel.currentMusic.value?.music)
                        putExtra("is_playing", viewModel.currentMusic.value?.isPlaying)
                        putExtra("progress", progress)
                        putExtra("duration", duration)
                    }
                }
            }
        }
        // 上一首
        binding.bottomContainer.bottombarPlayPrev.setOnClickListener {
            if (viewModel.music != null) {
                viewModel.playPre()
            }
        }
        // 播放、暂停
        binding.bottomContainer.bottombarPlayOrPause.setOnClickListener {
            viewModel.playOrPause()
        }
        // 下一首
        binding.bottomContainer.bottombarPlayNext.setOnClickListener {
            if (viewModel.music != null) {
                viewModel.playNext()
            }
        }
        // 播放列表
        binding.bottomContainer.bottombarPlayList.setOnClickListener {
            viewModel.getPlayListEvent()
            startActivity<PlayListActivity>(this) {
                putExtra("play_list_event", viewModel.currentPlayListEvent)
                putExtra("is_play_activity", false)
            }
        }
    }

    /**
     * 暗黑模式下bottombar显示为黑色
     */
    private fun darkThemeSetting() {
        @SuppressLint("UseCompatLoadingForDrawables")
        if (isDarkTheme(this)) {
            binding.bottomContainer.root.background = getDrawable(R.color.bottom_bar_dark)
        }
    }

    companion object {
        private const val TAG = "DailyRecommendActivity"
    }
}