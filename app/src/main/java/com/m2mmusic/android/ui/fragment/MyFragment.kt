package com.m2mmusic.android.ui.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.m2mmusic.android.R
import com.m2mmusic.android.databinding.FragmentMyBinding
import com.m2mmusic.android.logic.model.PlaylistDetailResponse
import com.m2mmusic.android.logic.model.RecommendPlaylistsResponse
import com.m2mmusic.android.ui.activity.*
import com.m2mmusic.android.ui.adapter.UserMusicListAdapter
import com.m2mmusic.android.utils.*
import kotlin.collections.ArrayList

/**
 * Created by 小小苏 at 2021/4/2
 * MyFragment
 * 展示用户基本信息（头像、昵称、等级、是否VIP）
 * 展示「用户喜欢的音乐」
 * 展示「播放统计」
 * 展示「创建的歌单」
 * 展示「收藏的歌单」
 */
class MyFragment : Fragment(), UserMusicListAdapter.OnItemClickListener {

    // ViewBinding
    private var _binding: FragmentMyBinding? = null
    private val binding get() = _binding!!
    private val viewModel by lazy {
        ViewModelProvider(this)[MyFragmentViewModel::class.java]
    }
    private lateinit var adapter: UserMusicListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogUtil.e(TAG, "onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMyBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * onActivityCreated（）
     * 1. 加载「用户信息」
     * 2. 加载「喜欢的音乐」
     * 3. 加载「创建的歌单、收藏的歌单」
     * 4. 监听网络、数据变化、点击事件
     */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        LogUtil.e(TAG, "onActivityCreated")

        val layoutManager = LinearLayoutManager(activity)
        binding.myMusicListRecyclerView.layoutManager = layoutManager
        adapter = UserMusicListAdapter(viewModel.userMusicList)
        binding.myMusicListRecyclerView.adapter = adapter
        adapter.setOnItemClickListener(this)
        binding.mySubscribedMusicList.isSelected = true

        if (viewModel.isLogin.value == true) {
            initUserInfo()
            viewModel.getUserLikeList()
            viewModel.getUserMusicList()
        } else {
            initUserInfo()
            initLikeList()
            initMusicLists()
        }

        registerNetworkState()
        registerLiveData()
        registerOnCLickListener()
    }

