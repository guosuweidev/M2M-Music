package com.m2mmusic.android.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.m2mmusic.android.R
import com.m2mmusic.android.databinding.ActivitySearchBinding
import com.m2mmusic.android.logic.model.SearchType
import com.m2mmusic.android.ui.adapter.SearchViewPagerAdapter
import com.m2mmusic.android.ui.fragment.SearchFragment

class SearchActivity : BaseActivity(), SearchView.OnQueryTextListener,
    AdapterView.OnItemClickListener {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var viewModel: SearchActivityViewModel
    private lateinit var suggestItems: Array<String?>
    private var mImm: InputMethodManager? = null
    private var queryString = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[SearchActivityViewModel::class.java]
        setSupportActionBar(binding.searchToolbar)
        supportActionBar!!.apply {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
        }
        initView()

        // 观察搜索建议
        viewModel.searchSuggestResponse.observe(this) {
            val result = it.getOrNull()
            if (result != null) {
                suggestItems = arrayOfNulls(result.size)
                for ((i, a) in result.withIndex()) {
                    suggestItems[i] = a.keyword
                }
                binding.apply {
                    listItem.visibility = View.VISIBLE
                    listItem.adapter = ArrayAdapter<String>(
                        this@SearchActivity,
                        android.R.layout.simple_list_item_1, suggestItems
                    )
                }
            }
        }
    }

    private fun initView() {
        binding.searchView.apply {
            adapter = SearchViewPagerAdapter(this@SearchActivity)
            TabLayoutMediator(binding.searchTab, binding.searchView) { tab, position ->
                tab.text = when (position) {
                    0 -> "单曲"
                    1 -> "专辑"
                    2 -> "歌手"
                    3 -> "歌单"
                    else -> ""
                }
            }.attach()
            offscreenPageLimit = 3
            registerOnPageChangeCallback(object :
                ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                }
            })
        }

        binding.searchInput.apply {
            setOnQueryTextListener(this@SearchActivity)
            queryHint = "输入关键词"
            setIconifiedByDefault(false)
            requestFocus()
            setOnSearchClickListener {
                binding.listItem.visibility = View.VISIBLE
            }
            setOnCloseListener {
                binding.listItem.visibility = View.GONE
                false
            }
        }
        binding.listItem.onItemClickListener = this
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        hideInputManager()
        binding.listItem.visibility = View.GONE
        supportFragmentManager.apply {
            (findFragmentByTag("f0") as SearchFragment).keyWords = query ?: ""
            (findFragmentByTag("f1") as SearchFragment).keyWords = query ?: ""
            (findFragmentByTag("f2") as SearchFragment).keyWords = query ?: ""
            (findFragmentByTag("f3") as SearchFragment).keyWords = query ?: ""
        }
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if (newText == queryString) {
            return true
        }
        queryString = newText ?: ""
        if (queryString.trim { it <= ' ' } != "") {
            viewModel.getSearchSuggest(queryString)
        }
        return true
    }

    private fun hideInputManager() {
        if (mImm != null) {
            mImm!!.hideSoftInputFromWindow(binding.searchInput.windowToken, 0)
        }
        binding.searchInput.clearFocus()
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (!::suggestItems.isInitialized)
            return
        if (position > suggestItems.size)
            return
        queryString = suggestItems[position]!!
        binding.searchInput.setQuery(queryString, true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }
}