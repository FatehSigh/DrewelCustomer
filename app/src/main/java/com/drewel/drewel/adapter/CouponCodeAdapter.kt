package com.drewel.drewel.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.drewel.drewel.R
import com.drewel.drewel.apicall.responsemodel.myorderdetailresponsemodel.Coupon
import kotlinx.android.synthetic.main.layout_applied_coupon_code_item.view.*

/**
 * Created by monikab on 3/13/2018.
 */

class CouponCodeAdapter(val mContext: Context, private val appliedCouponCodesAllInfo: MutableList<Coupon>) : RecyclerView.Adapter<CouponCodeAdapter.AppliedCouponCodeHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppliedCouponCodeHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_applied_coupon_code_item, parent, false)
        return AppliedCouponCodeHolder(view)
    }

    override fun onBindViewHolder(holder: AppliedCouponCodeHolder, position: Int) {
        holder.itemView.removeCouponCode.visibility = View.GONE
        holder.itemView.couponCodeTv.text = appliedCouponCodesAllInfo[position].coupone_code
        holder.itemView.couponTv.setTextColor(mContext.resources.getColor(R.color.grey_color_txt))
        holder.itemView.discountHint.setTextColor(mContext.resources.getColor(R.color.grey_color_txt))
        holder.itemView.discountTv.setTextColor(mContext.resources.getColor(R.color.grey_color_txt))
        holder.itemView.discountTv.text = String.format("%.3f", appliedCouponCodesAllInfo[position].amount!!.toDouble()) + " " + mContext.getString(R.string.omr)
    }


    override fun getItemCount(): Int {
        return appliedCouponCodesAllInfo.size;
    }


    inner class AppliedCouponCodeHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        init {
            itemView.removeCouponCode.setOnClickListener(this)
        }

        override fun onClick(v: View) {
        }


    }

}