package com.m2mmusic.android.ui.activity

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.core.widget.ContentLoadingProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.palette.graphics.Palette
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.m2mmusic.android.R
import com.m2mmusic.android.application.SharedPreferencesUtil
import com.m2mmusic.android.application.UserResDao
import com.m2mmusic.android.databinding.ActivityMainBinding
import com.m2mmusic.android.databinding.ViewToolbarBinding
import com.m2mmusic.android.logic.Repository
import com.m2mmusic.android.ui.adapter.MainViewPagerAdapter
import com.m2mmusic.android.ui.fragment.MainFragment
import com.m2mmusic.android.ui.fragment.MyFragment
import com.m2mmusic.android.utils.*
import java.lang.Exception

/**
 * Created by 小小苏
 * M2M Music的程序主页
 * 页面从上之下主要为Toolbar、ViewPager2、Bottombar
 * Toolbar包含DrawerLayout入口和搜索页入口
 * ViewPager2包含两个Fragment，分别为「首页」和「我的」
 * Bottombar可控制ViewPager2显示的内容，同时包含当前播放歌曲封面展示及播放页入口
 */
class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding                 // 注册binding对象
    private lateinit var toolbarBinding: ViewToolbarBinding           // 注册toolbarBinding对象
    lateinit var viewModel: MainActivityViewModel             // 注册viewModel对象
    private var currentMusic: Repository.CurrentMusic? = null         // 注册当前播放的Music对象
    private lateinit var sharedPreferences: SharedPreferences

    /**
     * onCreate()
     * 1. 利用生成的ViewBinding对象绑定视图
     * 2. DrawerLayout设置
     * 3. 暗黑模式下bottombar显示为黑色
     * 4. 设置ViewPager2事件响应、加载Fragment
     * 5. 设置除ViewPager2和DrawerLayout外的事件响应
     * 6. 设置对一些LiveData的观察
     * 7. 绑定服务PlayService
     * 8. 获取当前播放歌曲（启动时则为上次播放歌曲）
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogUtil.e(TAG, "onCreate")
        // 1
        setViewBinding()
        // 2
        setDrawerLayout()
        // 3
        darkThemeSetting()
        // 4
        initFragment()
        // 5
        onClickListener()
        // 6
        observeLiveData()
        // 7
        bindPlayService()
        // 8
        getCurrentMusic()
    }

    private fun onMusicUpdated(cm: Repository.CurrentMusic?) {
        LogUtil.e(TAG, "onMusicUpdated")
        if (cm?.music == null) {
            return
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

    /**
     * 利用生成的ViewBinding对象绑定视图
     */
    private fun setViewBinding() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        toolbarBinding = ViewToolbarBinding.bind(binding.root)
        setContentView(binding.root)
        setSupportActionBar(toolbarBinding.toolbar)
        viewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]
    }

    /**
     * DrawerLayout设置
     */
    private fun setDrawerLayout() {
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.ic_menu)
        }
        sharedPreferences = SharedPreferencesUtil.sharedPreferences("user_info", this)
        if (UserResDao.hasSetAutoLogin(sharedPreferences)) {
            viewModel.login()
        }
        binding.navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                /*R.id.nav_night_mode -> {
                    "夜间模式".showToast(this)
                }*/
                R.id.nav_time_to_close -> {
                    // 定时关闭
                }
                R.id.nav_about -> {
                    // 关于
                }
                R.id.nav_logout -> {
                    // 退出登录
                    viewModel.logout()
                }
                R.id.nav_close_app -> {
                    // 退出APP
                    viewModel.stopService()
                    ActivityCollector.finishAll()
                }
            }
            binding.drawerLayout.closeDrawers()
            it.isCheckable = false
            true
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

    /**
     * 设置ViewPager2事件响应、加载Fragment
     */
    private fun initFragment() {
        binding.mainViewPager.adapter = MainViewPagerAdapter(this)
        binding.mainViewPager.offscreenPageLimit = 1
        binding.mainViewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                changeTab(position)
                viewModel.setCurrentTab(position)
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
            R.id.search -> startActivity<SearchActivity>(this) {}   // 跳转SearchActivity
        }
        return true
    }

    /**
     * 设置除ViewPager2和DrawerLayout外的事件响应
     * Bottombar事件响应
     * 下拉刷新事件响应
     */
    private fun onClickListener() {
        // 主页
        binding.bottomContainer.tabMainBtn.setOnClickListener {
            changeTab(0)
        }
        // 我的
        binding.bottomContainer.tabMyBtn.setOnClickListener {
            changeTab(1)
        }
        // 下拉刷新
        binding.loadingMore.setOnRefreshListener {
            var myFragment = Fragment()
            when (binding.mainViewPager.currentItem) {
                0 -> {
                    myFragment = supportFragmentManager.findFragmentByTag("f0")!!
                    (myFragment as MainFragment).refreshFragment()
                }
                1 -> {
                    myFragment = supportFragmentManager.findFragmentByTag("f1")!!
                    (myFragment as MyFragment).refreshFragment()
                }
            }
            binding.loadingMore.isRefreshing = false
        }
        // 音乐播放
        binding.bottomContainer.coverContainer.setOnClickListener {
            // 跳转MusicPlayAcitivity
            viewModel.getPlayListEvent()
            if (viewModel.currentPlayListEvent.playlist.isNullOrEmpty()) {
                "当前无音乐在播放".showToast(this)
            } else {
                viewModel.apply {
                    getProgress()
                    getDuration()
                    startActivity<MusicPlayActivity>(this@MainActivity) {
                        putExtra("play_list_event", viewModel.currentPlayListEvent)
                        putExtra("now_playing", viewModel.currentMusic.value?.music)
                        putExtra("is_playing", viewModel.currentMusic.value?.isPlaying)
                        putExtra("progress", progress)
                        putExtra("duration", duration)
                    }
                }
            }
        }
        // 当前播放列表
        binding.playListFloatButton.setOnClickListener {
            viewModel.getPlayListEvent()
            startActivity<PlayListActivity>(this) {
                putExtra("play_list_event", viewModel.currentPlayListEvent)
                putExtra("is_play_activity", false)
            }
        }
        binding.navView.getHeaderView(0).findViewById<ImageView>(R.id.avatar).setOnClickListener {
            if (viewModel.isLogin.value == true) {
                "「个人中心」待开发，敬请期待".showToast(this)
            } else {
                // 启动登录页
                LogUtil.e(TAG, "登录页启动")
                startActivity<LoginActivity>(this) {}
            }
        }
    }

    /**
     *设置对一些LiveData的观察
     */
    @SuppressLint("ResourceAsColor")
    private fun observeLiveData() {
        // 观察当前ViewPager内容
        viewModel.currentTab.observe(this) {
            if (binding.mainViewPager.currentItem != it) {
                changeTab(it)
            }
        }
        // 观察currentMusic的变化，及时更新UI
        viewModel.currentMusic.observe(this) {
            onMusicUpdated(it)
            viewModel.music = it
        }
        // 观察当前登录状态
        viewModel.isLogin.observe(this) {
            if (it) {
                viewModel.getUserLevelResponse()
                viewModel.getUserProfile()
                binding.navView.getHeaderView(0).apply {
                    findViewById<TextView>(R.id.nickname).text = viewModel.profile!!.nickname
                    Glide.with(this@MainActivity)
                        .load(viewModel.profile!!.avatarUrl + "?param=300y300")
                        .into(findViewById(R.id.avatar))
                    Glide.with(this@MainActivity).load(viewModel.profile!!.backgroundUrl).apply(
                        RequestOptions.bitmapTransform(BlurTransformation(this@MainActivity, 25, 2))
                    ).into(findViewById(R.id.nav_background))

                    val simpleTarget: SimpleTarget<Bitmap> = object : SimpleTarget<Bitmap>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            // 使用Palette类提取图片的颜色信息
                            Palette.from(resource).generate { palette ->
                                val swatch = palette?.dominantSwatch
                                if (swatch != null) {
                                    findViewById<TextView>(R.id.level).setTextColor(swatch.bodyTextColor) //设置文本颜色
                                    findViewById<TextView>(R.id.nickname).setTextColor(swatch.bodyTextColor) //设置文本颜色
                                    findViewById<TextView>(R.id.lv).setTextColor(swatch.bodyTextColor) //设置文本颜色
                                    findViewById<TextView>(R.id.point).setTextColor(swatch.bodyTextColor) //设置文本颜色
                                }
                            }
                        }
                    }
                    Glide.with(this)
                        .asBitmap()
                        .load(viewModel.profile!!.backgroundUrl)
                        .into(simpleTarget)

                    if (viewModel.profile!!.vipType > 0)
                        findViewById<TextView>(R.id.vip).visibility = View.VISIBLE
                    else
                        findViewById<TextView>(R.id.vip).visibility = View.GONE
                }
            } else {
                viewModel.clearUserProfile()
                viewModel.clearUserLevel()
                binding.navView.getHeaderView(0).apply {
                    findViewById<TextView>(R.id.nickname).text = "点击头像登录"
                    findViewById<TextView>(R.id.nickname).setTextColor(R.color.black)
                    findViewById<TextView>(R.id.level).text = "0"
                    findViewById<TextView>(R.id.level).setTextColor(R.color.black)
                    findViewById<TextView>(R.id.lv).setTextColor(R.color.black)
                    findViewById<TextView>(R.id.point).setTextColor(R.color.black)
                    findViewById<ContentLoadingProgressBar>(R.id.level_progress).progress = 0
                    findViewById<TextView>(R.id.vip).visibility = View.GONE
                    findViewById<ImageView>(R.id.avatar).setImageResource(R.drawable.default_avatar)
                    findViewById<ImageView>(R.id.nav_background).setImageResource(0)
                }
            }
        }
        // 观察UserLevel
        viewModel.userLevelResponse.observe(this) {
            if (it.getOrDefault(false)) {
                viewModel.getUserLevel()
                binding.navView.getHeaderView(0).apply {
                    findViewById<TextView>(R.id.level).text = viewModel.userLevel!!.level.toString()
                    findViewById<ContentLoadingProgressBar>(R.id.level_progress).progress =
                        (viewModel.userLevel!!.progress * 100).toInt()
                }
            } else {
                viewModel.clearUserLevel()
            }
        }
        // 观察登录结果
        viewModel.loginResponse.observe(this) {
            val result = it.getOrNull()
            if (result != null) {
                viewModel.apply {
                    setCookie(result.cookie)
                    setUserProfile(result.profile)
                    setLoginState(true)
                }
                "自动登录成功".showToast(this)
            } else {
                "自动登录失败".showToast(this)
            }
        }
    }

    /**
     * 绑定服务PlayService
     */
    private fun bindPlayService() {
        viewModel.bindPlayService(this)
        LogUtil.e(TAG, "绑定服务")
    }

    private fun getCurrentMusic() {
        currentMusic = viewModel.getCurrentMusic()
    }

    /**
     * 切换Fragment
     */
    private fun changeTab(position: Int) {
        binding.bottomContainer.apply {
            tabMainBtn.isSelected = false
            tabMainTv.isSelected = false
            tabMyBtn.isSelected = false
            tabMyTv.isSelected = false
        }
        when (position) {
            0 -> {
                binding.bottomContainer.apply {
                    tabMainBtn.isSelected = true
                    tabMainTv.isSelected = true
                }
            }
            1 -> {
                binding.bottomContainer.apply {
                    tabMyBtn.isSelected = true
                    tabMyTv.isSelected = true
                }
            }
        }
        binding.mainViewPager.setCurrentItem(position, true)
    }

    companion object {

        /*fun startThisActivity(activity: AppCompatActivity) {
            val intent = Intent(activity, MainActivity::class.java)
            activity.startActivity(intent)
            activity.finish()
        }*/

        private const val TAG = "MainActivity"

    }

    override fun onStart() {
        super.onStart()
        LogUtil.e(TAG, "onStart")
    }

    override fun onResume() {
        super.onResume()
        LogUtil.e(TAG, "onResume")
    }

    override fun onPause() {
        super.onPause()
        LogUtil.e(TAG, "onPause")
    }

    override fun onStop() {
        super.onStop()
        LogUtil.e(TAG, "onStop")
    }

    override fun onDestroy() {
        LogUtil.e(TAG, "onDestroy")
        super.onDestroy()
    }
}
