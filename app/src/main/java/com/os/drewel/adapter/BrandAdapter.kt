package com.os.drewel.adapter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.os.drewel.R
import com.os.drewel.activity.BrandWiseProductActivity
import com.os.drewel.apicall.responsemodel.productlistresponsemodel.Brand
import com.os.drewel.constant.AppIntentExtraKeys
import com.os.drewel.utill.EqualSpacingItemDecoration
import com.os.drewel.utill.Utils
import kotlinx.android.synthetic.main.brand_row_selector.view.*


/**
 * Created by monikab on 3/13/2018.
 */

class BrandAdapter(val mContext: Context, private val brandList: List<Brand>) : RecyclerView.Adapter<BrandAdapter.BrandHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrandHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.brand_row_selector, parent, false)

        return BrandHolder(view)
    }

    override fun onBindViewHolder(holder: BrandHolder, position: Int) {
        holder.itemView.brandNameTv.text = brandList[position].brandName
        holder.itemView.productNumberTv.text = brandList[position].products!!.size.toString()
        holder.productAdapter.productList(brandList[position].products!!)
        holder.productAdapter.notifyDataSetChanged()
        holder.itemView.productNumberTv.tag = position
        holder.itemView.productNumberTv.setOnClickListener() { view ->
            val pos = view.tag as Int
            val intent = Intent(mContext, BrandWiseProductActivity::class.java)
            val bundle = Bundle()
            bundle.putSerializable(AppIntentExtraKeys.BRAND_WISE_PRODUCT, brandList[pos])
            intent.putExtras(bundle)
            mContext.startActivity(intent)

        }
    }

    override fun getItemCount(): Int {
        return brandList.size;
    }


    inner class BrandHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val productAdapter = ProductAdapter(mContext)

       /* init method call itself when class call & set layout manger for recycler view*/
        init {
            val llm = LinearLayoutManager(mContext)
            llm.orientation = LinearLayoutManager.HORIZONTAL
            itemView.productRv.layoutManager = llm
            itemView.productRv.addItemDecoration(EqualSpacingItemDecoration(16, EqualSpacingItemDecoration.HORIZONTAL))
            itemView.productRv.adapter = productAdapter


        }

    }

}