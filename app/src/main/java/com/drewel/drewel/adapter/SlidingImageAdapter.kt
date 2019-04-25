package com.drewel.drewel.adapter

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.nostra13.universalimageloader.core.ImageLoader

import com.drewel.drewel.R
import com.drewel.drewel.activity.ZoomActivity
import com.drewel.drewel.application.DrewelApplication


class SlidingImageAdapter(private val context: Context, private val IMAGES: List<String>) : PagerAdapter() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun getCount(): Int {
        return IMAGES.size
    }

    override fun instantiateItem(view: ViewGroup, position: Int): Any {
        val imageLayout = inflater.inflate(R.layout.child_product_layout, view, false)!!

        val imageView = imageLayout.findViewById<View>(R.id.imv_product) as ImageView

        ImageLoader.getInstance().displayImage(IMAGES[position], imageView, DrewelApplication.getInstance().options)


        view.addView(imageLayout, 0)
        imageView.setOnClickListener{context.startActivity(Intent(context, ZoomActivity::class.java).putExtra("URL",IMAGES[position]))}
        return imageLayout
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun restoreState(state: Parcelable?, loader: ClassLoader?) {}

    override fun saveState(): Parcelable? {
        return null
    }

}
