package com.os.drewel.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.os.drewel.R
import com.os.drewel.apicall.responsemodel.applycouponresponsemodel.Coupon
import com.os.drewel.apicall.responsemodel.walletTransactionResponseModel.Transaction
import com.os.drewel.delegate.CouponCodeRemove
import kotlinx.android.synthetic.main.layout_applied_coupon_code_item.view.*
import kotlinx.android.synthetic.main.transaction_item.view.*
import java.text.SimpleDateFormat


/**
 * Created by monikab on 3/13/2018.
 */

class TransactionAdpater(val mContext: Context, var transaction: MutableList<Transaction>) : RecyclerView.Adapter<TransactionAdpater.TransactionHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.transaction_item, parent, false)

        return TransactionHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionHolder, position: Int) {
        if (transaction[position].type!!.equals("Cr")) {
            holder.itemView.tv_price.text = "+" + transaction[position].amount + " " + mContext.getString(R.string.omr)
            holder.itemView.tv_from_order.text = mContext.getString(R.string.from) + " " + mContext.getString(R.string.order) + ": " + transaction[position].order_id

        } else if (transaction[position].type!!.equals("Db")) {
            holder.itemView.tv_price.text = "-" + transaction[position].amount + " " + mContext.getString(R.string.omr)
            holder.itemView.tv_from_order.text = mContext.getString(R.string.to) + " " + mContext.getString(R.string.order) + ":" + transaction[position].order_id

        }
        holder.itemView.tv_title.text = transaction[position].id

        try {
            val startdate = SimpleDateFormat("yyyy-MM-dd").parse(transaction[position].date!!)
            holder.itemView.tv_time.setText(SimpleDateFormat("dd MMM ''yy").format(startdate))
//            holder.itemView.txt_time.setText(SimpleDateFormat("h:mm a").format(startdate))
        } catch (e: Exception) {
        }
    }


    override fun getItemCount(): Int {
        return transaction.size
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