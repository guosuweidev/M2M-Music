package com.m2mmusic.android.ui.activity

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.m2mmusic.android.logic.Repository
import com.m2mmusic.android.utils.TimeUtil

class SearchActivityViewModel: ViewModel() {

    private val searchSuggestLiveData = MutableLiveData<String>()

    val searchSuggestResponse = Transformations.switchMap(searchSuggestLiveData) {
        Repository.getSearchSuggest(it, TimeUtil.getTimestamp())
    }

    fun getSearchSuggest(keywords: String) {
        searchSuggestLiveData.value = keywords
    }

}