package com.os.drewel.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nostra13.universalimageloader.core.ImageLoader
import com.os.drewel.R
import com.os.drewel.apicall.responsemodel.categoryresponsemodel.Home
import com.os.drewel.apicall.responsemodel.productlistresponsemodel.Brand
import com.os.drewel.application.DrewelApplication
import kotlinx.android.synthetic.main.brand_row_selector.view.*
import kotlinx.android.synthetic.main.home_screen_offer_layout.view.*


class HomeBannerAdapter(val mContext: Context?, private val homeBannerList: List<Home>) : RecyclerView.Adapter<HomeBannerAdapter.HomeBannerHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeBannerHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.home_screen_offer_layout, parent, false)
        return HomeBannerHolder(view)
    }

    override fun onBindViewHolder(holder: HomeBannerHolder, position: Int) {

        ImageLoader.getInstance().displayImage(homeBannerList[position].img, holder.itemView.offerIv, DrewelApplication.getInstance().options)
//        val viewWidthToBitmapWidthRatio = holder.itemView.offerIv.getWidth() as Double / bitmap.getWidth() as Double
//        holder.itemView.offerIv.getLayoutParams().height = bitmap.getHeight() * viewWidthToBitmapWidthRatio
    }

    override fun getItemCount(): Int {
        return homeBannerList.size;
    }


    inner class HomeBannerHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}