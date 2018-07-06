package com.os.drewel.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton

import com.os.drewel.R
import com.os.drewel.apicall.responsemodel.brandnameresponsemodel.Brand
import kotlinx.android.synthetic.main.child_filter_brand_name.view.*
import kotlinx.android.synthetic.main.search_suggestion_item.view.*
import java.util.ArrayList
import android.content.Context
import android.content.Intent
import com.os.drewel.activity.SearchWiseProductActivity
import com.os.drewel.constant.AppIntentExtraKeys

/**
 * Created by sharukhb on 3/13/2018.
 */

class SearchSuggestionAdapter(var context:Context,var searchSuggestionList: List<String>) : RecyclerView.Adapter<SearchSuggestionAdapter.BrandNameHolder>() {

    var selectedPosArray: ArrayList<Int> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrandNameHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.search_suggestion_item, parent, false)

        return BrandNameHolder(view)
    }

    override fun onBindViewHolder(holder: BrandNameHolder, position: Int) {

        holder.itemView.searchSuggestionText.text = searchSuggestionList[position]

      //  holder.itemView.brandProductsTv.text = brandNameList[position].products!!.size.toString()


    }

    override fun getItemCount(): Int {
        return searchSuggestionList.size
    }

    inner class BrandNameHolder(itemView: View) : RecyclerView.ViewHolder(itemView) , View.OnClickListener{
        init {
            itemView.setOnClickListener(this)
        }


        override fun onClick(view: View) {
            val intent =Intent(context,SearchWiseProductActivity::class.java)
            intent.putExtra(AppIntentExtraKeys.SEARCH_KEY,searchSuggestionList[adapterPosition] )
            context.startActivity(intent)
        }

    }
}
