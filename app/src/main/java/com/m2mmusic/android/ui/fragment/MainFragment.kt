package com.m2mmusic.android.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.m2mmusic.android.databinding.FragmentMainBinding
import com.m2mmusic.android.logic.Repository
import com.m2mmusic.android.logic.model.Playlist
import com.m2mmusic.android.ui.adapter.MainRecommendAdapter
import com.m2mmusic.android.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by 小小苏 at 2021/3/22
 * MainFragment
 * 展示「推荐歌单」
 * 展示「新歌」
 * 展示「新碟」
 * 展示「数字专辑」
 */
class MainFragment : Fragment() {

    // ViewModel
    private val viewModel by lazy { ViewModelProvider(this)[MainFragmentViewModel::class.java] }

    // ViewBinding
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    // 推荐歌单Adapter
    private lateinit var recommendList: ArrayList<Playlist>
    private lateinit var recommendAdapter: MainRecommendAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initRecommendList()
//        initNewSongsList()
//        initNewAlbumsList()
//        initDigitAlbumsList()
//        registeNetworkState()
        viewModel.recommendLiveData.observe(viewLifecycleOwner, Observer { result ->
            val list = result.getOrNull()
            if (list != null) {
                binding.mainRecommendList.visibility = View.VISIBLE
                viewModel.recommendPlaylists.apply {
                    clear()
                    addAll(list)
                }
                recommendAdapter.notifyDataSetChanged()
            } else {
                "服务器连接异常 or 数据丢失".showToast(context!!)
                result.exceptionOrNull()?.printStackTrace()
            }
        })
    }

    private fun initRecommendList() {
        initRecommendView()
    }

    private fun initRecommendView() {
//        val layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        val layoutManager = StaggeredGridLayoutManager(6, LinearLayoutManager.VERTICAL)
        binding.mainRecommendList.layoutManager = layoutManager
        recommendAdapter = MainRecommendAdapter(viewModel.recommendPlaylists)
        binding.mainRecommendList.adapter = recommendAdapter

        viewModel.getRecommendPlaylists(Repository.RECOMMENDLIMIT)

    }

    private fun initNewSongsList() {
        //
    }

    private fun initNewAlbumsList() {
        //
    }

    private fun initDigitAlbumsList() {
        //
    }

    private fun registeNetworkState() {
        // 监听网络状态
        NetworkLiveData.getInstance().observe(viewLifecycleOwner, Observer {
            when (it) {
                NetworkState.CONNECTED -> {
                    "网络已连接".showToast(context!!)
                }

                NetworkState.UNCONNECTED -> {
                    "网络中断".showToast(context!!)
                }

                NetworkState.CELLULAR -> {
                    "移动网络已连接".showToast(context!!)
                }

                NetworkState.WIFI -> {
                    "WIFI已连接".showToast(context!!)
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
    }
}