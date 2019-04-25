package com.drewel.drewel.activity

import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.WindowManager
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.drewel.drewel.R
import com.drewel.drewel.application.DrewelApplication
import kotlinx.android.synthetic.main.activity_zoom.*

class ZoomActivity : AppCompatActivity() {
    internal var imageLoader: com.nostra13.universalimageloader.core.ImageLoader = com.nostra13.universalimageloader.core.ImageLoader.getInstance()
    internal var options = DisplayImageOptions.Builder().cacheInMemory(true)
            .cacheOnDisc(true).resetViewBeforeLoading(true).build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE
            val window = window
            val background = resources.getDrawable(android.R.color.transparent)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = resources.getColor(android.R.color.transparent)
            window.setBackgroundDrawable(background)
        }
        setContentView(R.layout.activity_zoom)
        imageLoader.init(ImageLoaderConfiguration.createDefault(this))
        var url = intent.getStringExtra("URL")
        ImageLoader.getInstance().displayImage(url, img_view, DrewelApplication.getInstance().options)
        img_close.setOnClickListener {
            finish()
        }
    }
}
