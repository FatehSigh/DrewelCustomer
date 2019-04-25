package com.drewel.drewel.adapter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nostra13.universalimageloader.core.ImageLoader
import com.drewel.drewel.R
import com.drewel.drewel.activity.ProductActivity
import com.drewel.drewel.apicall.responsemodel.categoryresponsemodel.Category
import com.drewel.drewel.application.DrewelApplication
import com.drewel.drewel.constant.AppIntentExtraKeys
import com.drewel.drewel.constant.Constants
import kotlinx.android.synthetic.main.category_row_selector.view.*


/**
 * Created by sharukhb on 3/13/2018.
 */

class CategoryAdapter(val mContext: Context?, val categoryList: List<Category>, var img: String) : RecyclerView.Adapter<CategoryAdapter.CategoryHolder>() {


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
                ImageLoader.getInstance().displayImage(img, holder.itemView.categoryIv, DrewelApplication.getInstance().options)
            }
        } else {
            if (DrewelApplication.getInstance().getLanguage().equals(Constants.LANGUAGE_ENGLISH)) {
                holder.itemView.categoryNameTv.text = categoryList.get(position - 1).categoryName
            } else {
                holder.itemView.categoryNameTv.text = categoryList.get(position - 1).ar_category_name
            }

            var subCategoryName = ""
            val subCategory = categoryList[position - 1].subcategories

            if (subCategory != null) {
                for (i in 0 until subCategory.size) {
                    subCategoryName +=
                            if (i == (subCategory.size - 1)) {
                                if (DrewelApplication.getInstance().getLanguage().equals(Constants.LANGUAGE_ENGLISH)) {
                                    subCategory[i].categoryName
                                } else {
                                    subCategory[i].ar_category_name
                                }
                            } else
                                if (DrewelApplication.getInstance().getLanguage().equals(Constants.LANGUAGE_ENGLISH)) {
                                    subCategory[i].categoryName + ", "
                                } else {
                                    subCategory[i].ar_category_name + ", "
                                }


                }
                if (subCategoryName != "" && subCategoryName.split(",").size > 2) {
                    var str=subCategoryName.split(",")
                    holder.itemView.subCategoryNameTv.text = str[0].trim()+" , "+str[1].trim()+" etc."
                }
                else
                    holder.itemView.subCategoryNameTv.text = subCategoryName

            }

            ImageLoader.getInstance().displayImage(categoryList.get(position - 1).img, holder.itemView.categoryIv, DrewelApplication.getInstance().options)
        }

        holder.itemView.setOnClickListener { view ->
            val pos: Int = view.tag as Int
            if (pos == 0) {
                val intent = Intent(mContext, ProductActivity::class.java)
                mContext?.startActivity(intent)
            } else {
                val intent = Intent(mContext, ProductActivity::class.java)
                val bundle = Bundle()
                bundle.putSerializable(AppIntentExtraKeys.SELECTED_CATEGORY, categoryList[pos - 1])
                intent.putExtras(bundle)
                if (mContext != null) {
                    mContext.startActivity(intent)
                }
            }
        }
    }


    override fun getItemCount(): Int {
        Log.e("Size=", categoryList.size.toString())
        return categoryList.size + 1
    }


    class CategoryHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

}
