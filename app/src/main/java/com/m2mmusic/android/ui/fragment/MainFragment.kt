package com.m2mmusic.android.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.m2mmusic.android.databinding.FragmentMainBinding
import com.m2mmusic.android.logic.Repository
import com.m2mmusic.android.logic.model.RecommendPlaylistsResponse.Result
import com.m2mmusic.android.logic.model.NewResourcesResponse.Creative
import com.m2mmusic.android.logic.model.Music
import com.m2mmusic.android.logic.model.PlayMode
import com.m2mmusic.android.logic.model.PlaylistEvent
import com.m2mmusic.android.ui.activity.AlbumActivity
import com.m2mmusic.android.ui.activity.MainActivity
import com.m2mmusic.android.ui.activity.MusicListActivity
import com.m2mmusic.android.ui.adapter.MainNewAdapter
import com.m2mmusic.android.ui.adapter.MainRecommendAdapter
import com.m2mmusic.android.utils.*

/**
 * Created by 小小苏 at 2021/3/22
 * MainFragment
 * 展示「推荐歌单」
 * 展示「新歌」
 * 展示「新碟」
 * 展示「数字专辑」
 */
class MainFragment : Fragment(), MainRecommendAdapter.OnItemClickListener,
    MainNewAdapter.OnItemClickListener {


    private val viewModel by lazy {
        ViewModelProvider(this)[MainFragmentViewModel::class.java]
    }                // ViewModel
    private var _binding: FragmentMainBinding? =
        null                                                                   // ViewBinding
    private val binding get() = _binding!!
    private lateinit var newResourcesAdapter: MainNewAdapter          // 新歌、新碟、数字专辑Adapter
    private var newResources: ArrayList<Creative> =
        ArrayList()                                    // 作为返回结果的新歌、新碟、数字专辑列表
    private lateinit var recommendAdapter: MainRecommendAdapter      // 推荐歌单Adapter
    private var currentTab = 0                                    // 当前Fragment的position
    private var currentNewResTab = 0                                   // 当前NewRes的position
    private var currentTime: Long = TimeUtil.getTimestamp()                 // 当前时间戳
    private val newSonglist = ArrayList<Music>()                      // 作为播放歌单传递的新歌列表

    /**
     * OnCreate()
     * 1. super
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogUtil.e(TAG, "onCreate")
    }

    /**
     * onCreateView()
     * 1. 进行ViewBinding
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        LogUtil.e(TAG, "onCreateView")
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * onActivityCreated（）
     * 1. 加载「推荐歌单」
     * 2. 加载「新歌、新碟、数字专辑」
     * 3. 监听网络、数据变化、点击事件
     */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        LogUtil.e(TAG, "onActivityCreated")
        initRecommendList()
        initNewResources()
        registerNetworkState()
        registerLiveData()
        registerOnCLickListener()
    }

    /**
     * 加载「推荐歌单」
     */
    private fun initRecommendList() {
        val layoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.HORIZONTAL)
        binding.mainRecommendList.layoutManager = layoutManager
        viewModel.getRecommendPlaylists(Repository.RECOMMENDLIMIT)
        recommendAdapter = MainRecommendAdapter(viewModel.recommendPlayLists)
        binding.mainRecommendList.adapter = recommendAdapter
        recommendAdapter.notifyDataSetChanged()
    }

    /**
     * 加载「新歌、新碟、数字专辑」
     */
    private fun initNewResources() {
        val layoutManager = StaggeredGridLayoutManager(3, LinearLayoutManager.HORIZONTAL)
        binding.mainNewList.layoutManager = layoutManager
        viewModel.getNewResources()
        initNewSongsList()
        binding.tvNewSong.isSelected = true
        newResourcesAdapter = MainNewAdapter(newResources)
        binding.mainNewList.adapter = newResourcesAdapter
    }

    /**
     * 加载「新歌」
     */
    private fun initNewSongsList() {
        if (viewModel.newResourcesLists.isNotEmpty()) {
            newResources.clear()
            newResources.add(viewModel.newResourcesLists[0])
            newResources.add(viewModel.newResourcesLists[1])
            val newResResultlist = ArrayList<Long>()
            viewModel.newResourcesLists[0].resources.forEach {
                newResResultlist.add(it.resourceId.toLong())
            }
            viewModel.newResourcesLists[1].resources.forEach {
                newResResultlist.add(it.resourceId.toLong())
            }
            LogUtil.e(TAG, "加载歌曲信息")
            launch({
                viewModel.getOnlineMusic(newResResultlist)
            }, {
                it.message?.showToast((activity as MainActivity).applicationContext)
            })
        }
    }

    /**
     * 加载「新碟」
     */
    private fun initNewAlbumsList() {
        if (viewModel.newResourcesLists.isNotEmpty()) {
            newResources.clear()
            newResources.add(viewModel.newResourcesLists[2])
            newResources.add(viewModel.newResourcesLists[3])
        }
    }

    /**
     * 加载「数字专辑」
     */
    private fun initDigitAlbumsList() {
        if (viewModel.newResourcesLists.isNotEmpty()) {
            newResources.clear()
            newResources.add(viewModel.newResourcesLists[4])
            newResources.add(viewModel.newResourcesLists[5])
        }
    }

    /**
     *
     */
    private fun refreshNewRes() {
        if (binding.tvNewSong.isSelected) {
            initNewSongsList()
        } else if (binding.tvNewAlbum.isSelected) {
            initNewAlbumsList()
        } else {
            initDigitAlbumsList()
        }
    }

    /**
     * 在当前Fragment中注册点击事件
     */
    private fun registerOnCLickListener() {
        onCLickListener()
        recommendAdapter.setOnItemClickListener(this)
        newResourcesAdapter.setOnItemClickListener(this)
    }

    /**
     * 「推荐歌单」列表点击事件
     */
    override fun onRecommendItemClick(position: Int) {
        viewModel.recommendPlayLists[position].apply {
            startActivity<MusicListActivity>(activity as MainActivity) {
                putExtra("which_fragment", TAG)
                putExtra("music_list", viewModel.recommendPlayLists[position])
            }
        }
    }

    /**
     * 「新歌、新碟、数字专辑」列表点击事件
     */
    override fun onNewResItemClick(position: Int) {
        if (System.currentTimeMillis() - currentTime < 2000) {
            return
        }
        when (currentNewResTab) {
            0 -> {
                currentTime = TimeUtil.getTimestamp()
                (activity as MainActivity).viewModel.apply {
                    setPlaylistEvent(
                        PlaylistEvent(
                            newSonglist,
                            position,
                            PlayMode.getDefault()
                        )
                    )
                }
            }
            1 -> {
                currentTime = TimeUtil.getTimestamp()
                startActivity<AlbumActivity>(activity as MainActivity) {
                    putExtra("fragment", TAG)
                    putExtra(
                        "new_album",
                        viewModel.newResourcesLists[(position / 3) + 2].resources[position % 3]
                    )
                }
            }
            2 -> {
                currentTime = TimeUtil.getTimestamp()
                "功能待开发，敬请期待".showToast(context!!)
            }
        }

    }

    /**
     * 设置Fragment点击事件
     */
    private fun onCLickListener() {
        // 点击「新歌」标题
        binding.tvNewSong.setOnClickListener {
            initNewSongsList()
            changeNewResTab(0)
            currentNewResTab = 0
            newResourcesAdapter.notifyDataSetChanged()
        }
        // 点击「新碟」标题
        binding.tvNewAlbum.setOnClickListener {
            initNewAlbumsList()
            changeNewResTab(1)
            currentNewResTab = 1
            newResourcesAdapter.notifyDataSetChanged()
        }
        // 点击「数字专辑」标题
        binding.tvDigitAlbum.setOnClickListener {
            initDigitAlbumsList()
            changeNewResTab(2)
            currentNewResTab = 2
            newResourcesAdapter.notifyDataSetChanged()
        }
    }

    /**
     * 切换新歌、新碟、数字专辑
     */
    private fun changeNewResTab(position: Int) {
        binding.tvNewSong.isSelected = false
        binding.tvNewAlbum.isSelected = false
        binding.tvDigitAlbum.isSelected = false
        currentTab = 0

        when (position) {
            0 -> {
                binding.tvNewSong.isSelected = true
            }
            1 -> {
                binding.tvNewAlbum.isSelected = true
            }
            2 -> {
                binding.tvDigitAlbum.isSelected = true
            }
        }
    }

    /**
     *
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

                /*NetworkState.CELLULAR -> {
                    "移动网络已连接".showToast(context!!)
                }

                NetworkState.WIFI -> {
                    "WIFI已连接".showToast(context!!)
                }*/
            }
        })
    }

    /**
     *
     */
    private fun registerLiveData() {
        // 观察推荐歌单请求的返回值，及时更新UI
        viewModel.recommendLiveData.observe(viewLifecycleOwner, Observer { result ->
            val list = result.getOrNull()
            if (list != null) {
                binding.mainRecommendList.visibility = View.VISIBLE
                viewModel.recommendPlayLists.apply {
                    clear()
                    addAll(list)
                }
                recommendAdapter.notifyDataSetChanged()
            } else {
                "服务器连接异常 or 数据丢失".showToast(context!!)
                result.exceptionOrNull()?.printStackTrace()
            }
        })
        // 观察获取新歌、新碟、数字专辑请求的返回值，及时更新UI
        viewModel.newResLiveData.observe(viewLifecycleOwner, { result ->
            val list = result.getOrNull()
            if (list != null) {
                binding.mainNewList.visibility = View.VISIBLE
                viewModel.newResourcesLists.apply {
                    clear()
                    addAll(list)
                }
                refreshNewRes()
                newResourcesAdapter.notifyDataSetChanged()
            } else {
                "服务器连接异常 or 数据丢失".showToast(context!!)
                result.exceptionOrNull()?.printStackTrace()
            }
        })
        // 观察获取新歌资源请求的返回值，及时更新UI
        viewModel.onlineMusic.observe(viewLifecycleOwner) { result ->
            result.getOrNull()?.forEach {
                newSonglist.add(it)
            }
            LogUtil.e(TAG, "NewSonglist$newSonglist")
        }
    }

    /**
     * 提供刷新数据的方法
     * 外部访问
     */
    fun refreshFragment() {
        LogUtil.e(TAG, "refreshing")
        viewModel.apply {
            getRecommendPlaylists(Repository.RECOMMENDLIMIT)
            getNewResources()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()

        private const val TAG = "MainFragment"
    }
}