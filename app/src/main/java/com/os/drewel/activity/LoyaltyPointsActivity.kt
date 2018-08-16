package com.os.drewel.activity

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.os.drewel.R
import com.os.drewel.adapter.LoyaltyPointTransactionAdapter
import com.os.drewel.adapter.NotificationAdapter
import com.os.drewel.apicall.DrewelApi
import com.os.drewel.apicall.responsemodel.loyaltypointresponsemodel.LoyaltyPoint
import com.os.drewel.application.DrewelApplication
import com.os.drewel.constant.AppRequestCodes
import com.os.drewel.prefrences.Prefs
import com.os.drewel.utill.SwipeHelper
import com.os.drewel.utill.Utils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.app_toolbar.*
import kotlinx.android.synthetic.main.layout_loyalty_points_list.*
import java.text.NumberFormat


class LoyaltyPointsActivity : BaseActivity(), View.OnClickListener {

    private var disposable: Disposable? = null
    private var loyaltyPointTransactionAdapter: LoyaltyPointTransactionAdapter? = null

    private var loyaltyPointsTransaction: MutableList<LoyaltyPoint> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_loyalty_points_list)
        initView()
        callLoyaltyPointTransactionApi()
    }

    /* set toolbar and back button*/
    private fun initView() {
        toolbarTitleTv.text = getString(R.string.loyalty_point)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        loyaltyPointHintTv.setOnClickListener(this)
        loyaltyPointTv.setOnClickListener(this)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
        /* finish activity if user tap on action bar back button*/
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun setAdapter() {

        if (loyaltyPointTransactionAdapter == null) {
            loyaltyPointTransactionRv.layoutManager = LinearLayoutManager(this)
            loyaltyPointTransactionAdapter = LoyaltyPointTransactionAdapter(this, loyaltyPointsTransaction)
            loyaltyPointTransactionRv.adapter = loyaltyPointTransactionAdapter
        } else {
            loyaltyPointTransactionAdapter?.loyaltyPointsTransaction = loyaltyPointsTransaction
            loyaltyPointTransactionAdapter?.notifyDataSetChanged()
        }


        val swipeHelper = object : SwipeHelper(this, loyaltyPointTransactionRv) {
            override fun instantiateUnderlayButton(viewHolder: RecyclerView.ViewHolder, underlayButtons: MutableList<SwipeHelper.UnderlayButton>) {
                underlayButtons.add(SwipeHelper.UnderlayButton(
                        getString(R.string.delete),
                        0,
                        Color.parseColor("#eb011c"),
                        UnderlayButtonClickListener {
                            //                            Log.e("Position on delete", it.toString())
                            showLogoutDialog(getString(R.string.delete_loyaltypoint), it, false)
                        }
                ))

            }
        }

    }

    /* show logout confirmation popup to user*/
    private fun showLogoutDialog(message: String, position: Int, clearAll: Boolean) {
        val logoutAlertDialog = AlertDialog.Builder(this, R.style.DeliveryTypeTheme).create()
        logoutAlertDialog.setTitle(getString(R.string.app_name))
        logoutAlertDialog.setMessage(message)
        logoutAlertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.yes), DialogInterface.OnClickListener { dialog, id ->
            logoutAlertDialog.dismiss()
            callDeleteNotificationApi(position, clearAll)
        })
        logoutAlertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.no), DialogInterface.OnClickListener { dialog, id ->
            logoutAlertDialog.dismiss()
            loyaltyPointTransactionAdapter!!.notifyDataSetChanged()
        })
        logoutAlertDialog.show()
    }

    private fun callDeleteNotificationApi(position: Int, clearAll: Boolean) {
        setProgressState(View.VISIBLE)
//        setProgressState(View.VISIBLE, View.GONE)
        val readNotificationRequest = java.util.HashMap<String, String>()
        readNotificationRequest["user_id"] = pref!!.getPreferenceStringData(pref!!.KEY_USER_ID)
        readNotificationRequest["language"] = DrewelApplication.getInstance().getLanguage()
        if (!clearAll)
            readNotificationRequest["wallet_id"] = loyaltyPointsTransaction[position].orderId!!
        val cancelOrderObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).clear_loyalty(readNotificationRequest)
        cancelOrderObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    setProgressState(View.GONE)
                    DrewelApplication.getInstance().logoutWhenAccountDeactivated(result.response!!.isDeactivate!!, this)
