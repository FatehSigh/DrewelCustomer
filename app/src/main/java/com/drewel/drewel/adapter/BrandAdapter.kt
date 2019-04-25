package com.drewel.drewel.adapter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.drewel.drewel.R
import com.drewel.drewel.activity.BrandWiseProductActivity
import com.drewel.drewel.apicall.responsemodel.productlistresponsemodel.Brand
import com.drewel.drewel.application.DrewelApplication
import com.drewel.drewel.constant.AppIntentExtraKeys
import com.drewel.drewel.constant.Constants
import com.drewel.drewel.utill.EqualSpacingItemDecoration
import kotlinx.android.synthetic.main.brand_row_selector.view.*


/**
 * Created by monikab on 3/13/2018.
 */

class BrandAdapter(val mContext: Context, private val brandList: List<Brand>, internal  var visibility: Int) : RecyclerView.Adapter<BrandAdapter.BrandHolder>() {
    var imageViewHeight = 0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrandHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.brand_row_selector, parent, false)
        return BrandHolder(view)
    }

    override fun onBindViewHolder(holder: BrandHolder, position: Int) {

//        val linearPram = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,imageViewHeight)
//        holder.itemView.cl_main.layoutParams = linearPram



        if (DrewelApplication.getInstance().getLanguage().equals(Constants.LANGUAGE_ENGLISH)) {
            holder.itemView.brandNameTv.text = brandList[position].brandName
        } else {
            holder.itemView.brandNameTv.text = brandList[position].ar_brand_name
        }

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

        val productAdapter = ProductAdapter(mContext,visibility)

       /* init method call itself when class call & set layout manger for recycler view*/
        init {
//           val displaymetrics = DisplayMetrics()
//           (mContext as AppCompatActivity).windowManager.defaultDisplay.getMetrics(displaymetrics)
//           val width = displaymetrics.heightPixels
//           imageViewHeight = (width / 2)
            val llm = LinearLayoutManager(mContext)
            llm.orientation = LinearLayoutManager.HORIZONTAL
            itemView.productRv.layoutManager = llm
            itemView.productRv.addItemDecoration(EqualSpacingItemDecoration(5, EqualSpacingItemDecoration.HORIZONTAL))
            itemView.productRv.adapter = productAdapter

        }

    }

}