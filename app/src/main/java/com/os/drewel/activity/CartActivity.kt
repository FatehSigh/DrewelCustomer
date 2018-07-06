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
import com.os.drewel.adapter.CartItemAdapter
import com.os.drewel.apicall.DrewelApi
import com.os.drewel.apicall.responsemodel.cartdetailresponsemodel.Cart
import com.os.drewel.application.DrewelApplication
import com.os.drewel.constant.AppIntentExtraKeys
import com.os.drewel.rxbus.CartRxJavaBus
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_my_cart.*
import kotlinx.android.synthetic.main.content_delivery_details_activity.*
import kotlinx.android.synthetic.main.content_my_cart.*
import java.text.DecimalFormat
import java.util.*

/**
 * Created by monikab on 4/3/2018.
 */
class CartActivity : BaseActivity(), View.OnClickListener {


    private var cartItemAdapter: CartItemAdapter? = null
    private var disposable: Disposable? = null
    private var cartProductList = mutableListOf<Cart>()
    private lateinit var itemClickDisposable: Disposable
    private val cartItemClickSubject = PublishSubject.create<String>()
    var is_read = ""
    var isCalled = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_cart)
        initView()
    }

    private var notificationId = ""
    private fun initView() {
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
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
        checkoutBt.setOnClickListener(this)
        continueShoppingBt.setOnClickListener(this)
        callGetProductApi()
        productQuantityChangeListener()
    }

    private fun callReadNotificationApi() {
//        setProgressState(View.VISIBLE, View.GONE)
        val readNotificationRequest = java.util.HashMap<String, String>()
        readNotificationRequest["user_id"] = pref!!.getPreferenceStringData(pref!!.KEY_USER_ID)
        readNotificationRequest["language"] = DrewelApplication.getInstance().getLanguage()
        readNotificationRequest["notification_id"] = notificationId
        val cancelOrderObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).readNotification(readNotificationRequest)
        cancelOrderObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    DrewelApplication.getInstance().logoutWhenAccountDeactivated(result.response!!.isDeactivate!!, this)
                    isCalled = true
                    is_read = "1"
                }, { error ->
                    Log.e("TAG", "{$error.message}")
                }
                )
//        myOrderDetailDisposable.add(disposable)
    }

    var FROM = 0

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
    }

    private fun setAdapter() {
        if (cartItemAdapter == null) {
            cartItemAdapter = CartItemAdapter(this, cartProductList)
            cartItemRecyclerView.layoutManager = LinearLayoutManager(this)
            cartItemRecyclerView.adapter = cartItemAdapter
            cartItemAdapter!!.cartItemClickSubject = cartItemClickSubject
        }
    }

    private fun productQuantityChangeListener() {
        itemClickDisposable = cartItemClickSubject.subscribe({
            if (cartProductList.isEmpty()) {
                noItemAvailableTv.visibility = View.VISIBLE
                setProgressState(View.GONE, View.GONE)
            } else
                setTotalAmount()
        }
        )
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


    private fun callGetProductApi() {
        setProgressState(View.VISIBLE, View.GONE)
        val getProductRequest = HashMap<String, String>()
        getProductRequest["cart_id"] = pref!!.getPreferenceStringData(pref!!.KEY_CART_ID)
        getProductRequest["user_id"] = pref!!.getPreferenceStringData(pref!!.KEY_USER_ID)
        getProductRequest["language"] = DrewelApplication.getInstance().getLanguage()

        val getProductObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).getCartDetail(getProductRequest)
        disposable = getProductObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->

                    DrewelApplication.getInstance().logoutWhenAccountDeactivated(result.response!!.isDeactivate!!, this)

                    if (result.response!!.status!!) {

                        noItemAvailableTv.visibility = View.GONE
                        setProgressState(View.GONE, View.VISIBLE)
                        cartProductList = result.response!!.data!!.cart!!

                        pref!!.setPreferenceStringData(pref!!.KEY_CART_ID, result.response!!.data!!.cartId!!)
                        CartRxJavaBus.getInstance().cartPublishSubject.onNext(result.response!!.data!!.quantity!!)

                        setData()
                        setAdapter()
                    } else {
                        pref!!.setPreferenceStringData(pref!!.KEY_CART_ID, "")
                        CartRxJavaBus.getInstance().cartPublishSubject.onNext("0")
                        noItemAvailableTv.visibility = View.VISIBLE
                        setProgressState(View.GONE, View.GONE)
                        Toast.makeText(this, result.response!!.message, Toast.LENGTH_LONG).show()

                    }
                }, { error ->
                    setProgressState(View.GONE, View.GONE)
                    Toast.makeText(this, error.message, Toast.LENGTH_LONG).show()
                    Log.e("TAG", "{$error.message}")
                }
                )
    }


    private fun setData() {
        val quantityData = cartProductList.size.toString() + " " + getString(R.string.items)
        productQuantityTv.text = quantityData
        setTotalAmount()
    }

    private fun setTotalAmount() {
        var totalItemQuantity = 0
        var totalAmount = 0f
        for (i in cartProductList.indices) {
            val cartItem = cartProductList[i]
            val price = if (!cartItem.offerPrice.isNullOrEmpty())
                cartItem.offerPrice!!.toFloat()
            else
                cartItem.productPrice!!.toFloat()

            val quantity = cartItem.quantity!!.toInt()
            totalItemQuantity += quantity
            totalAmount += price * quantity
        }
        tv_amount_total.text = DecimalFormat("#.###").format(totalAmount) + " " + getString(R.string.omr)
        orderItemQuantity = totalItemQuantity.toString()
        orderNetPrice = DecimalFormat("#.###").format(totalAmount)
    }


    private fun setProgressState(progressVisibility: Int, viewVisibility: Int) {
        progressBar.visibility = progressVisibility
        contentLayout.visibility = viewVisibility
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.continueShoppingBt -> {

                finish()
            }
            R.id.checkoutBt -> {
                if (cartItemAdapter?.isAnyProductOutOfStock!!) {
                    Toast.makeText(this, getString(R.string.remove_item_which_are_out_of_stock), Toast.LENGTH_SHORT).show()

                } else if (pref?.getPreferenceStringData(pref!!.KEY_FULL_DELIVERY_ADDRESS).isNullOrEmpty()) {
                    val intent = Intent(this, DeliveryDetailActivity::class.java)

                    startActivity(intent)
                } else {
                    val intent = Intent(this, CheckOutActivity::class.java)

                    intent.putExtra(AppIntentExtraKeys.ADDRESS, pref?.getPreferenceStringData(pref!!.KEY_FULL_DELIVERY_ADDRESS))
                    intent.putExtra(AppIntentExtraKeys.NAME, pref?.getPreferenceStringData(pref!!.KEY_DELIVERY_ADDRESS_USERNAME))
                    intent.putExtra(AppIntentExtraKeys.MOBILE_NUMBER, pref?.getPreferenceStringData(pref!!.KEY_DELIVERY_ADDRESS_PHONE_NUMBER))
                    intent.putExtra(AppIntentExtraKeys.LANDMARK, pref?.getPreferenceStringData(pref!!.KEY_DELIVERY_ADDRESS_lANDMARK))
                    startActivity(intent)
                }
            }
        }

    }

}