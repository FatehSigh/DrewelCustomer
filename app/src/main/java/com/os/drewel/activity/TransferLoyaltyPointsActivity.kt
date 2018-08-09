package com.os.drewel.activity

import android.app.Activity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.os.drewel.R
import com.os.drewel.apicall.DrewelApi
import com.os.drewel.application.DrewelApplication
import com.os.drewel.utill.ValidationUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.app_toolbar.*
import kotlinx.android.synthetic.main.layout_transfer_loyalty_point.*
import java.text.NumberFormat


class TransferLoyaltyPointsActivity : BaseActivity(), View.OnClickListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_transfer_loyalty_point)
        initView()
    }

    /* set toolbar and back button*/
    private fun initView() {
        toolbarTitleTv.text = getString(R.string.transfer_loyalty_point)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        transferLoyaltyPointBt.setOnClickListener(this)
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

    override fun onClick(v: View) {

        when(v.id){
            R.id.transferLoyaltyPointBt ->{

                if(validateLoyaltyPoint() && isNetworkAvailable()){

                    callTransferLoyaltyPointApi()

                }
            }
        }
    }


    private var disposable: Disposable?=null

    private fun callTransferLoyaltyPointApi() {
        setProgressState(View.VISIBLE)

        val transferLoyaltyPointRequest = HashMap<String, String>()

        transferLoyaltyPointRequest["language"] = DrewelApplication.getInstance().getLanguage()
        transferLoyaltyPointRequest["user_id"] = pref!!.getPreferenceStringData(pref!!.KEY_USER_ID)
        transferLoyaltyPointRequest["email"] = emailAddressEditText.text.toString().trim()
        transferLoyaltyPointRequest["loyalty_points"] =loyaltyPointEditText.text.toString()

        val transferLoyaltyPointObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).transferLoyaltyPoints(transferLoyaltyPointRequest)
        disposable = transferLoyaltyPointObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    setProgressState(View.GONE)
                    com.os.drewel.utill.Utils.getInstance().showToast(this,result.response!!.message!!)
                    if (result.response!!.status!!) {
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                }, { error ->
                    setProgressState(View.GONE)
                    com.os.drewel.utill.Utils.getInstance().showToast(this,error.message!!)
                    Log.e("TAG", "{$error.message}")
                }
                )
    }


    private fun setProgressState(visibility: Int) {
        progressBar.visibility = visibility
    }
    private fun validateLoyaltyPoint(): Boolean {

        if (TextUtils.isEmpty(emailAddressEditText.text.toString())) {
            emailAddressTextLayout.error = getString(R.string.enter_email_address)
            emailAddressTextLayout.requestFocus()
            return false
        }
        emailAddressTextLayout.isErrorEnabled = false

        if (!ValidationUtil.isEmailValid(emailAddressEditText.text.toString())) {
            emailAddressTextLayout.error = getString(R.string.enter_email_address)
            emailAddressTextLayout.requestFocus()
            return false
        }
        emailAddressTextLayout.isErrorEnabled = false

        if (TextUtils.isEmpty(loyaltyPointEditText.text.toString())) {
            loyaltyPointTextLayout.error = getString(R.string.enter_loyalty_point)
            loyaltyPointTextLayout.requestFocus()
            return false
        }
        loyaltyPointTextLayout.isErrorEnabled = false

        if (loyaltyPointEditText.text.toString().toFloat()<=0) {
            loyaltyPointTextLayout.error = getString(R.string.loyalty_point_more_then_zero)
            loyaltyPointTextLayout.requestFocus()
            return false
        }
        loyaltyPointTextLayout.isErrorEnabled = false


        return true
    }

    override fun onStop() {
        super.onStop()
        disposable?.dispose()
    }

}


