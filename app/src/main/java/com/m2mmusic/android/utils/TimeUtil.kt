package com.m2mmusic.android.utils

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by 小小苏 at 2021/3/21
 * 时间工具
 */
object TimeUtil {

    fun getTimestamp() = System.currentTimeMillis()

    @SuppressLint("SimpleDateFormat")
    fun getDateTime(s: Long): String {
        return try {
            val sdf = SimpleDateFormat("yyyy.MM.dd")
            val netDate = Date(s)
            sdf.format(netDate)
        } catch (e: Exception) {
            e.toString()
        }
    }
}