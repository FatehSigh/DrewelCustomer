package com.os.drewel.adapter

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton

import com.os.drewel.R
import kotlinx.android.synthetic.main.child_filter_brand_name.view.*
import java.util.ArrayList
import com.os.drewel.apicall.responsemodel.productlistresponsemodel.Brand

/**
 * Created by sharukhb on 3/13/2018.
 */

class BrandNameAdapter(private val brandNameList: List<Brand>) : RecyclerView.Adapter<BrandNameAdapter.BrandNameHolder>() {

    var selectedPosArray: ArrayList<Int> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrandNameHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.child_filter_brand_name, parent, false)
        return BrandNameHolder(view)
    }

    override fun onBindViewHolder(holder: BrandNameHolder, position: Int) {

        holder.itemView.brandNameCheckBox.text = brandNameList[position].brandName
        //  holder.itemView.brandProductsTv.text = brandNameList[position].products!!.size.toString()

        holder.itemView.brandNameCheckBox.isChecked = selectedPosArray.contains(position)

        holder.itemView.brandNameCheckBox.setOnCheckedChangeListener { compoundButton: CompoundButton, checked: Boolean ->
            Log.e("position==", holder.layoutPosition.toString())
            if (checked)
                selectedPosArray.add(holder.layoutPosition)
            else
                selectedPosArray.remove(holder.layoutPosition)
        }
    }

    override fun getItemCount(): Int {
        return brandNameList.size
    }

    inner class BrandNameHolder(itemView: View) : RecyclerView.ViewHolder(itemView)


}
