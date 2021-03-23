package com.m2mmusic.android.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.view.GravityCompat
import androidx.viewpager2.widget.ViewPager2
import com.m2mmusic.android.R
import com.m2mmusic.android.databinding.ActivityMainBinding
import com.m2mmusic.android.databinding.ViewToolbarBinding
import com.m2mmusic.android.ui.adapter.MainViewPagerAdapter

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var toolbarBinding: ViewToolbarBinding
    private var currentTab = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        toolbarBinding = ViewToolbarBinding.bind(binding.root)
        setContentView(binding.root)
        setSupportActionBar(toolbarBinding.toolbar)

        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.ic_menu)
        }

        binding.navView.setNavigationItemSelectedListener {
            binding.drawerLayout.closeDrawers()
            true
        }

        // 加载Fragment
        initFragment()

        // 绑定点击事件
        onClickListener()

    }

    /**
     * 加载Fragment
     */
    private fun initFragment() {
        binding.mainViewPager.adapter = MainViewPagerAdapter(this)
        binding.mainViewPager.offscreenPageLimit = 1
//        binding.mainViewPager.isUserInputEnabled = false
        binding.mainViewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                changeTab(position)
            }
        })
    }

    /**
     * 加载Menu布局文件
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar, menu)
        return true
    }

    /**
     * 处理Menu点击事件
     * 打开搜索页、滑动菜单
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> binding.drawerLayout.openDrawer(GravityCompat.START)
        }
        // 跳转SearchActivity

        return true
    }

    /**
     * 设置点击事件
     */
    private fun onClickListener() {
        binding.bottomContainer.tabMainBtn.setOnClickListener {
            changeTab(0)
        }
        binding.bottomContainer.tabMyBtn.setOnClickListener {
            changeTab(1)
        }
    }

    /**
     * 切换Fragment
     */
    private fun changeTab(position: Int) {
        binding.bottomContainer.tabMainBtn.isSelected = false
        binding.bottomContainer.tabMainTv.isSelected = false
        binding.bottomContainer.tabMyBtn.isSelected = false
        binding.bottomContainer.tabMyTv.isSelected = false
        currentTab = position

        when (position) {
            0 -> {
                binding.bottomContainer.tabMainBtn.isSelected = true
                binding.bottomContainer.tabMainTv.isSelected = true
            }
            1 -> {
                binding.bottomContainer.tabMyBtn.isSelected = true
                binding.bottomContainer.tabMyTv.isSelected = true
            }
        }
        binding.mainViewPager.setCurrentItem(position, true)
    }

    companion object {
        /**
         * 向外部提供跳转MainActivity的方法
         */
        fun startThisActivity(activity: AppCompatActivity) {
            val intent = Intent(activity, MainActivity::class.java)
            activity.startActivity(intent)
            activity.finish()
        }

    }
}
