package com.m2mmusic.android.utils

import android.app.Activity

/**
 * Created by 小小苏 at 2021/3/22
 * ActivityCollector
 * Activity管理工具
 * 一键关闭所有Activity
 */
object ActivityCollector {

    private val activities = ArrayList<Activity>()

    fun addActivity(activity: Activity) {
        activities.add(activity)
    }

    fun removeActivity(activity: Activity) {
        activities.remove(activity)
    }

    fun finishAll() {
        for (activity in activities) {
            if (!activity.isFinishing) {
                activity.finish()
            }
        }
        activities.clear()
    }

}