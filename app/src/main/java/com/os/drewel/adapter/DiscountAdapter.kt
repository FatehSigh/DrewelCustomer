package com.os.drewel.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nostra13.universalimageloader.core.ImageLoader
import com.os.drewel.R
import com.os.drewel.apicall.responsemodel.couponresponsemodel.Coupon
import com.os.drewel.application.DrewelApplication
import com.os.drewel.utill.Utils
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.discounts_row.view.*
import java.text.NumberFormat

class DiscountAdapter(val mContext: Context?, private val couponList: List<Coupon>) : RecyclerView.Adapter<DiscountAdapter.DiscountHolder>() {


     var applyCouponCodeClickSubject: PublishSubject<Int> = PublishSubject.create<Int>()
    var isFromCheckout = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiscountHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.discounts_row, parent, false)

        return DiscountHolder(view)
    }

    override fun onBindViewHolder(holder: DiscountHolder, position: Int) {

        ImageLoader.getInstance().displayImage(couponList[position].img, holder.itemView.discountImageIv, DrewelApplication.getInstance().options)
        val expireDate = mContext?.getString(R.string.expires) + " " + Utils.getInstance().convertTimeFormat(couponList[position].expiresOn!!, "yyyy-MM-dd", "dd MMM yyyy")

        holder.itemView.expireDateTv.text = expireDate

        holder.itemView.offerDescriptionTv.text = couponList[position].couponDescription
        holder.itemView.discountPercentageTv.text = NumberFormat.getInstance().format(couponList[position].discount!!.toDouble())
        holder.itemView.couponCodeTv.text = couponList[position].couponCode

        holder.itemView.isRedeemTv.setOnClickListener({
            applyCouponCodeClickSubject.onNext(holder.layoutPosition)
        })

    }

    override fun getItemCount(): Int {
        return couponList.size
    }

     class DiscountHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}
