package com.m2mmusic.android.ui.activity

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.m2mmusic.android.R
import com.m2mmusic.android.databinding.ActivityMusicListBinding
import com.m2mmusic.android.logic.Repository
import com.m2mmusic.android.logic.model.PlayMode
import com.m2mmusic.android.logic.model.RecommendPlaylistsResponse.Result
import com.m2mmusic.android.logic.model.PlaylistDetailResponse.Playlist
import com.m2mmusic.android.logic.model.PlaylistEvent
import com.m2mmusic.android.ui.adapter.MusicListAdapter
import com.m2mmusic.android.utils.*
import java.lang.Exception

class MusicListActivity : BaseActivity(), MusicListAdapter.OnItemClickListener {

    private lateinit var binding: ActivityMusicListBinding
    private lateinit var musicList: Result
    private lateinit var viewModel: MusicListActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMusicListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.musicListToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        viewModel = ViewModelProvider(this)[MusicListActivityViewModel::class.java]

        when(intent.getStringExtra("which_fragment")) {
            "MainFragment" -> {}
            "MyFragment" -> binding.like.visibility = View.GONE
        }
        musicList = intent.getParcelableExtra("music_list") as Result

        viewModel.musicListId = musicList.id
        binding.collapsingToolbar.apply {
            title = musicList.name
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

        // 观察歌单是否已收藏
        viewModel.isSubscribed(musicList.id)
        viewModel.isSubscribed.observe(this) {
            val result = it.getOrNull()
            if (result != null) {
                if (result.subscribed) {
                    binding.like.setImageResource(R.drawable.ic_favorite)
                    viewModel.hasSubscribed = true
                } else {
                    binding.like.setImageResource(R.drawable.ic_favorite_border)
                    viewModel.hasSubscribed = false
                }
            }
        }
        // 观察歌单详情Response
        viewModel.getMusicListDetail(musicList.id)
        viewModel.musicListDetail.observe(this) {
            val result = it.getOrNull()
            if (result != null) {
                updateHeadUI(result)
                val trackIds = ArrayList<Long>()
                for (track in result.trackIds) {
                    trackIds.add(track.id)
                }
                viewModel.getOnlineMusic(trackIds)
            }
        }
        // 观察歌曲信息Response
        viewModel.onlineMusic.observe(this) { result ->
            result.getOrNull()?.forEach {
                viewModel.musicList.add(it)
            }
            adapter.notifyDataSetChanged()
        }
        // 观察currentMusic的变化，及时更新UI
        viewModel.currentMusic.observe(this) {
            onMusicUpdated(it)
            viewModel.music = it
        }
        // 观察收藏歌单Response
        viewModel.subscribed.observe(this) {
            if (it.getOrDefault(false)) {
                if (viewModel.hasSubscribed) {
                    "取消收藏成功".showToast(this)
                    binding.like.setImageResource(R.drawable.ic_favorite_border)
                    viewModel.hasSubscribed = false
                } else{
                    "收藏成功".showToast(this)
                    binding.like.setImageResource(R.drawable.ic_favorite)
                    viewModel.hasSubscribed = true
                }
            } else {
                "操作失败".showToast(this)
            }
        }

        bindPlayService()

        onClickListener()
    }

    private fun updateHeadUI(playlist: Playlist) {
        binding.collapsingToolbar.title = playlist.name
        Glide.with(this).load("${playlist.coverImgUrl}?param=400y400").apply(
            RequestOptions.bitmapTransform(BlurTransformation(this, 25, 2))
        ).into(binding.coverBackground)
        Glide.with(this).load("${playlist.coverImgUrl}?param=400y400")
            .into(binding.coverForeground)
        binding.playCount.text = musicList.playCount.toString()
        Glide.with(this).load("${playlist.creator.avatarUrl}?param=40y40")
            .into(binding.creatorAvatar)
        binding.creatorName.text = playlist.creator.nickname
        val simpleTarget: SimpleTarget<Bitmap> = object : SimpleTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                // 使用Palette类提取图片的颜色信息
                Palette.from(resource).generate { palette ->
                    val swatch = palette?.dominantSwatch
                    if (swatch != null) {
                        binding.collapsingToolbar.setExpandedTitleColor(swatch.bodyTextColor) //设置文本颜色
                        binding.creatorName.setTextColor(swatch.bodyTextColor) //设置文本颜色
                        binding.playCount.setTextColor(swatch.bodyTextColor) //设置文本颜色
                    }
                }
            }
        }

        Glide.with(this)
            .asBitmap()
            .load(playlist.coverImgUrl)
            .into(simpleTarget)
        val count = with(playlist.playCount) {
            if (this > 99999999) {
                "${this / 100000000}.${(this % 100000000) / 10000000}亿"
            } else if (this > 9999) {
                "${this / 10000}.${(this % 10000) / 1000}万"
            } else if (this > 999) {
                "${this / 1000}.${(this % 1000) / 100}千"
            } else {
                this.toString()
            }
        }
        binding.playCount.text = count
        binding.songCount.text = "共 ${playlist.trackCount} 首"
    }

    /**
     * 绑定服务PlayService
     */
    private fun bindPlayService() {
        viewModel.bindPlayService(this)
        LogUtil.e(TAG, "绑定服务")
    }

    companion object {
        private const val TAG = "MusicListActivity"
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
        if (cm.isPlaying){
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
                    startActivity<MusicPlayActivity>(this@MusicListActivity) {
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
        // 收藏歌单
        binding.like.setOnClickListener {
            if (viewModel.isLogin() == true) {
                viewModel.subscribeMusicList()
            } else {
                startActivity<LoginActivity>(this) {}
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
}