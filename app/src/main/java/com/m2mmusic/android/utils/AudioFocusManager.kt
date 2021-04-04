package com.m2mmusic.android.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build

class AudioFocusManager(context: Context) : AudioManager.OnAudioFocusChangeListener {
    private val mAudioManager: AudioManager
    private lateinit var mFocusRequest: AudioFocusRequest
    private lateinit var mAudioAttributes: AudioAttributes
    private var mAudioFocusChangeListener: OnAudioFocusChangeListener? = null

    /**
     * Request audio focus.
     */
    fun requestFocus() : Int {
        val result: Int
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mAudioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
            mFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(mAudioAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(this)
                .build()
            result = mAudioManager.requestAudioFocus(mFocusRequest)
        } else {
            result = mAudioManager.requestAudioFocus(
                this,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
        return result
    }

    /**
     * 监听音频焦点变化
     */
    override fun onAudioFocusChange(focusChange: Int) {
        if (mAudioFocusChangeListener != null) {
            mAudioFocusChangeListener!!.onAudioFocusChange(focusChange)
        }
    }

    /**
     * Release audio focus.
     */
    fun releaseAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mAudioManager.abandonAudioFocusRequest(mFocusRequest)
        } else {
            mAudioManager.abandonAudioFocus(this)
        }
    }


    /**
     * Same as AudioManager.OnAudioFocusChangeListener.
     */
    interface OnAudioFocusChangeListener {
        fun onAudioFocusChange(focusChange: Int)
    }

    fun setOnAudioFocusChangeListener(listener: OnAudioFocusChangeListener?) {
        mAudioFocusChangeListener = listener
    }

    init {
        mAudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
}