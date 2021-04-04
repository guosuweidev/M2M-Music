package com.m2mmusic.android.ui.activity

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.m2mmusic.android.logic.Repository
import com.m2mmusic.android.logic.model.LoginResponse
import com.m2mmusic.android.utils.TimeUtil

class LoginActivityViewModel : ViewModel() {

    private var phone: String = ""
    private var password: String = ""

    private val loginByPhoneLiveData = MutableLiveData<Int>()

    val loginResponse = Transformations.switchMap(loginByPhoneLiveData) {
        Repository.loginByPhone(phone, password)
    }

    fun login(phone: String, password: String) {
        this.phone = phone
        this.password = password
        loginByPhoneLiveData.value = loginByPhoneLiveData.value
    }

    fun setCookie(cookie:String){
        Repository.setCookie(cookie)
    }
    fun setUserProfile(profile: LoginResponse.Profile) {
        Repository.setUserProfile(profile)
    }
    fun setLoginState(s: Boolean) {
        Repository.setLoginState(s)
    }
}