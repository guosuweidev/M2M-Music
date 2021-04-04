package com.m2mmusic.android.ui.activity

import android.util.Log
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class Test {

    val sPool = Executors.newScheduledThreadPool(2)!!    //2个线程

    val taskOne = object : TimerTask() {

        override fun run() {
            Log.i("Main", "taskOne")
        }
    }


    val taskTwo = object : TimerTask() {

        override fun run() {
            Log.i("Main", "taskTwo")
        }
    }

    init {

        sPool.scheduleWithFixedDelay(taskOne,1000,1000, TimeUnit.MILLISECONDS)
        sPool.scheduleWithFixedDelay(taskTwo,1500,1500, TimeUnit.MILLISECONDS)
    }
}