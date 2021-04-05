package com.m2mmusic.android.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.m2mmusic.android.logic.model.SearchType
import com.m2mmusic.android.ui.fragment.LoadingFragment
import com.m2mmusic.android.ui.fragment.SearchFragment

class SearchViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    companion object {
        const val PAGE_MUSIC = 0
        const val PAGE_ALBUM = 1
        const val PAGE_ARTIST = 2
        const val PAGE_MUSICLIST = 3
    }

    override fun getItemCount(): Int {
        return 4
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            PAGE_MUSIC -> SearchFragment.newInstance(SearchType.MUSIC.type)
            PAGE_ALBUM -> SearchFragment.newInstance(SearchType.ALBUMS.type)
            PAGE_ARTIST -> SearchFragment.newInstance(SearchType.ARTIST.type)
            PAGE_MUSICLIST -> SearchFragment.newInstance(SearchType.MUSICLIST.type)
            else -> LoadingFragment.newInstance()
        }
    }

}