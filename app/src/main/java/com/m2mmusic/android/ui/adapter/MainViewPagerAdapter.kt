package com.m2mmusic.android.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.m2mmusic.android.ui.fragment.LoadingFragment
import com.m2mmusic.android.ui.fragment.MainFragment
import com.m2mmusic.android.ui.fragment.MyFragment

class MainViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    companion object {
        const val PAGE_MAIN = 0
        const val PAGE_MY = 1
    }

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            PAGE_MAIN -> MainFragment.newInstance()
            PAGE_MY -> MyFragment.newInstance()
            else -> LoadingFragment.newInstance()
        }
    }
}