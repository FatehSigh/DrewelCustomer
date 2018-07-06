package com.os.drewel.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.os.drewel.R
import com.os.drewel.apicall.responsemodel.deliveryaddressresponsemodel.Address
import com.os.drewel.utill.CommonUtil
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.delivery_address_item.view.*

/**
 * Created by sharukhb on 3/13/2018.
 */

class DeliveryListAdapter(val mContext: Context?, private val addressList: List<Address>) : RecyclerView.Adapter<DeliveryListAdapter.DeliveryListHolder>() {
    lateinit var defaultAddressClickSubject: PublishSubject<Int>
    lateinit var deleteAddressClickSubject: PublishSubject<Int>
    var defualtAddressPos = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeliveryListHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.delivery_address_item, parent, false)
        Log.e("addresList in adap==", addressList.toString())
        return DeliveryListHolder(view)
    }

    /*val APARTMENT = "1"
    val VILLA = "2"
    val OFFICE = "3"*/
    override fun onBindViewHolder(holder: DeliveryListHolder, position: Int) {
        holder.itemView.deliverAddressTitle.text = addressList[position].name
        holder.itemView.deliverAddressDesc.text = addressList[position].address
        holder.itemView.deliverAddressCheckBox.isChecked = addressList[position].isDefault.equals("1")
        if (!addressList[position].delivery_address_type.isNullOrEmpty()) {
            if (addressList[position].delivery_address_type!!.equals("1")) {
                holder.itemView.deliverAddressTypeName.visibility = View.VISIBLE
                holder.itemView.deliverAddressTypeName.setText(mContext!!.getString(R.string.apartment) + "-")
            } else if (addressList[position].delivery_address_type!!.equals("2")) {
                holder.itemView.deliverAddressTypeName.visibility = View.VISIBLE
                holder.itemView.deliverAddressTypeName.setText(mContext!!.getString(R.string.house) + "-")
            } else if (addressList[position].delivery_address_type!!.equals("3")) {
                holder.itemView.deliverAddressTypeName.visibility = View.VISIBLE
                holder.itemView.deliverAddressTypeName.setText(mContext!!.getString(R.string.office) + "-")
            } else
                holder.itemView.deliverAddressTypeName.visibility = View.GONE
        } else
            holder.itemView.deliverAddressTypeName.visibility = View.GONE

        if (addressList[position].isDefault.equals("1"))
            defualtAddressPos = position

        holder.itemView.setOnClickListener({
            defaultAddressClickSubject.onNext(holder.layoutPosition)
        })

        holder.itemView.deliverAddressCheckBox.setOnClickListener({
            defaultAddressClickSubject.onNext(holder.layoutPosition)
        })
        holder.itemView.deliverAddressDelete.setOnClickListener({
            deleteAddressClickSubject.onNext(holder.layoutPosition)
        })
    }


    override fun getItemCount(): Int {
        Log.e("Size of list=", addressList.size.toString())
        return addressList.size
    }

    class DeliveryListHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}