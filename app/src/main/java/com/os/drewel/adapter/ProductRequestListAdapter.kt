package com.os.drewel.adapter

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.os.drewel.R
import com.os.drewel.apicall.DrewelApi
import com.os.drewel.apicall.responsemodel.productRequestresponsemodel.ProductRequest
import com.os.drewel.application.DrewelApplication
import com.os.drewel.delegate.OnClickItem
import com.os.drewel.prefrences.Prefs
import com.os.drewel.utill.CommonUtil
import com.os.drewel.utill.Utils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.product_request_row.view.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class ProductRequestListAdapter(val mContext: Context?, private val couponList: List<ProductRequest>, private val onClickItem: OnClickItem) : RecyclerView.Adapter<ProductRequestListAdapter.DiscountHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiscountHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.product_request_row, parent, false)
        return DiscountHolder(view)
    }

    override fun onBindViewHolder(holder: DiscountHolder, position: Int) {
        val expireDate = Utils.getInstance().convertTimeFormat(couponList[position].requested_on!!, "yyyy-MM-dd", "dd MMM, yyyy")
        try {
            val startdate = SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(couponList[position].requested_on!!)
            holder.itemView.txt_date.setText(SimpleDateFormat("dd MMM ''yy").format(startdate))
            holder.itemView.txt_time.setText(SimpleDateFormat("h:mm a").format(startdate))
        } catch (e: Exception) {
        }
        if (holder.timer != null) {
            holder.timer!!.cancel()
        }
        if (couponList[position].stopTimer!!) {
            if (holder.timer != null) {
                holder.timer!!.cancel()
            }
        } else
            startTimer(couponList[position], holder)
        holder.itemView.txt_date.text = expireDate
        holder.itemView.txt_title.text = couponList[position].product_name
        holder.itemView.txt_reply.text = mContext!!.getString(R.string.reply) + couponList[position].reply

        holder.itemView.txt_edit.setOnClickListener {
            if (onClickItem != null)
                onClickItem.onClick("edit", position)
        }
        holder.itemView.txt_delete.setOnClickListener {
            val logoutAlertDialog = AlertDialog.Builder(mContext!!, R.style.DeliveryTypeTheme).create()
            logoutAlertDialog.setTitle(mContext!!.getString(R.string.app_name))
            logoutAlertDialog.setMessage(mContext!!.getString(R.string.want_to_delete_product))
            logoutAlertDialog.setButton(AlertDialog.BUTTON_POSITIVE, mContext!!.getString(R.string.yes), DialogInterface.OnClickListener { dialog, id ->
                logoutAlertDialog.dismiss()
                if (onClickItem != null)
                    onClickItem.onClick("Delete", position)
            })
            logoutAlertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, mContext!!.getString(R.string.no), DialogInterface.OnClickListener { dialog, id ->
                logoutAlertDialog.dismiss()
            })
            logoutAlertDialog.show()
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun startTimer(order: ProductRequest, holder: DiscountHolder) {
        val time = dateDifference(order.requested_on!!, order.server_time!!)
        var millisUntil: Long? = 0
        var min2 = TimeUnit.MINUTES.toMillis(10)
        var actualTime = min2 - TimeUnit.MILLISECONDS.toMillis(time)
        Log.e("time=", TimeUnit.MILLISECONDS.toMinutes(time).toString() + ", min2=" + min2 + " actualTime==" + TimeUnit.MILLISECONDS.toMinutes(actualTime) + " position=" + holder.adapterPosition)

        if (TimeUnit.MILLISECONDS.toMillis(time) < TimeUnit.MINUTES.toMillis(10)) {
            if (couponList[holder.adapterPosition].reply!!.isNotEmpty())
                return
            holder.itemView.txt_edit.visibility = View.VISIBLE
            holder.itemView.txt_delete.visibility = View.VISIBLE
            holder.timer = object : CountDownTimer(actualTime, 1000) {

                override fun onTick(millisUntilFinished: Long) {
                    val h = Handler(Looper.getMainLooper())
                    h.post(Runnable {
                        run {
                            millisUntil = millisUntilFinished
                            val secondsInMilli = 1000
                            val minutesInMilli = secondsInMilli * 60
                            val hoursInMilli = minutesInMilli * 60
                            val daysInMilli = hoursInMilli * 24
                            var different = millisUntilFinished
                            val elapsedDays = different / daysInMilli
                            different %= daysInMilli
                            val elapsedHours = different / hoursInMilli
                            different %= hoursInMilli

                            val elapsedMinutes = different / minutesInMilli
                            different %= minutesInMilli
                            val elapsedSeconds = different / secondsInMilli
                            Log.e("Time product",
                                    "%d days, %d hours, %d minutes, %d seconds%n" +
                                            elapsedDays + ", " + elapsedHours + ", " + elapsedMinutes + ", " + elapsedSeconds)
                            holder.itemView.txt_edit.visibility = View.VISIBLE
                            holder.itemView.txt_delete.visibility = View.VISIBLE
                        }
                    })
                }

                override fun onFinish() {
                    holder.itemView.txt_edit.visibility = View.GONE
                    holder.itemView.txt_delete.visibility = View.GONE
                }
            }.start()
        } else return

//        } else {
//            holder.itemView.txt_edit.visibility = View.GONE
//            holder.itemView.txt_delete.visibility = View.GONE
////            }
//        }
    }

    fun dateDifference(startDate: String, endDate: String): Long {
        Log.e("startDate==>", startDate + ", endDate==>" + endDate)
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        var difference: Long? = null
        try {
            val start: Date = simpleDateFormat.parse(startDate)
            val end: Date = simpleDateFormat.parse(endDate)
            difference = end.time - start.time
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return difference!!
    }

    override fun getItemCount(): Int {
        return couponList.size
    }


    var progressDialog: ProgressDialog? = null
    private fun callDeleteApi(position: Int, itemView: View) {
        itemView.txt_delete.isEnabled = false
        val pref = Prefs.getInstance(mContext)
        progressDialog = CommonUtil.showLoadingDialog(mContext!!)
        val reorderRequest = HashMap<String, String>()
        reorderRequest["user_id"] = pref.getPreferenceStringData(pref.KEY_USER_ID)
        reorderRequest["language"] = DrewelApplication.getInstance().getLanguage()
        reorderRequest["request_id"] = couponList[position].request_id!!

        val cancelOrderObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).delete_product_request(reorderRequest)

        val disposable = cancelOrderObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    DrewelApplication.getInstance().logoutWhenAccountDeactivated(result.response!!.isDeactivate!!, mContext)
                    itemView.txt_delete.isEnabled = true
                    progressDialog?.dismiss()
                    com.os.drewel.utill.Utils.getInstance().showToast(mContext,result.response!!.message!!)
                    if (result.response!!.status!!) {

                    }
                }, { error ->
                    itemView.txt_delete.isEnabled = true
                    progressDialog?.dismiss()
                    com.os.drewel.utill.Utils.getInstance().showToast(mContext,error.message!!)
                    Log.e("TAG", "{$error.message}")
                }
                )
    }

    inner class DiscountHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var timer: CountDownTimer? = null


    }

}
