package com.os.drewel.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nostra13.universalimageloader.core.ImageLoader
import com.os.drewel.R
import com.os.drewel.apicall.responsemodel.loyaltypointresponsemodel.LoyaltyPoint
import com.os.drewel.application.DrewelApplication
import com.os.drewel.utill.Utils
import kotlinx.android.synthetic.main.layout_loyalty_point_transaction_item.view.*
import java.text.NumberFormat


class LoyaltyPointTransactionAdapter(val mContext: Context, var loyaltyPointsTransaction: List<LoyaltyPoint>) : RecyclerView.Adapter<LoyaltyPointTransactionAdapter.LoyaltyPointTransactionHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoyaltyPointTransactionHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_loyalty_point_transaction_item, parent, false)

        return LoyaltyPointTransactionHolder(view)
    }

    override fun onBindViewHolder(holder: LoyaltyPointTransactionHolder, position: Int) {

        ImageLoader.getInstance().displayImage(loyaltyPointsTransaction[position].img, holder.itemView.userImageIv, DrewelApplication.getInstance().options)
        holder.itemView.amountTv.text = NumberFormat.getInstance().format(loyaltyPointsTransaction[position].loyaltyPoints!!.toFloat())+""/*+ mContext.getString(R.string.omr)*/
        val earnedStr = StringBuffer()
        when {
            loyaltyPointsTransaction[position].type.toString() == "Credit" -> {
                holder.itemView.loyaltyPointsStatusTv.text = mContext.getString(R.string.loyalty_point_earned)
                earnedStr.append(mContext.getString(R.string.from))
            }
            loyaltyPointsTransaction[position].userId!!.isNotEmpty() -> {
                holder.itemView.loyaltyPointsStatusTv.text = mContext.getString(R.string.loyalty_point_transferred)
                earnedStr.append(mContext.getString(R.string.to))
            }
            else -> {
                holder.itemView.loyaltyPointsStatusTv.text = mContext.getString(R.string.loyalty_point_redeem)
                earnedStr.append(mContext.getString(R.string.to))
            }
        }
        if (loyaltyPointsTransaction[position].orderId!!.isNotEmpty()) {
            earnedStr.append(" " + mContext.getString(R.string.order) + ": #")
            earnedStr.append(loyaltyPointsTransaction[position].orderId)
        }
        else
            earnedStr.append(" " + loyaltyPointsTransaction[position].userName)

        holder.itemView.loyaltyPointEarnedFromTv.text = earnedStr

        holder.itemView.loyaltyPointEarnedDateTv.text = Utils.getInstance().convertTimeFormatAndTimeZone(loyaltyPointsTransaction[position].createdAt!!, "yyyy-MM-dd HH:mm:ss", "dd MMM yyyy, hh:mm aa")

    }


    override fun getItemCount(): Int {
        return loyaltyPointsTransaction.size
    }


    inner class LoyaltyPointTransactionHolder(itemView: View) : RecyclerView.ViewHolder(itemView)


}