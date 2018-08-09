package com.os.drewel.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.os.drewel.R
import com.os.drewel.apicall.responsemodel.applycouponresponsemodel.Coupon
import com.os.drewel.delegate.CouponCodeRemove
import kotlinx.android.synthetic.main.layout_applied_coupon_code_item.view.*


/**
 * Created by monikab on 3/13/2018.
 */

class AppliedCouponCodeAdapter(val mContext: Context, private val appliedCouponCodesAllInfo: MutableList<Coupon>, val couponCodeRemove: CouponCodeRemove) : RecyclerView.Adapter<AppliedCouponCodeAdapter.AppliedCouponCodeHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppliedCouponCodeHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_applied_coupon_code_item, parent, false)

        return AppliedCouponCodeHolder(view)
    }

    override fun onBindViewHolder(holder: AppliedCouponCodeHolder, position: Int) {

        holder.itemView.couponCodeTv.text = appliedCouponCodesAllInfo[position].couponCode

        holder.itemView.discountTv.text =String.format("%.3f", appliedCouponCodesAllInfo[position].discountAmount!!.toDouble())+" "+mContext.getString(R.string.omr)
    }


    override fun getItemCount(): Int {
        return appliedCouponCodesAllInfo.size;
    }


    inner class AppliedCouponCodeHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        init {
            itemView.removeCouponCode.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            couponCodeRemove.onCouponCodeRemove(adapterPosition)
        }


    }

}