package com.drewel.drewel.adapter

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.nostra13.universalimageloader.core.ImageLoader

import com.drewel.drewel.R
import com.drewel.drewel.activity.ProductDetailActivity
import com.drewel.drewel.apicall.responsemodel.Product
import com.drewel.drewel.application.DrewelApplication
import com.drewel.drewel.constant.AppIntentExtraKeys
import com.drewel.drewel.constant.Constants
import kotlinx.android.synthetic.main.similar_product_child.view.*


/**
 * Created by sharukhb on 3/13/2018.
 */

class SimilarProductAdapter(val mContext: Context?) : RecyclerView.Adapter<SimilarProductAdapter.SimilarProductHolder>() {

    var productList: List<Product> = ArrayList()
    var imageViewHeight = 0


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimilarProductHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.similar_product_child, parent, false)
        return SimilarProductHolder(view)
    }

    override fun onBindViewHolder(holder: SimilarProductHolder, position: Int) {

        val linearPram = LinearLayout.LayoutParams(imageViewHeight, LinearLayout.LayoutParams.WRAP_CONTENT)
        holder.itemView.productListRootLL.layoutParams = linearPram

        val cardViewPram = LinearLayout.LayoutParams(imageViewHeight, imageViewHeight)
        holder.itemView.card_view.layoutParams = cardViewPram

        ImageLoader.getInstance().displayImage(productList[position].productImage, holder.itemView.imv_product, DrewelApplication.getInstance().options)
        if (DrewelApplication.getInstance().getLanguage().equals(Constants.LANGUAGE_ENGLISH)){
            holder.itemView.productTitle.text = productList[position].productName
        }else
        {
            holder.itemView.productTitle.text = productList[position].ar_product_name
        }

        if (mContext != null) {
            val amount =String.format("%.3f", productList[position].avgPrice!!.toDouble()) + " " + mContext.getString(R.string.omr)
            holder.itemView.tv_product_amount.text = amount
        }


        holder.itemView.setOnClickListener(View.OnClickListener { view ->

            val intent = Intent(mContext, ProductDetailActivity::class.java)
            intent.putExtra(AppIntentExtraKeys.PRODUCT_ID, productList[holder.adapterPosition].productId)
            mContext?.startActivity(intent)
        })
    }


    override fun getItemCount(): Int {
        return productList.size;
    }


    fun productList(data: List<Product>) {
        if (productList !== data) {
            productList = data
            notifyDataSetChanged()
        }
    }


    inner class SimilarProductHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        init {
            val displaymetrics = DisplayMetrics()
            (mContext as AppCompatActivity).windowManager.defaultDisplay.getMetrics(displaymetrics)
            val width = displaymetrics.widthPixels
            imageViewHeight = (width / 2) - 32
        }

    }

}