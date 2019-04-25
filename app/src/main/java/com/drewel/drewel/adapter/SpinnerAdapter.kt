package com.drewel.drewel.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

import java.util.ArrayList


/**
 * Created by hemendrag on 5/07/2016.
 */
class SpinnerAdapter(private val activity: Context?, private val dataList: ArrayList<String>) : BaseAdapter() {

    /**
     * decalre variables of class
     */
    private var mContext: Context? = null
    private var convertView: View? = null
    private var mDataList: ArrayList<String>? = null


    /**
     * initialize variables
     */
    init {
        mContext = activity
        mDataList = dataList
    }


    override fun getView(position: Int, view: View, arg2: ViewGroup): View {


        /********************************************** INITIALIZATION  */
        convertView = view

        var mViewHolder: ViewHolder? = null

        var inflater = mContext!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater


        if (convertView == null) {
            mViewHolder = ViewHolder()
        } else {
            mViewHolder = convertView!!.tag as ViewHolder
        }

        try {
            if (position == 0) {
//                mViewHolder.mTextViewProviderTitle!!.setTextColor(ContextCompat.getColor(mContext!!, R.color.edtHintColor))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return convertView as View
    }


    override fun getCount(): Int {
        return dataList!!.size
    }

    override fun getItem(position: Int): Any {
        return dataList!![position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }


    /**
     * defining view holder
     */
    inner class ViewHolder {

        var mTextViewProviderTitle: TextView? = null

    }
}