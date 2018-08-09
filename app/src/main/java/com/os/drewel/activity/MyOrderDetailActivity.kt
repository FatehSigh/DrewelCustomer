package com.os.drewel.activity

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.widget.Toast
import com.os.drewel.R
import com.os.drewel.adapter.MyOrderProductItemAdapter
import com.os.drewel.apicall.DrewelApi
import com.os.drewel.apicall.responsemodel.myorderdetailresponsemodel.Data
import com.os.drewel.apicall.responsemodel.myorderdetailresponsemodel.Product
import com.os.drewel.application.DrewelApplication
import com.os.drewel.constant.AppIntentExtraKeys
import com.os.drewel.rxbus.CartRxJavaBus
import com.os.drewel.utill.Utils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.content_my_order_detail.*
import kotlinx.android.synthetic.main.my_order_detail.*
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList

class MyOrderDetailActivity : BaseActivity(), View.OnClickListener {

    private var orderId = ""
    private var notificationId = ""

    private var myOrderDetailResponse: Data? = null

    private var myOrderDetailDisposable: CompositeDisposable = CompositeDisposable()

    private var myOrderProductList: List<Product> = ArrayList()
    var FROM = 0
    var is_read = ""
    var isCalled = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.my_order_detail)
        initView()
    }

    private fun initView() {
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        orderId = intent.getStringExtra(AppIntentExtraKeys.ORDER_ID)

        if (intent.getIntExtra(AppIntentExtraKeys.FROM, 0) == 1) {
            FROM = 1
            notificationId = intent.getStringExtra(AppIntentExtraKeys.NOTIFICATION_ID)
            if (isNetworkAvailable())
                callReadNotificationApi()
        } else if (intent.getIntExtra(AppIntentExtraKeys.FROM, 0) == 2) {
            FROM = 2
            notificationId = intent.getStringExtra(AppIntentExtraKeys.NOTIFICATION_ID)
            is_read = intent.getStringExtra(AppIntentExtraKeys.IS_READ)
            if (is_read.equals("0"))
                if (isNetworkAvailable())
                    callReadNotificationApi()
        }
        setClickListener()
        if (isNetworkAvailable())
            callGetMyOrderDetailApi()
    }

    private fun callReadNotificationApi() {
//        setProgressState(View.VISIBLE, View.GONE)
        val readNotificationRequest = HashMap<String, String>()
        readNotificationRequest["user_id"] = pref!!.getPreferenceStringData(pref!!.KEY_USER_ID)
        readNotificationRequest["language"] = DrewelApplication.getInstance().getLanguage()
        readNotificationRequest["notification_id"] = notificationId
        val cancelOrderObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).readNotification(readNotificationRequest)
        val disposable = cancelOrderObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    DrewelApplication.getInstance().logoutWhenAccountDeactivated(result.response!!.isDeactivate!!, this)
                    isCalled = true
                    is_read = "1"
                }, { error ->
                    Log.e("TAG", "{$error.message}")
                }
                )
        myOrderDetailDisposable.add(disposable)
    }

    private fun setClickListener() {
        order_detail_btn_cancel.setOnClickListener(this)
        reorderTv.setOnClickListener(this)
        order_detail_btn_track_order.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.order_detail_btn_cancel -> {
                if (isNetworkAvailable())
                    callCancelOrderApi()
            }
            R.id.reorderTv -> {
                if (isNetworkAvailable()) {
                    val logoutAlertDialog = AlertDialog.Builder(this, R.style.DeliveryTypeTheme).create()
                    logoutAlertDialog.setTitle(this!!.getString(R.string.app_name))
                    logoutAlertDialog.setMessage(this!!.getString(R.string.want_to_reorder))
                    logoutAlertDialog.setButton(AlertDialog.BUTTON_POSITIVE, this!!.getString(R.string.yes), DialogInterface.OnClickListener { dialog, id ->
                        logoutAlertDialog.dismiss()
                        callReorderApi()
                    })

                    logoutAlertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, this!!.getString(R.string.no), DialogInterface.OnClickListener { dialog, id ->
                        logoutAlertDialog.dismiss()
                    })
                    logoutAlertDialog.show()
                }

            }

            R.id.order_detail_btn_track_order -> {
                val intent = Intent(this, TrackOrderActivity::class.java)
                intent.putExtra(AppIntentExtraKeys.DELIVERY_ADDRESS_LATITUDE, myOrderDetailResponse?.order?.deliveryLatitude
                        ?: "")
                intent.putExtra(AppIntentExtraKeys.DELIVERY_ADDRESS_LONGITUDE, myOrderDetailResponse?.order?.deliveryLongitude
                        ?: "")
                intent.putExtra(AppIntentExtraKeys.DELIVERY_BOY_ID, myOrderDetailResponse?.deliveryBoy?.id
                        ?: "")

                startActivity(intent)
            }

            R.id.order_detail_txt_order_call_captain -> {
                try {
                    val intent = Intent(Intent.ACTION_DIAL)
                    intent.data = Uri.parse("tel:" + myOrderDetailResponse?.deliveryBoy?.mobileNumber)
                    startActivity(intent)
                } catch (e: Exception) {
                }
            }
        }
    }

    private fun callCancelOrderApi() {
        order_detail_btn_cancel.isEnabled = false
        setProgressState(View.VISIBLE, View.VISIBLE)
        val cancelOrderRequest = HashMap<String, String>()
        cancelOrderRequest["user_id"] = pref!!.getPreferenceStringData(pref!!.KEY_USER_ID)
        cancelOrderRequest["language"] = DrewelApplication.getInstance().getLanguage()
        cancelOrderRequest["order_id"] = orderId

        val cancelOrderObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).cancelOrder(cancelOrderRequest)

        val disposable = cancelOrderObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->

                    DrewelApplication.getInstance().logoutWhenAccountDeactivated(result.response!!.isDeactivate!!, this)
                    order_detail_btn_cancel.isEnabled = true
                    setProgressState(View.GONE, View.VISIBLE)
                    Utils.getInstance().showToast(this, result.response!!.message!!)
                    if (result.response!!.status!!) {
                        order_detail_btn_cancel.visibility = View.GONE
                    }
                }, { error ->
                    order_detail_btn_cancel.isEnabled = true
                    setProgressState(View.GONE, View.VISIBLE)
                    Utils.getInstance().showToast(this, error.message!!)
                    Log.e("TAG", "{$error.message}")
                }
                )
        myOrderDetailDisposable.add(disposable)

    }


    private fun callReorderApi() {
        reorderTv.isEnabled = false
        setProgressState(View.VISIBLE, View.VISIBLE)

        val reorderRequest = HashMap<String, String>()
        reorderRequest["user_id"] = pref!!.getPreferenceStringData(pref!!.KEY_USER_ID)
        reorderRequest["language"] = DrewelApplication.getInstance().getLanguage()
        reorderRequest["order_id"] = orderId

        val cancelOrderObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).reorder(reorderRequest)

        val disposable = cancelOrderObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->

                    DrewelApplication.getInstance().logoutWhenAccountDeactivated(result.response!!.isDeactivate!!, this)
                    reorderTv.isEnabled = true
                    setProgressState(View.GONE, View.VISIBLE)
                    Utils.getInstance().showToast(this, result.response!!.message!!)

                    if (result.response!!.status!!) {
                        pref!!.setPreferenceStringData(pref!!.KEY_CART_ID, result.response!!.data!!.cart!!.cartId!!)
                        CartRxJavaBus.getInstance().cartPublishSubject.onNext(result.response!!.data!!.cart!!.quantity!!)
                    }

                }, { error ->
                    reorderTv.isEnabled = true
                    setProgressState(View.GONE, View.VISIBLE)
                    Utils.getInstance().showToast(this, error.message!!)
                    Log.e("TAG", "{$error.message}")
                }
                )
        myOrderDetailDisposable.add(disposable)
    }


    private fun callGetMyOrderDetailApi() {
        setProgressState(View.VISIBLE, View.GONE)
        val getMyOrderDetailRequest = HashMap<String, String>()
        getMyOrderDetailRequest["user_id"] = pref!!.getPreferenceStringData(pref!!.KEY_USER_ID)
        getMyOrderDetailRequest["language"] = DrewelApplication.getInstance().getLanguage()
        getMyOrderDetailRequest["order_id"] = orderId

        val getProductObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).getMyOrderDetail(getMyOrderDetailRequest)

        val disposable = getProductObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->

                    DrewelApplication.getInstance().logoutWhenAccountDeactivated(result.response!!.isDeactivate!!, this)

                    if (result.response!!.status!!) {
                        setProgressState(View.GONE, View.VISIBLE)
                        myOrderDetailResponse = result.response!!.data
                        setOrderDetailData()
                    } else {
                        setProgressState(View.GONE, View.GONE)
                    }
                }, { error ->
                    setProgressState(View.GONE, View.GONE)
                    Utils.getInstance().showToast(this, error.message!!)
                    Log.e("TAG", "{$error.message}")
                }
                )
        myOrderDetailDisposable.add(disposable)
    }

    private fun setOrderDetailData() {

        myOrderProductList = myOrderDetailResponse!!.products!!
        val myOrderProductItemAdapter = MyOrderProductItemAdapter(this, myOrderProductList)
        order_detail_recyl_products.layoutManager = LinearLayoutManager(this)
        order_detail_recyl_products.adapter = myOrderProductItemAdapter

        order_detail_txt_address_line.text = myOrderDetailResponse!!.order!!.deliveryAddress
        order_detail_txt_delivery_time_line1.text = Utils.getInstance().convertTimeFormat(myOrderDetailResponse!!.order!!.deliveryDate!!, "yyyy-MM-dd", "MMM dd, yyyy")

        val deliveryTime = getString(R.string.from) + " " + Utils.getInstance().convertTimeFormat(myOrderDetailResponse!!.order!!.deliveryStartTime!!, "HH:mm:ss", "hh:mm a") + " " + getString(R.string.to) + " " + Utils.getInstance().convertTimeFormat(myOrderDetailResponse!!.order!!.deliveryEndTime!!, "HH:mm:ss", "hh:mm a")

        order_detail_txt_delivery_time_line2.text = deliveryTime

        order_detail_txt_payment_type.text = myOrderDetailResponse?.order?.paymentMode

        order_detail_txt_subtotal.text = String.format("%.3f", myOrderDetailResponse?.order?.netAmount!!.toDouble()) + " " + getString(R.string.omr)
        order_detail_txt_delivery_fee.text = String.format("%.3f", myOrderDetailResponse?.order?.deliveryCharges!!.toDouble()) + " " + getString(R.string.omr)

        order_detail_txt_total_amount.text = String.format("%.3f", myOrderDetailResponse?.order?.totalAmount!!.toDouble()) + " " + getString(R.string.omr)

        val totalDiscount = myOrderDetailResponse?.order?.loyaltyDiscount!!.toDouble() + myOrderDetailResponse?.order?.couponDiscount!!.toDouble()

        order_detail_txt_discount.text = String.format("%.3f", totalDiscount.toDouble()) + " " + getString(R.string.omr)

        order_detail_txt_order_status.text = myOrderDetailResponse?.order?.orderDeliveryStatus

        if (myOrderDetailResponse?.order?.paymentMode.equals("COD"))
            order_detail_txt_payment_type.text = getString(R.string.COD)
        else
            order_detail_txt_payment_type.text = myOrderDetailResponse?.order?.paymentMode

        if (myOrderDetailResponse?.order?.orderDeliveryStatus.equals("Cancelled"))
            order_detail_txt_order_status.text = getString(R.string.Cancelled)
        else if (myOrderDetailResponse?.order?.orderDeliveryStatus.equals("Not Cancelled"))
            order_detail_txt_order_status.text = getString(R.string.NotCancelled)
        else if (myOrderDetailResponse?.order?.orderDeliveryStatus.equals("Pending"))
            order_detail_txt_order_status.text = getString(R.string.Pending)
        else if (myOrderDetailResponse?.order?.orderDeliveryStatus.equals("Under Packaging"))
            order_detail_txt_order_status.text = getString(R.string.UnderPackaging)
        else if (myOrderDetailResponse?.order?.orderDeliveryStatus.equals("Ready To Deliver"))
            order_detail_txt_order_status.text = getString(R.string.ReadyToDeliver)
        else if (myOrderDetailResponse?.order?.orderDeliveryStatus.equals("Delivered"))
            order_detail_txt_order_status.text = getString(R.string.delivered)

        if (myOrderDetailResponse?.order?.orderDeliveryStatus.equals("Pending"))
            reorderTv.visibility = GONE
        if (showCancelButton())
            order_detail_btn_cancel.visibility = View.VISIBLE
        else
            order_detail_btn_cancel.visibility = View.GONE

        if (showTrackOrderButton()) {
            order_detail_btn_track_order.visibility = View.VISIBLE
            order_detail_txt_order_call_captain.visibility = View.VISIBLE
        } else {
            order_detail_btn_track_order.visibility = View.GONE
            order_detail_txt_order_call_captain.visibility = View.GONE
        }
        order_detail_txt_order_call_captain.setOnClickListener(this)

        if (myOrderDetailResponse!!.deliveryBoy != null) {
            order_detail_lyt_delivery_boy.visibility = View.VISIBLE
            setDeliveryBoyData()
        } else
            order_detail_lyt_delivery_boy.visibility = View.GONE
    }

    private fun showTrackOrderButton(): Boolean {
        if (myOrderDetailResponse?.order?.isCancelled.equals("1"))
            return false

        if (myOrderDetailResponse?.order?.orderDeliveryStatus.equals("delivered", true))
            return false

        if (myOrderDetailResponse?.deliveryBoy != null)
            return true
        return false
    }

    private fun setDeliveryBoyData() {
        val deliveryBoyInfo = myOrderDetailResponse!!.deliveryBoy
        if (deliveryBoyInfo != null)
            order_detail_txt_order_captain_name.text = deliveryBoyInfo.name
        if (deliveryBoyInfo != null)
            order_detail_txt_order_captain_no.text = deliveryBoyInfo.mobileNumber
    }


    private fun showCancelButton(): Boolean {
        if (myOrderDetailResponse?.order?.isCancelled.equals("1"))
            return false

        if (myOrderDetailResponse?.order?.orderDeliveryStatus.equals("delivered", true))
            return false

        if (myOrderDetailResponse?.deliveryBoy != null)
            return false

        if (myOrderDetailResponse?.order?.cancelledBefore!!.toInt() <= Utils.getInstance().getHourDifferenceBetweenTwoDate(myOrderDetailResponse?.order?.orderDate!!))
            return false

        return true
    }

    private fun setProgressState(progressVisibility: Int, viewVisibility: Int) {
        progressBar.visibility = progressVisibility
        contentLayout.visibility = viewVisibility
    }

    override fun onStop() {
        super.onStop()
        myOrderDetailDisposable.dispose()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {

                if (FROM == 1) {
                    var intent = Intent(this, HomeActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                } else if (FROM == 2) {
                    if (isCalled) {
                        val intent = Intent()
                        intent.putExtra(AppIntentExtraKeys.IS_READ, is_read)
                        setResult(Activity.RESULT_OK, intent)
                    }
                }
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (FROM == 1) {
            var intent = Intent(this, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } else if (FROM == 2) {
            if (isCalled) {
                val intent = Intent()
                intent.putExtra(AppIntentExtraKeys.IS_READ, is_read)
                setResult(Activity.RESULT_OK, intent)
            }
        }
        super.onBackPressed()
//        onBackPressed()
    }
}
