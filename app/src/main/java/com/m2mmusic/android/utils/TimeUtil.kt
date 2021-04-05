package com.m2mmusic.android.utils

import android.annotation.SuppressLint
import android.icu.util.Calendar
import android.os.Build
import android.text.format.Time
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

    /**
     * 获取当前日期
     */
    fun getCalendarDay(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Calendar.getInstance().get(Calendar.DAY_OF_MONTH).toString()
        } else {
            val time = Time()
            time.setToNow()
            time.monthDay.toString()
        }
    }
}