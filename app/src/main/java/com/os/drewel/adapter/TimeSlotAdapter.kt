package com.os.drewel.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton

import com.os.drewel.R
import com.os.drewel.delegate.OnClick
import com.os.drewel.model.TimeSlotModel
import kotlinx.android.synthetic.main.timeslot_child.view.*
import java.util.ArrayList


/**
 * Created by sharukhb on 3/13/2018.
 */

class TimeSlotAdapter(private val timeSlotList: MutableList<TimeSlotModel>, val onClick: OnClick) : RecyclerView.Adapter<TimeSlotAdapter.BrandNameHolder>() {

    var selectedPosArray: ArrayList<Int> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrandNameHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.timeslot_child, parent, false)
        return BrandNameHolder(view)
    }

    override fun onBindViewHolder(holder: BrandNameHolder, position: Int) {

        holder.itemView.txt_timeslot.text = timeSlotList[position].slot
        holder.itemView.txt_fare.text = timeSlotList[position].fare
        //  holder.itemView.brandProductsTv.text = brandNameList[position].products!!.size.toString()

        holder.itemView.txt_timeslot.isSelected = timeSlotList[position].isCheck!!
        holder.itemView.rl_time.isSelected = timeSlotList[position].isCheck!!
        holder.itemView.txt_fare.isSelected = timeSlotList[position].isCheck!!

        holder.itemView.txt_timeslot.setOnClickListener {
            onClick.onClick(position)
        }

    }

    override fun getItemCount(): Int {
        return timeSlotList.size
    }

    inner class BrandNameHolder(itemView: View) : RecyclerView.ViewHolder(itemView)


}