    /**
     * 加载「用户信息」
     */
    @SuppressLint("ResourceAsColor")
    private fun initUserInfo() {
        if (viewModel.isLogin.value == true) {
            viewModel.getUserLevelResponse()
            viewModel.getUserProfile()
            binding.apply {
                nickname.text = viewModel.profile!!.nickname
                Glide.with(this@MyFragment).load(viewModel.profile!!.avatarUrl)
                    .into(avatar)
                Glide.with(this@MyFragment).load(viewModel.profile!!.backgroundUrl).apply(
                    RequestOptions.bitmapTransform(
                        BlurTransformation(
                            (activity as MainActivity),
                            25,
                            2
                        )
                    )
                ).into(myUserBackground)

                val simpleTarget: SimpleTarget<Bitmap> = object : SimpleTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        // 使用Palette类提取图片的颜色信息
                        Palette.from(resource).generate { palette ->
                            val swatch = palette?.dominantSwatch
                            if (swatch != null) {
                                level.setTextColor(swatch.bodyTextColor) //设置文本颜色
                                nickname.setTextColor(swatch.bodyTextColor) //设置文本颜色
                                lv.setTextColor(swatch.bodyTextColor) //设置文本颜色
                                point.setTextColor(swatch.bodyTextColor) //设置文本颜色
                            }
                        }
                    }
                }
                Glide.with(this@MyFragment)
                    .asBitmap()
                    .load(viewModel.profile!!.backgroundUrl)
                    .into(simpleTarget)

                if (viewModel.profile!!.vipType > 0)
                    vip.visibility = View.VISIBLE
                else
                    vip.visibility = View.GONE
            }
        } else {
            viewModel.clearUserProfile()
            viewModel.clearUserLevel()
            binding.apply {
                nickname.text = "点击头像登录"
                nickname.setTextColor(R.color.black)
                level.text = "0"
                level.setTextColor(R.color.black)
                lv.setTextColor(R.color.black)
                point.setTextColor(R.color.black)
                levelProgress.progress = 0
                vip.visibility = View.GONE
                avatar.setImageResource(R.drawable.default_avatar)
                myUserBackground.setImageResource(0)
            }
        }
    }

    /**
     * 加载喜欢的音乐列表
     */
    private fun initLikeList() {
        if (viewModel.isLogin.value == true) {
            binding.apply {
                myLikeMusicFirst.visibility = View.VISIBLE
                myLikeMusicSecond.visibility = View.VISIBLE
            }
            Glide.with(this).apply {
                load(viewModel.userLikeList[0].album).into(binding.myLikeMusicFirst)
                load(viewModel.userLikeList[1].album).into(binding.myLikeMusicSecond)
            }
            binding.myLikeMusic.setOnClickListener {
                startActivity<MyLikeListActivity>(activity as Activity) {
                    putExtra("like_list", viewModel.userLikeList)
                }
            }
        } else {
            binding.apply {
                myLikeMusicFirst.visibility = View.GONE
                myLikeMusicSecond.visibility = View.GONE
            }
            binding.myLikeMusic.setOnClickListener {
                "请先登录".showToast(activity as MainActivity)
                startActivity<LoginActivity>(activity as MainActivity) {}
            }
        }
    }

    /**
     * 加载用户歌单
     */
    private fun initMusicLists() {
        if (viewModel.isLogin.value == true) {
            binding.myMusicListRecyclerView.visibility = View.VISIBLE
            if (binding.mySubscribedMusicList.isSelected) {
                viewModel.userMusicList.apply {
                    clear()
                    addAll(viewModel.userSubscribedMusicList)
                }
                adapter.notifyDataSetChanged()
            } else {
                viewModel.userMusicList.apply {
                    clear()
                    addAll(viewModel.userCreatedMusicList)
                }
                adapter.notifyDataSetChanged()
            }
        } else {
            viewModel.apply {
                userMusicList.clear()
                userSubscribedMusicList.clear()
                userCreatedMusicList.clear()
            }
            binding.myMusicListRecyclerView.visibility = View.GONE
        }
    }

    /**
     * 监听网络状态
     */
    private fun registerNetworkState() {
        // 监听网络状态
        NetworkLiveData.getInstance().observe(viewLifecycleOwner, {
            when (it) {
                NetworkState.CONNECTED -> {
                    "网络已连接".showToast(context!!)
                }

                NetworkState.UNCONNECTED -> {
                    "网络中断".showToast(context!!)
                }
            }
        })
    }

    /**
     * 观察一些数据
     */
    private fun registerLiveData() {
        // 观察登录状态变化
        viewModel.isLogin.observe(viewLifecycleOwner) {
            if (it) {
                initUserInfo()
                viewModel.getUserLikeList()
                viewModel.getUserMusicList()
            } else {
                initUserInfo()
                initLikeList()
            }
        }
        // 观察UserLikeList
        viewModel.userLikeListResponse.observe(viewLifecycleOwner) {
            val result = it.getOrNull()
            if (result != null) {
                val likeMusicList = ArrayList<Long>()
                for (id in result) {
                    likeMusicList.add(id.toLong())
                }
                viewModel.getUserLikeListItem(likeMusicList)
                binding.myLikeMusicCount.text = likeMusicList.size.toString()
            }
        }
        // 观察UserLikeListItem
        viewModel.userLikeListItemResponse.observe(viewLifecycleOwner) {
            val result = it.getOrNull()
            if (result != null) {
                viewModel.saveUserLikeList(result)
                initLikeList()
            }
        }
        // 观察UserLevel
        viewModel.userLevelResponse.observe(viewLifecycleOwner) {
            if (it.getOrDefault(false)) {
                viewModel.getUserLevel()
                binding.apply {
                    level.text = viewModel.userLevel!!.level.toString()
                    levelProgress.progress =
                        (viewModel.userLevel!!.progress * 100).toInt()
                }
            } else {
                viewModel.clearUserLevel()
            }
        }
        // 观察UserMusicList
        viewModel.userMusicListResponse.observe(viewLifecycleOwner) {
            val result = it.getOrNull() as ArrayList<PlaylistDetailResponse.Playlist>
            if (!result.isNullOrEmpty()) {
                result.removeAt(0)
                viewModel.userSubscribedMusicList.clear()
                viewModel.userCreatedMusicList.clear()
                result.forEach { playlist ->
                    if (playlist.creator.nickname == viewModel.profile!!.nickname)
                        viewModel.userSubscribedMusicList.add(playlist)
                    else
                        viewModel.userCreatedMusicList.add(playlist)
                }
                initMusicLists()
            }
        }
    }

    /**
     * 处理用户点击事件
     */
    private fun registerOnCLickListener() {
        binding.mySubscribedMusicList.setOnClickListener {
            binding.apply {
                mySubscribedMusicList.isSelected = true
                myCreatedMusicList.isSelected = false
                initMusicLists()
            }
        }
        binding.myCreatedMusicList.setOnClickListener {
            binding.apply {
                mySubscribedMusicList.isSelected = false
                myCreatedMusicList.isSelected = true
                initMusicLists()
            }
        }
        binding.recentPlay.setOnClickListener {
            "「最近」待开发，敬请期待".showToast(context!!)
        }
        binding.myCloudMusic.setOnClickListener {
            "「云盘」待开发，敬请期待".showToast(context!!)
        }
        binding.myMusicRecord.setOnClickListener {
            "「记录」待开发，敬请期待".showToast(context!!)
        }
        binding.avatar.setOnClickListener {
            if (viewModel.isLogin.value == true) {
                "「个人中心」待开发，敬请期待".showToast(context!!)
            } else {
                // 启动登录页
                LogUtil.e(TAG, "登录页启动")
                com.m2mmusic.android.utils.startActivity<LoginActivity>(context!!) {}
            }
        }
    }

    override fun onUserMusicListItemClick(position: Int) {
        if (binding.mySubscribedMusicList.isSelected) {
            startActivity<MusicListActivity>(activity as Activity) {
                putExtra("which_fragment", TAG)
                putExtra("music_list", with(viewModel.userSubscribedMusicList[position]) {
                    RecommendPlaylistsResponse.Result(
                        id,
                        name,
                        "",
                        coverImgUrl,
                        playCount,
                        trackCount
                    )
                })
            }
        } else {
            startActivity<MusicListActivity>(activity as Activity) {
                putExtra("which_fragment", "MainFragment")
                putExtra("music_list", with(viewModel.userCreatedMusicList[position]) {
                    RecommendPlaylistsResponse.Result(
                        id,
                        name,
                        "",
                        coverImgUrl,
                        playCount,
                        trackCount
                    )
                })
            }
        }
    }

    /**
     * 提供刷新数据的方法
     * 外部访问
     */
    fun refreshFragment() {
        LogUtil.e(TAG, "refreshing")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance() = MyFragment()

        private const val TAG = "MyFragment"
    }
}