package com.drewel.drewel.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import com.blankj.utilcode.util.KeyboardUtils
import com.drewel.drewel.R
import com.drewel.drewel.apicall.DrewelApi
import com.drewel.drewel.apicall.responsemodel.productRequestresponsemodel.ProductRequest
import com.drewel.drewel.application.DrewelApplication
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.content_request_product_activity.*
import kotlinx.android.synthetic.main.request_product_activity.*

/**
 * Created by monikab on 3/23/2018.
 */
class RequestProductActivity : BaseActivity(), View.OnClickListener {
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.request_product_activity)
        initView()
    }

    var From = 0
    var couponList: ProductRequest? = null
    private fun initView() {
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        /* when user click on adapter row*/
        setClickListeners()
        if (intent != null) {
            From = intent.getIntExtra("From", 0)
            if (From == 1) {
                couponList = intent.getSerializableExtra("Data") as ProductRequest?
                et_product_title.setText(couponList!!.product_name)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {

                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setClickListeners() {
        txt_request.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.txt_request -> {

                if (et_product_title.text.toString().isBlank())
                    com.drewel.drewel.utill.Utils.getInstance().showToast(this, getString(R.string.product_name))
                else if (isNetworkAvailable()) {
                    if (From == 1) {
                        if (isNetworkAvailable())
                            calleditRequestProductApi()
                    } else
                        if (isNetworkAvailable())
                            callRequestProductApi()
                } else
                    com.drewel.drewel.utill.Utils.getInstance().showToast(this, getString(R.string.error_network_connection))
            }
        }
    }

    private fun callRequestProductApi() {
        progressBar.visibility = View.VISIBLE
        val addDeliveryAddressRequest = HashMap<String, String>()
        addDeliveryAddressRequest["user_id"] = pref!!.getPreferenceStringData(pref!!.KEY_USER_ID)
        addDeliveryAddressRequest["language"] = DrewelApplication.getInstance().getLanguage()
        addDeliveryAddressRequest["product_name"] = et_product_title.text.toString()
        val signUpObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).requestProduct(addDeliveryAddressRequest)
        compositeDisposable.add(signUpObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    progressBar.visibility = View.GONE
                    if (result.response!!.status!!) {
                        com.drewel.drewel.utill.Utils.getInstance().showToast(this, result.response!!.message!!)
                        val intent2 = Intent()
                        intent2.action = "UPDATE_ACCEPTED"
                        sendBroadcast(intent2)
                        finish()
                    } else {
                        com.drewel.drewel.utill.Utils.getInstance().showToast(this, result.response!!.message!!)
                    }
                }, { error ->
                    progressBar.visibility = View.GONE
                    com.drewel.drewel.utill.Utils.getInstance().showToast(this, error.message!!)
                    Log.e("TAG", "{$error.message}")
                }
                ))
    }

    private fun calleditRequestProductApi() {
        progressBar.visibility = View.VISIBLE
        val addDeliveryAddressRequest = HashMap<String, String>()
        addDeliveryAddressRequest["user_id"] = pref!!.getPreferenceStringData(pref!!.KEY_USER_ID)
        addDeliveryAddressRequest["language"] = DrewelApplication.getInstance().getLanguage()
        addDeliveryAddressRequest["product_name"] = et_product_title.text.toString()
        addDeliveryAddressRequest["request_id"] = couponList!!.request_id!!
        val signUpObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).edit_product_request(addDeliveryAddressRequest)
        compositeDisposable.add(signUpObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    progressBar.visibility = View.GONE
                    if (result.response!!.status!!) {
                        com.drewel.drewel.utill.Utils.getInstance().showToast(this, result.response!!.message!!)
                        couponList!!.product_name = et_product_title.text.toString()
                        val intent = Intent()
                        intent.putExtra("Data", couponList)
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    } else {
                        com.drewel.drewel.utill.Utils.getInstance().showToast(this, result.response!!.message!!)
                    }
                }, { error ->
                    progressBar.visibility = View.GONE
                    com.drewel.drewel.utill.Utils.getInstance().showToast(this, error.message!!)
                    Log.e("TAG", "{$error.message}")
                }
                ))
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }


    override fun onResume() {
        super.onResume()
        KeyboardUtils.hideSoftInput(this)
    }

}