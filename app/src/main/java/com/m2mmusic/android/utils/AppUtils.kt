package com.m2mmusic.android.utils

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * MD5加密
 */
fun encode(password: String): String {
    try {
        val instance: MessageDigest = MessageDigest.getInstance("MD5")//获取md5加密对象
        val digest:ByteArray = instance.digest(password.toByteArray())//对字符串加密，返回字节数组
        val sb : StringBuffer = StringBuffer()
        for (b in digest) {
            val i :Int = b.toInt() and 0xff//获取低八位有效值
            var hexString = Integer.toHexString(i)//将整数转化为16进制
            if (hexString.length < 2) {
                hexString = "0" + hexString//如果是一位的话，补0
            }
            sb.append(hexString)
        }
        return sb.toString()

    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
    }

    return ""
}

/**
 * 封装启动Activity方法
 */
inline fun <reified T> startActivity(context: Context, block: Intent.() -> Unit) {
    val intent = Intent(context, T::class.java)
    intent.block()
    context.startActivity(intent)
}

/**
 * 封装绑定Service方法
 */
inline fun <reified T> bindService(
    context: Context,
    connection: ServiceConnection,
    block: Intent.() -> Unit
) {
    val intent = Intent(context, T::class.java)
    intent.block()
    context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
}

/**
 * 封装启动Service方法
 */
inline fun <reified T> startService(
    context: Context,
    block: Intent.() -> Unit
) {
    val intent = Intent(context, T::class.java)
    intent.block()
    context.startService(intent)
}

/**
 * 封装停止Service方法
 */
inline fun <reified T> stopService(
    context: Context,
    block: Intent.() -> Unit
) {
    val intent = Intent(context, T::class.java)
    intent.block()
    context.stopService(intent)
}

/**
 * 获取当前进程名
 */
fun getCurrProcessName(context: Context): String? {
    try {
        val currProcessId = android.os.Process.myPid()
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val processInfos = am.runningAppProcesses
        if (processInfos != null) {
            for (info in processInfos) {
                if (info.pid == currProcessId) {
                    return info.processName
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

/**
 * 开启一个协程
 */
fun launch(block: suspend () -> Unit, error: suspend (Throwable) -> Unit) = CoroutineScope(
    Dispatchers.Main
).launch {
    try {
        block()
    } catch (e: Throwable) {
        try {
            error(e)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}