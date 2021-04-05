package com.m2mmusic.android.ui.activity

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.m2mmusic.android.R
import com.m2mmusic.android.databinding.ActivityAboutBinding
import mehdi.sakout.aboutpage.AboutPage
import mehdi.sakout.aboutpage.Element


class AboutActivity : BaseActivity() {

    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.aboutToolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val aboutPage = AboutPage(this).isRTL(false)
            .enableDarkMode(false)
            .setImage(R.drawable.nfs_pony)
            .setDescription("Made To Measure\n借用我最喜欢的咖啡豆烘焙品牌命名\n努力迎合大众喜好的同时\n不断尝试不一样的……\n\n对我个菜鸟来说虽然有点不切实际\n但目标总是要有的嘛😝")
            .addItem(Element().setTitle("Version 1.0.0"))
            .addGroup("与我联系")
            .addItem(Element().setTitle("酷安：NFS_PONY").setIconDrawable(R.drawable.kuan))
            .addEmail("guosuweilw@qq.com", "guosuweilw@qq.com")
            .addGitHub("https://github.com/guosuweidev/M2M-Music", "HXD给个Star吧")
            .create()

        binding.relativeLayout.addView(aboutPage)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}