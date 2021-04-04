package com.m2mmusic.android.utils

import android.content.Context
import android.net.*
import android.os.Build
import androidx.lifecycle.LiveData
import com.m2mmusic.android.application.M2MMusicApplication

private const val TAG = "NetworkInfo"

/**
 * Created by 小小苏 at 2021/3/22
 * 网络工具集合
 * 判断网络连接状态
 * 判断当前网络类型
 */
object NetworkUtil {

    @Suppress("DEPRECATION")
    fun isInternetAvailable(context: Context): Boolean {
        var result = false
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cm?.run {
                cm.getNetworkCapabilities(cm.activeNetwork)?.run {
                    result = when {
                        hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                        hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                        hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                        else -> false
                    }
                }
            }
        } else {
            cm?.run {
                cm.activeNetworkInfo?.run {
                    if (type == ConnectivityManager.TYPE_WIFI) {
                        result = true
                    } else if (type == ConnectivityManager.TYPE_MOBILE) {
                        result = true
                    }
                }
            }
        }
        return result
    }

}

object NetworkState {

    // 无网络
    const val UNCONNECTED = 0

    // 网络连接
    const val CONNECTED = 1

    // WIFI
    const val WIFI = 2

    // 移动网络
    const val CELLULAR = 3

}

class NetworkLiveData : LiveData<Int>() {

    private var networkCallback: ConnectivityManager.NetworkCallback
    private var request: NetworkRequest
    private var manager: ConnectivityManager

    init {
        networkCallback = NetworkCallbackImpl()
        request = NetworkRequest.Builder().build()
        manager = M2MMusicApplication.context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    override fun onActive() {
        super.onActive()
        manager.registerNetworkCallback(request, networkCallback)
    }

    override fun onInactive() {
        super.onInactive()
        manager.unregisterNetworkCallback(networkCallback)
    }

    class NetworkCallbackImpl : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            LogUtil.d(TAG, "网络已连接")
            getInstance().postValue(NetworkState.CONNECTED)
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            LogUtil.d(TAG, "网络已断开")
            getInstance().postValue(NetworkState.UNCONNECTED)
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                LogUtil.d(TAG, "WiFi已连接")
                getInstance().postValue(NetworkState.WIFI)
            } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                LogUtil.d(TAG, "移动网络已连接")
                getInstance().postValue(NetworkState.CELLULAR)
            }
        }
    }

    object NetworkLiveDataHolder {
        val INSTANCE = NetworkLiveData()
    }

    companion object {
        fun getInstance(): NetworkLiveData {
            return NetworkLiveDataHolder.INSTANCE
        }
    }

}

