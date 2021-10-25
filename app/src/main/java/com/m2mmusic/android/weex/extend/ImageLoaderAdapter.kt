package com.m2mmusic.android.weex.extend

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.m2mmusic.android.R
import com.taobao.weex.WXEnvironment
import com.taobao.weex.WXSDKManager
import com.taobao.weex.adapter.IWXImgLoaderAdapter
import com.taobao.weex.common.WXImageStrategy
import com.taobao.weex.dom.WXImageQuality

/**
 * @ClassName ImageLoaderAdapter
 * @Description TODO
 * @Author suwei
 * @Date 2021/10/25 15:52
 */
class ImageLoaderAdapter : IWXImgLoaderAdapter {

    override fun setImage(url: String?, view: ImageView?, quality: WXImageQuality?, strategy: WXImageStrategy?) {
        if (view != null) {
            Glide.with(WXEnvironment.getApplication()).load(url).placeholder(R.mipmap.ic_launcher).diskCacheStrategy(
                DiskCacheStrategy.RESOURCE).transition(withCrossFade()).dontAnimate().into(view)
        };
    }

}