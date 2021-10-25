package com.m2mmusic.android.weex.intent

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.m2mmusic.android.R
import com.taobao.weex.IWXRenderListener
import com.taobao.weex.WXSDKInstance
import com.taobao.weex.common.WXRenderStrategy
import com.taobao.weex.utils.WXFileUtils

/**
 * @ClassName WXPageActivity
 * @Description TODO
 * @Author suwei
 * @Date 2021/10/25 17:06
 */
class WXPageActivity : AppCompatActivity(), IWXRenderListener {

    lateinit var mWXSDKInstance: WXSDKInstance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wxpage)
        mWXSDKInstance = WXSDKInstance(this)
        mWXSDKInstance.registerRenderListener(this)
        /**
         * WXSample 可以替换成自定义的字符串，针对埋点有效。
         * template 是.we transform 后的 js文件。
         * option 可以为空，或者通过option传入 js需要的参数。例如bundle js的地址等。
         * jsonInitData 可以为空。
         * width 为-1 默认全屏，可以自己定制。
         * height =-1 默认全屏，可以自己定制。
         */
        /*mWXSDKInstance.renderByUrl(
            "WXSample",
            "http://172.16.11.122:8081/dist/index.js?wsport=8082&_wx_tpl=http",
            null,
            null,
            WXRenderStrategy.APPEND_ASYNC
        )*/
        mWXSDKInstance.render(
            "WXSample",
            WXFileUtils.loadAsset("index.js", this),
            null,
            null,
            -1,
            -1,
            WXRenderStrategy.APPEND_ASYNC
        )
    }

    override fun onViewCreated(instance: WXSDKInstance?, view: View?) {
        setContentView(view)
    }

    override fun onRenderSuccess(instance: WXSDKInstance?, width: Int, height: Int) {
    }

    override fun onRefreshSuccess(instance: WXSDKInstance?, width: Int, height: Int) {
    }

    override fun onException(instance: WXSDKInstance?, errCode: String?, msg: String?) {
    }

    override fun onResume() {
        super.onResume()
        mWXSDKInstance.onActivityResume()
    }

    override fun onPause() {
        super.onPause()
        mWXSDKInstance.onActivityPause()
    }

    override fun onStop() {
        super.onStop()
        mWXSDKInstance.onActivityStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mWXSDKInstance.onActivityDestroy()
    }
}