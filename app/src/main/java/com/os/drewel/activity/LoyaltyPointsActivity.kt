package com.os.drewel.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.os.drewel.R
import com.os.drewel.adapter.LoyaltyPointTransactionAdapter
import com.os.drewel.apicall.DrewelApi
import com.os.drewel.apicall.responsemodel.loyaltypointresponsemodel.LoyaltyPoint
import com.os.drewel.application.DrewelApplication
import com.os.drewel.constant.AppRequestCodes
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

    private var loyaltyPointsTransaction: List<LoyaltyPoint> = ArrayList()

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
    }


    override fun onClick(v: View) {
        when (v.id) {

            R.id.loyaltyPointHintTv -> {
                val intent = Intent(this, TransferLoyaltyPointsActivity::class.java)
                startActivityForResult(intent, AppRequestCodes.TRANSFER_LOYALTY_POINT_CODE)
            }

            R.id.loyaltyPointTv -> {
                val intent = Intent(this, TransferLoyaltyPointsActivity::class.java)
                startActivityForResult(intent, AppRequestCodes.TRANSFER_LOYALTY_POINT_CODE)
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
                        loyaltyPointsTransaction = result.response!!.data!!.loyaltyPoints!!
                        loyaltyPointTv.text = NumberFormat.getInstance().format(result.response!!.data!!.currentLoyaltyPoints!!.toDouble())+" "+ getString(R.string.omr)
                        setAdapter()
                    } else {
                        Utils.getInstance().showToast(this,result.response!!.message!!)
                        noTransactionAlertTv.visibility = View.VISIBLE
                    }
                }, { error ->
                    setProgressState(View.GONE)
                    Utils.getInstance().showToast(this,error.message!!)
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


