package com.m2mmusic.android.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.m2mmusic.android.R
import com.m2mmusic.android.databinding.FragmentSearchBinding
import com.m2mmusic.android.logic.model.*
import com.m2mmusic.android.ui.activity.*
import com.m2mmusic.android.ui.adapter.SearchDataAdapter
import com.m2mmusic.android.ui.custom.CustomRoundAngleImageView
import com.m2mmusic.android.utils.TimeUtil
import com.m2mmusic.android.utils.showToast
import com.m2mmusic.android.utils.startActivity

/**
 * Created by 小小苏 on 2021/4/4
 * SearchFragment
 */
class SearchFragment : Fragment() {

    private var type: Int = 0
    var keyWords: String = ""
        set(value) {
            field = value
            loadSearchData()
        }
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val viewModel by lazy {
        ViewModelProvider(this)[SearchFragmentViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            type = it.getInt("search_type")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val layoutManager = LinearLayoutManager(context!!)
        binding.recyclerView.layoutManager = layoutManager
        loadSearchData()
        observeSearchResponse()
        viewModel.bindPlayService(context!!)
    }

    private fun loadSearchData() {
        when (type) {
            SearchType.MUSIC.type -> {
                viewModel.searchMusic(keyWords)
            }
            SearchType.ALBUMS.type -> {
                viewModel.searchAlbum(keyWords)
            }
            SearchType.ARTIST.type -> {
                viewModel.searchArtist(keyWords)
            }
            SearchType.MUSICLIST.type -> {
                viewModel.searchMusicList(keyWords)
            }
        }
    }

    private fun observeSearchResponse() {
        viewModel.apply {
            // 观察搜索单曲
            searchMusicResponse.observe(viewLifecycleOwner) {
                val result = it.getOrNull()
                if (result != null) {
                    val adapter = SearchDataAdapter.Builder<SongResponse.Song>()
                        .setData(result.songs as ArrayList<SongResponse.Song>)
                        .setLayoutId(R.layout.item_search_music)
                        .addBindView { itemView, itemData ->
                            run {
                                Glide.with(context!!).load(itemData.al.picUrl + "?param=300y300")
                                    .into(itemView.findViewById<CustomRoundAngleImageView>(R.id.search_music_cover))
                                itemView.findViewById<TextView>(R.id.search_music_title).text =
                                    itemData.name
                                itemView.findViewById<TextView>(R.id.search_music_artist).text =
                                    with(itemData.ar) {
                                        val artists = StringBuilder()
                                        for (i in 0 until size - 1) {
                                            artists.append("${this[i].name}/")
                                        }
                                        artists.append(this[size - 1].name)
                                    }
                                itemView.findViewById<TextView>(R.id.search_music_album).text =
                                    itemData.al.name
                                itemView.setOnClickListener {
                                    viewModel.getMusicToPlay(itemData.id)
                                }
                            }
                        }.create()
                    binding.recyclerView.adapter = adapter
                }
            }
            // 观察搜索专辑
            searchAlbumResponse.observe(viewLifecycleOwner) {
                val result = it.getOrNull()
                if (result != null) {
                    val adapter =
                        SearchDataAdapter.Builder<SearchAlbumResponse.AlbumsResult.AlbumResult>()
                            .setData(result.albums as ArrayList<SearchAlbumResponse.AlbumsResult.AlbumResult>)
                            .setLayoutId(R.layout.item_search_album)
                            .addBindView { itemView, itemData ->
                                run {
                                    Glide.with(context!!).load(itemData.picUrl + "?param=300y300")
                                        .into(itemView.findViewById<CustomRoundAngleImageView>(R.id.search_album_cover))
                                    itemView.apply {
                                        findViewById<TextView>(R.id.search_album_title).text =
                                            itemData.name
                                        findViewById<TextView>(R.id.search_album_artist).text =
                                            itemData.artist.name
                                        findViewById<TextView>(R.id.search_album_publish_time).text =
                                            TimeUtil.getDateTime(itemData.publishTime)
                                        setOnClickListener {
                                            startActivity<AlbumActivity>(activity as SearchActivity) {
                                                putExtra("fragment", TAG)
                                                putExtra(
                                                    "search_album",
                                                    itemData
                                                )
                                            }
                                        }
                                    }
                                }
                            }.create()
                    binding.recyclerView.adapter = adapter
                }
            }
            // 观察搜索歌手
            searchArtistResponse.observe(viewLifecycleOwner) {
                val result = it.getOrNull()
                if (result != null) {
                    val adapter =
                        SearchDataAdapter.Builder<SearchArtistResponse.ArtistsResult.ArtistResult>()
                            .setData(result.artists as ArrayList<SearchArtistResponse.ArtistsResult.ArtistResult>)
                            .setLayoutId(R.layout.item_search_artist)
                            .addBindView { itemView, itemData ->
                                run {
                                    Glide.with(context!!).load(itemData.picUrl + "?param=300y300")
                                        .into(itemView.findViewById<CustomRoundAngleImageView>(R.id.search_artist_cover))
                                    itemView.findViewById<TextView>(R.id.search_artist_title).text =
                                        itemData.name
                                    itemView.setOnClickListener {
                                        "「歌手」模块待开发，敬请期待".showToast(context!!)
                                    }
                                }
                            }.create()
                    binding.recyclerView.adapter = adapter
                }
            }
            // 观察搜索歌单
            searchMusicListResponse.observe(viewLifecycleOwner) {
                val result = it.getOrNull()
                if (result != null) {
                    val adapter =
                        SearchDataAdapter.Builder<SearchMusicListResponse.PlayListsResult.PlayListResult>()
                            .setData(result.playlists as ArrayList<SearchMusicListResponse.PlayListsResult.PlayListResult>)
                            .setLayoutId(R.layout.item_search_music_list)
                            .addBindView { itemView, itemData ->
                                run {
                                    Glide.with(context!!)
                                        .load(itemData.picUrl + "?param=300y300")
                                        .into(itemView.findViewById<CustomRoundAngleImageView>(R.id.search_music_list_cover))
                                    itemView.apply {
                                        findViewById<TextView>(R.id.search_music_list_title).text =
                                            itemData.name
                                        findViewById<TextView>(R.id.search_music_list_count).text =
                                            itemData.trackCount.toString()
                                        findViewById<TextView>(R.id.search_music_list_creator).text =
                                            itemData.creator.nickname
                                        findViewById<TextView>(R.id.search_music_list_play_count).text =
                                            itemData.playCount.toString()
                                        setOnClickListener {
                                            startActivity<MusicListActivity>(activity as SearchActivity) {
                                                putExtra("which_fragment", TAG)
                                                putExtra("music_list", with(itemData) {
                                                    RecommendPlaylistsResponse.Result(
                                                        id,
                                                        name,
                                                        "",
                                                        picUrl,
                                                        playCount,
                                                        trackCount
                                                    )
                                                })
                                            }
                                        }
                                    }
                                }
                            }.create()
                    binding.recyclerView.adapter = adapter
                }
            }
            // 观察获取OnlineMusic结果
            musicToPlayResponse.observe(viewLifecycleOwner) {
                val result = it.getOrNull()
                if (result != null) {
                    viewModel.apply {
                        setPlaylistEvent(PlaylistEvent(result, 0, PlayMode.LIST))
                        getProgress()
                        getDuration()
                        startActivity<MusicPlayActivity>(context!!) {
                            putExtra("play_list_event", PlaylistEvent(result, 0, PlayMode.LIST))
                            putExtra("now_playing", viewModel.currentMusic.value?.music)
                            putExtra("is_playing", viewModel.currentMusic.value?.isPlaying)
                            putExtra("progress", progress)
                            putExtra("duration", duration)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance(type: Int) =
            SearchFragment().apply {
                arguments = Bundle().apply {
                    putInt("search_type", type)
                }
            }

        private const val TAG = "SearchFragment"
    }
}