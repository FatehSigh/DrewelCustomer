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

class TransactionAdpater(val mContext: Context) : RecyclerView.Adapter<TransactionAdpater.TransactionHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.transaction_item, parent, false)

        return TransactionHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionHolder, position: Int) {

    }


    override fun getItemCount(): Int {
        return 9
    }


    inner class TransactionHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        init {
//            itemView.removeCouponCode.setOnClickListener(this)
        }

        override fun onClick(v: View) {
//            couponCodeRemove.onCouponCodeRemove(adapterPosition)
        }

    }

}