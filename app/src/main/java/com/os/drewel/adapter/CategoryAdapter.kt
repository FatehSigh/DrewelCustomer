package com.os.drewel.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.assist.FailReason
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener
import com.os.drewel.R
import com.os.drewel.activity.ProductActivity
import com.os.drewel.apicall.responsemodel.categoryresponsemodel.Category
import com.os.drewel.application.DrewelApplication
import com.os.drewel.constant.AppIntentExtraKeys
import kotlinx.android.synthetic.main.category_row_selector.view.*
import kotlinx.android.synthetic.main.product_list_all_child.view.*


/**
 * Created by sharukhb on 3/13/2018.
 */

class CategoryAdapter(val mContext: Context?, val categoryList: List<Category>) : RecyclerView.Adapter<CategoryAdapter.CategoryHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.category_row_selector, parent, false)
        return CategoryHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryHolder, position: Int) {
        holder.itemView.tag = position
        if (position == 0) {
            if (mContext != null) {
                holder.itemView.categoryNameTv.text = mContext.getString(R.string.all_products)
                holder.itemView.subCategoryNameTv.text = mContext.getString(R.string.all_products_from_this_store)
            }
        } else {
            holder.itemView.categoryNameTv.text = categoryList.get(position - 1).categoryName
            var subCategoryName = ""
            val subCategory = categoryList[position - 1].subcategories

            if (subCategory != null) {
                for (i in 0 until subCategory.size) {
                    subCategoryName +=
                            if (i == (subCategory.size - 1))
                                subCategory[i].categoryName
                            else
                                subCategory[i].categoryName + " , "
                }
                holder.itemView.subCategoryNameTv.text = subCategoryName
            }


            ImageLoader.getInstance().displayImage(categoryList.get(position - 1).img, holder.itemView.categoryIv, DrewelApplication.getInstance().options)

        }

        holder.itemView.setOnClickListener(View.OnClickListener { view ->
            val pos: Int = view.tag as Int

            if (pos == 0) {
                val intent = Intent(mContext, ProductActivity::class.java)
                if (mContext != null) {
                    mContext.startActivity(intent)
                }
            } else {

                val intent = Intent(mContext, ProductActivity::class.java)
                val bundle = Bundle()
                bundle.putSerializable(AppIntentExtraKeys.SELECTED_CATEGORY, categoryList[pos - 1])
                intent.putExtras(bundle)
                if (mContext != null) {
                    mContext.startActivity(intent)
                }
            }

        })


    }


    override fun getItemCount(): Int {
        Log.e("Size=", categoryList.size.toString())
        return categoryList.size + 1;
    }


    class CategoryHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


    }

}
