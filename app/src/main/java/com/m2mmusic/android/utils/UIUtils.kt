package com.m2mmusic.android.utils

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.os.AsyncTask
import android.os.Build
import android.util.Base64
import android.view.View
import android.view.WindowManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.m2mmusic.android.R
import com.m2mmusic.android.application.M2MMusicApplication
import java.io.ByteArrayOutputStream

/**
 * 设置状态栏透明
 */
fun setStatusBarTranslucent(activity: Activity) {
    val window = activity.window
    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    val decorView = window.decorView
    val option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    decorView.systemUiVisibility = option
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    window.statusBarColor = Color.TRANSPARENT
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }
    if (isDarkTheme(activity.applicationContext)) {
        activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    }
}

/**
 * 导航栏、状态栏的隐藏（全屏模式）
 */
fun setFullScreen(activity: Activity) {
    // 全屏展示
    // 全屏显示，隐藏状态栏和导航栏，拉出状态栏和导航栏显示一会儿后消失。
    activity.window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
}

/**
 * 修改状态栏文字颜色
 */
fun setStatusBarTextColor(
    activity: Activity,
    // true则文字为深色，false则文字为浅色
    dark: Boolean
): Unit {
    val decor = activity.window.decorView
    if (dark) {
        decor.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    } else {
        decor.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    }
}

/**
 * 获得状态栏高度
 */
fun getStatusBarHeight(context: Context): Int {
    val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
    return context.resources.getDimensionPixelSize(resourceId)
}

fun isDarkTheme(context: Context): Boolean {
    val flag = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return flag == Configuration.UI_MODE_NIGHT_YES
}

/**
 * bitmap to string
 */
fun getStringCover(bitmap: Bitmap? = null): String {
    val btStream = ByteArrayOutputStream()
    val bitmapTemp = bitmap ?: BitmapFactory.decodeResource(
        M2MMusicApplication.context.resources,
        R.drawable.default_cover
    )
    bitmapTemp.compress(Bitmap.CompressFormat.PNG, 100, btStream)
    val resultpicture = btStream.toByteArray()
    return Base64.encodeToString(resultpicture, Base64.DEFAULT)
}

/**
 * string to bitmap
 */
fun string2Bitmap(bitmapString: String?): Bitmap? {
    var bitmap: Bitmap? = null
    if (bitmapString != null) {
        try {
            val b = Base64.decode(bitmapString, Base64.DEFAULT)
            bitmap = BitmapFactory.decodeByteArray(
                b, 0,
                b.size
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    return bitmap ?: BitmapFactory.decodeResource(
        M2MMusicApplication.context.resources,
        R.drawable.default_cover
    )
}

/**
 * 从localmusic中提取album封面bitmap
 */
fun loadingCover(mediaUri: String): String {
    val mediaMetadataRetriever = MediaMetadataRetriever()
    mediaMetadataRetriever.setDataSource(mediaUri)
    val picture = mediaMetadataRetriever.embeddedPicture
    return if (picture == null) getStringCover() else Base64.encodeToString(picture, Base64.DEFAULT)
}

/**
 * 从onlineURL获取bitmap
 */
suspend fun loadingOnlineCover(url: String): Bitmap {
    if (url.isEmpty()) {
        return string2Bitmap(null)!!
    }
    return Glide.with(M2MMusicApplication.context)
        .asBitmap()
        .load("$url?param=400y400")
        .apply(option)
        .submit(400, 400)
        .get()
}

val option = RequestOptions()
    .error(R.drawable.default_cover)
    .diskCacheStrategy(DiskCacheStrategy.ALL)

class BitmapTask(_url: String) : AsyncTask<Unit, Unit, Bitmap>() {
    val url = _url

    override fun doInBackground(vararg params: Unit?): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            bitmap = Glide.with(M2MMusicApplication.context)
                .asBitmap()
                .load(url)
                .apply(option)
                .submit(400, 400)
                .get()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bitmap
    }

}