package com.os.drewel.adapter

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nostra13.universalimageloader.core.ImageLoader
import com.os.drewel.R
import com.os.drewel.activity.ProductDetailActivity
import com.os.drewel.apicall.responsemodel.myorderdetailresponsemodel.Product
import com.os.drewel.application.DrewelApplication
import com.os.drewel.constant.AppIntentExtraKeys
import com.os.drewel.constant.Constants
import kotlinx.android.synthetic.main.my_order_product_item.view.*
import java.text.NumberFormat

/**
 * Created by sharukhb on 3/13/2018.
 */

class MyOrderProductItemAdapter(val mContext: Context, private val myOrderIemList: List<Product>) : RecyclerView.Adapter<MyOrderProductItemAdapter.MyOrderItemHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyOrderItemHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.my_order_product_item, parent, false)

        return MyOrderItemHolder(view)
    }

    override fun onBindViewHolder(holder: MyOrderItemHolder, position: Int) {
        ImageLoader.getInstance().displayImage(myOrderIemList[position].productImage, holder.itemView.productImageIv, DrewelApplication.getInstance().options)
        if (DrewelApplication.getInstance().getLanguage().equals(Constants.LANGUAGE_ENGLISH)) {
            holder.itemView.tv_product_title.text = myOrderIemList[position].productName
        } else
            holder.itemView.tv_product_title.text = myOrderIemList[position].ar_product_name

        holder.itemView.tv_product_quantity.text = mContext.getString(R.string.quantity_colon) + " " + myOrderIemList[position].quantity

        /*  val cartItem = myOrderIemList[position]
          val price = cartItem.productPrice!!.toDouble()
          val quantity = cartItem.quantity!!.toInt()
          val totalAmount = price * quantity*/

        holder.itemView.tv_product_amount.text = mContext.getString(R.string.price_colon) + " " + String.format("%.3f", myOrderIemList[position].productPrice!!.toDouble()) + " " + mContext.getString(R.string.omr)

        holder.itemView.tv_product_categories.text = showProductCategory(myOrderIemList[position])

        val subcategory = showProductSubcategory(myOrderIemList[position])
        holder.itemView.tv_product_sub_categories.text = subcategory
        if (subcategory.isBlank())
            holder.itemView.tv_product_sub_categories.visibility = View.GONE
        else
            holder.itemView.tv_product_sub_categories.visibility = View.VISIBLE
        holder.itemView.tv_product_sub_categories.text = "> "+subcategory
    }

    override fun getItemCount(): Int {
        return myOrderIemList.size
    }


    inner class MyOrderItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        init {
            itemView.setOnClickListener(this)
        }


        override fun onClick(v: View) {
            val intent = Intent(mContext, ProductDetailActivity::class.java)
            intent.putExtra(AppIntentExtraKeys.PRODUCT_ID, myOrderIemList[adapterPosition].productId)
            mContext.startActivity(intent)
        }

    }

    private fun showProductCategory(product: Product): String {

        var category = ""

        for (i in product.category!!.indices) {
            if (product.category!!.isNotEmpty())
                category += if (i == product.category!!.size - 1) {
                    if (DrewelApplication.getInstance().getLanguage().equals(Constants.LANGUAGE_ENGLISH)) {
                        product.category!![i].categoryName!!
                    } else
                        product.category!![i].ar_category_name!!
                } else
                    if (DrewelApplication.getInstance().getLanguage().equals(Constants.LANGUAGE_ENGLISH)) {
                        product.category!![i].categoryName!! + ", "
                    } else
                        product.category!![i].ar_category_name!! + ", "
        }
Log.e("Category=",category)
        return category
    }

    private fun showProductSubcategory(product: Product): String {

        var subCategory = ""

        if (product.subCategory?.isEmpty() ?: true) {

        } else {
            for (i in product.subCategory!!.indices) {
                if (product.subCategory!!.isNotEmpty())
                    subCategory += if (i == product.subCategory!!.size - 1) {
                        if (DrewelApplication.getInstance().getLanguage().equals(Constants.LANGUAGE_ENGLISH)) {
                            product.subCategory!![i].categoryName!!
                        } else {
                            product.subCategory!![i].ar_category_name!!
//                        product.category!![i].ar_category_name!!+ ", "
                        }
                    } else
                        if (DrewelApplication.getInstance().getLanguage().equals(Constants.LANGUAGE_ENGLISH)) {
                            product.subCategory!![i].categoryName!! + ", "
                        } else
                            product.subCategory!![i].ar_category_name!! + ", "
            }
        }
        Log.e("subCategory=",subCategory)
        return subCategory
    }

}


