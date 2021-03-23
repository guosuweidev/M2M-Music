package com.m2mmusic.android.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class CustomSwipeRefreshLayout : SwipeRefreshLayout {

    private var mInitialDownYValue = 0
    private var miniTouchSlop = 0

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        miniTouchSlop = ViewConfiguration.get(context).scaledTouchSlop*8
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if (!isEnabled) {
            return false
        }
        val action = ev?.actionMasked
        ev?.let {
            when (action) {
                MotionEvent.ACTION_DOWN -> mInitialDownYValue = it.y.toInt()
                MotionEvent.ACTION_MOVE -> {
                    val yDiff = it.y - mInitialDownYValue
                    if (yDiff < miniTouchSlop) {
                        return false
                    }
                }
            }
        }
        return super.onInterceptTouchEvent(ev)
    }
}