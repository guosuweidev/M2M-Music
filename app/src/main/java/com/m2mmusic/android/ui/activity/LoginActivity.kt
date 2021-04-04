package com.m2mmusic.android.ui.activity

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import com.m2mmusic.android.application.SharedPreferencesUtil
import com.m2mmusic.android.application.UserResDao
import com.m2mmusic.android.databinding.ActivityLoginBinding
import com.m2mmusic.android.logic.Repository
import com.m2mmusic.android.utils.encode
import com.m2mmusic.android.utils.showToast

class LoginActivity : BaseActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginActivityViewModel
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[LoginActivityViewModel::class.java]

        initView()
        setListener()

        // 观察登录结果
        viewModel.loginResponse.observe(this) {
            binding.loginProgress.visibility = View.GONE
            val result = it.getOrNull()
            if (result != null) {
                viewModel.apply {
                    setCookie(result.cookie)
                    setUserProfile(result.profile)
                    setLoginState(true)
                    if (binding.rememberPassword.isChecked) {
                        if (binding.autoLogin.isChecked) {
                            UserResDao.saveLoginInfo(
                                sharedPreferences,
                                binding.phoneInput.text.toString(),
                                binding.passwordInput.text.toString(),
                                true,
                                true
                            )
                            UserResDao.saveUserProfile(sharedPreferences, result.profile)
                        } else
                            UserResDao.saveLoginInfo(
                                sharedPreferences,
                                binding.phoneInput.text.toString(),
                                binding.passwordInput.text.toString(),
                                true,
                                false
                            )
                    }
                }
                "登录成功".showToast(this)
                finish()
            } else {
                binding.passwordInput.setText("")
            }
        }
    }

    private fun initView() {
        sharedPreferences = SharedPreferencesUtil.sharedPreferences("user_info", this)
        if (UserResDao.hasSavePassword(sharedPreferences)) {
            binding.apply {
                phoneInput.setText(UserResDao.getUserPhone(sharedPreferences))
                passwordInput.setText(UserResDao.getUserPassword(sharedPreferences) ?: "")
                rememberPassword.isChecked = true
            }
        }
    }

    private fun setListener() {
        binding.apply {
            // 登录按钮
            loginButton.setOnClickListener {
                if (phoneInput.text.isNullOrEmpty())
                    if (passwordInput.text.isNullOrEmpty())
                        "请输入手机号和密码".showToast(this@LoginActivity)
                    else {
                        "请输入手机号".showToast(this@LoginActivity)
                    }
                else if (passwordInput.text.isNullOrEmpty()) "请输入密码".showToast(this@LoginActivity)
                else {
                    // 去登录
                    binding.loginProgress.visibility = View.VISIBLE
                    login()
                }
            }
            autoLogin.setOnCheckedChangeListener { buttonView, isChecked ->
                rememberPassword.isChecked = true
            }
        }
    }

    private fun login() {
        with(binding) {
            viewModel.login(phoneInput.text.toString(), encode(passwordInput.text.toString()))
        }
    }
}