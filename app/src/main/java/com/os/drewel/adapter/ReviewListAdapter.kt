package com.os.drewel.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton

import com.os.drewel.R
import com.os.drewel.apicall.responsemodel.reviewResponseModel.Review
import kotlinx.android.synthetic.main.review_item.view.*
import java.util.ArrayList

/**
 * Created by sharukhb on 3/13/2018.
 */

class ReviewListAdapter(private val brandNameList: List<Review>) : RecyclerView.Adapter<ReviewListAdapter.BrandNameHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrandNameHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.review_item, parent, false)
        return BrandNameHolder(view)
    }

    override fun onBindViewHolder(holder: BrandNameHolder, position: Int) {
//        holder.itemView.brandNameCheckBox.text = brandNameList[position].brandName
        //  holder.itemView.brandProductsTv.text = brandNameList[position].products!!.size.toString()
        holder.itemView.tv_rating.text = brandNameList[position].ratings ?: "0"
        holder.itemView.ratingBar.rating = brandNameList[position].ratings?.toFloat() ?: 0.0F
        holder.itemView.txt_name.text = brandNameList[position].user_name
        holder.itemView.txt_review.text = brandNameList[position].reviews
    }

    override fun getItemCount(): Int {
        return brandNameList.size
    }

    inner class BrandNameHolder(itemView: View) : RecyclerView.ViewHolder(itemView)


}
