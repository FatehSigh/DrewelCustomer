@file:Suppress("Annotator", "Annotator", "Annotator", "Annotator", "Annotator", "Annotator", "Annotator", "Annotator", "Annotator", "Annotator", "Annotator", "Annotator", "Annotator", "Annotator")

package com.drewel.drewel.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.drewel.drewel.R
import com.drewel.drewel.model.ChatModel
import com.drewel.drewel.prefrences.Prefs
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.content_activity_otp_verification.*
import kotlinx.android.synthetic.main.item_msg.view.*
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by heena on 23/8/17.
 */
class ChatMessageAdapter(internal var context: Context, internal var orderArrayList: ArrayList<ChatModel>?) : RecyclerView.Adapter<ChatMessageAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_msg, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var chatModel = orderArrayList!![position]
        var pref = Prefs(context)
        val cal = Calendar.getInstance(Locale.ENGLISH)
        Log.e("time ",chatModel.messages!!.time!!)
        cal.timeInMillis = chatModel.messages!!.time!!.toLong()
        val date = DateFormat.format("dd MMM yyyy hh:mm a", cal).toString()
        if (chatModel.messages!!.sender_id == pref.getPreferenceStringData(pref.KEY_USER_ID)) {
            holder!!.itemView!!.rl_outgoing!!.visibility = View.VISIBLE
            holder.itemView.ll_incoming!!.visibility = View.GONE

            holder.itemView.txt_snd_message.text = chatModel.messages!!.message
            holder.itemView.txt_snd_message_time.text = date
        } else {
            holder!!.itemView!!.rl_outgoing!!.visibility = View.GONE
            holder.itemView.ll_incoming!!.visibility = View.VISIBLE
            holder.itemView.txt_rcv_message!!.text = chatModel.messages!!.message
            holder.itemView.txt_rcv_message_time!!.text = date
        }
    }


    override fun getItemCount(): Int {
        return orderArrayList!!.size
    }

/*    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {

    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_msg, parent, false))
    }*/

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    }




}