//                    if (FROM!=1){
                    if (clearAll) {
                        loyaltyPointsTransaction = ArrayList()
                        loyaltyPointTransactionRv.layoutManager = LinearLayoutManager(this)
                        loyaltyPointTransactionAdapter = LoyaltyPointTransactionAdapter(this, loyaltyPointsTransaction)
                        loyaltyPointTransactionRv.adapter = loyaltyPointTransactionAdapter
//                        unread = 0
//                        txt_clearall.visibility = View.GONE
                    } else {
                        loyaltyPointsTransaction.removeAt(position)
                        loyaltyPointTransactionAdapter!!.notifyDataSetChanged()
//                        unread = Prefs.getInstance(this).getPreferenceIntData(Prefs.getInstance(this).UNREAD_COUNT)
//                        unread -= 1
//                        try {
//                            if (notificationList.isEmpty())
//                                txt_clearall.visibility = View.GONE
//                            else
//                                txt_clearall.visibility = View.VISIBLE
//                        } catch (e: Exception) {
//
//                        }
                    }

                }, { error ->
                    setProgressState(View.GONE)
                    Log.e("TAG", "{$error.message}")
                })
    }

    override fun onClick(v: View) {
        when (v.id) {

            R.id.loyaltyPointHintTv -> {
//                val intent = Intent(this, TransferLoyaltyPointsActivity::class.java)
//                startActivityForResult(intent, AppRequestCodes.TRANSFER_LOYALTY_POINT_CODE)
            }

            R.id.loyaltyPointTv -> {
//                val intent = Intent(this, TransferLoyaltyPointsActivity::class.java)
//                startActivityForResult(intent, AppRequestCodes.TRANSFER_LOYALTY_POINT_CODE)
            }
        }
    }


    private fun callLoyaltyPointTransactionApi() {
        setProgressState(View.VISIBLE)

        val getLoyaltyPointTransactionRequest = HashMap<String, String>()

        getLoyaltyPointTransactionRequest["language"] = DrewelApplication.getInstance().getLanguage()
        getLoyaltyPointTransactionRequest["user_id"] = pref!!.getPreferenceStringData(pref!!.KEY_USER_ID)


        val loyaltyPointObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).getLoyaltyPointsTransaction(getLoyaltyPointTransactionRequest)
        disposable = loyaltyPointObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    setProgressState(View.GONE)

                    if (result.response!!.status!!) {
                        if (result.response!!.data!!.currentLoyaltyPoints!=null)
                        loyaltyPointTv.text = NumberFormat.getInstance().format(result.response!!.data!!.currentLoyaltyPoints!!.toDouble()) + " " + getString(R.string.omr)
                        else
                            loyaltyPointTv.text =   NumberFormat.getInstance().format(0.000)+" "+ getString(R.string.omr)
                        if (result.response!!.data!!.loyaltyPoints!=null) {

                            loyaltyPointsTransaction = (result.response!!.data!!.loyaltyPoints as MutableList<LoyaltyPoint>?)!!

                            setAdapter()
                        }

                    } else {
                        Utils.getInstance().showToast(this, result.response!!.message!!)
                        noTransactionAlertTv.visibility = View.VISIBLE
                    }
                }, { error ->
                    setProgressState(View.GONE)
                    Utils.getInstance().showToast(this, error.message!!)
                    Log.e("TAG", "{$error.message}")
                }
                )
    }

    private fun setProgressState(visibility: Int) {
        progressBar.visibility = visibility
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AppRequestCodes.TRANSFER_LOYALTY_POINT_CODE && resultCode == Activity.RESULT_OK) {
            callLoyaltyPointTransactionApi()
        }
    }
}


