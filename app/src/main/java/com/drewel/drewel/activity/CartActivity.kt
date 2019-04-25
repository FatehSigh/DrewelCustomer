package com.drewel.drewel.activity

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.MenuItem
import android.view.View
import com.drewel.drewel.R
import com.drewel.drewel.adapter.CartItemAdapter
import com.drewel.drewel.apicall.DrewelApi
import com.drewel.drewel.apicall.responsemodel.cartdetailresponsemodel.Cart
import com.drewel.drewel.application.DrewelApplication
import com.drewel.drewel.constant.AppIntentExtraKeys
import com.drewel.drewel.prefrences.Prefs
import com.drewel.drewel.rxbus.CartRxJavaBus
import com.drewel.drewel.utill.Utils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_my_cart.*
import kotlinx.android.synthetic.main.content_my_cart.*
import me.leolin.shortcutbadger.ShortcutBadger
import java.text.DecimalFormat
import java.text.NumberFormat
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
        if (isNetworkAvailable())
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
                    var unread = Prefs.getInstance(this).getPreferenceIntData(Prefs.getInstance(this).UNREAD_COUNT)
                    unread -= 1
                    Prefs.getInstance(this).setPreferenceIntData(Prefs.getInstance(this).UNREAD_COUNT, unread)
                    ShortcutBadger.applyCount(this, Prefs.getInstance(this).getPreferenceIntData(Prefs.getInstance(this).UNREAD_COUNT))
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

    var out_of_stock = false
    private fun setAdapter() {
        if (cartItemAdapter == null) {
            cartItemAdapter = CartItemAdapter(this, cartProductList)
            cartItemRecyclerView.layoutManager = LinearLayoutManager(this)
            cartItemRecyclerView.adapter = cartItemAdapter
            cartItemAdapter!!.cartItemClickSubject = cartItemClickSubject
        }
        for (i in cartProductList.indices) {
            if (cartProductList[i].outOfStock == 1) {
                out_of_stock = true
                break
            }
        }
    }

    private fun productQuantityChangeListener() {
        itemClickDisposable = cartItemClickSubject.subscribe {
            if (cartProductList.isEmpty()) {
                noItemAvailableTv.visibility = View.VISIBLE
                setProgressState(View.GONE, View.GONE)
            } else
                setTotalAmount()
        }
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

    var cartResponse: com.drewel.drewel.apicall.responsemodel.cartdetailresponsemodel.Data? = null

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
                        cartResponse = result.response!!.data!!
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
//                        Utils.getInstance().showToast(this, result.response!!.message!!)
//                        Toast.makeText(this, result.response!!.message, Toast.LENGTH_LONG).show()

                    }
                }, { error ->
                    setProgressState(View.GONE, View.GONE)
//                    Utils.getInstance().showToast(this, error.message!!)
                    Log.e("TAG", "{$error.message}")
                }
                )
    }


    private fun setData() {
        var quantityData = ""
        if (cartProductList.size > 1)
            quantityData = cartProductList.size.toString() + " " + getString(R.string.items)
        else
            quantityData = cartProductList.size.toString() + " " + getString(R.string.item)
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
        var quantityData = ""
        if (cartProductList.size > 1)
            quantityData = cartProductList.size.toString() + " " + getString(R.string.items)
        else
            quantityData = cartProductList.size.toString() + " " + getString(R.string.item)
        productQuantityTv.text = quantityData
        tv_amount_total.text = String.format("%.3f", totalAmount) + " " + getString(R.string.omr)
        orderItemQuantity = totalItemQuantity.toString()
//        productQuantityTv.text = totalItemQuantity.toString()
        val nf = NumberFormat.getNumberInstance(Locale.US)
        val formatter = nf as DecimalFormat
        formatter.applyPattern("#.###")
        val fString =/*String.format("%.3f",totalAmount)*/ formatter.format(totalAmount)
//        return convertNumbersToEnglish(fString)
        orderNetPrice = fString
//        orderNetPrice = DecimalFormat("#.###").format((totalAmount))
    }

    fun nFormate(d: Float): String {
        val nf = NumberFormat.getInstance(Locale.ENGLISH)
//        nf.maximumFractionDigits = 10
        return nf.format(d)
    }

    private fun setProgressState(progressVisibility: Int, viewVisibility: Int) {
        progressBar.visibility = progressVisibility
        contentLayout.visibility = viewVisibility
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.continueShoppingBt -> {
                val intent = Intent(applicationContext, HomeActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)

            }
            R.id.checkoutBt -> {
                if (!isNetworkAvailable())
                    return
                for (i in cartProductList.indices) {
                    if (cartProductList[i].outOfStock == 1) {
                        out_of_stock = true
                        break
                    } else
                        out_of_stock = false
                }
                if (out_of_stock) {
                    Utils.getInstance().showToast(this, getString(R.string.remove_item_which_are_out_of_stock))
                    for (i in cartProductList.indices) {
                        if (cartProductList[i].outOfStock == 1) {
                            cartItemRecyclerView.scrollToPosition(i)
                            break
                        }
                    }
                } else {
                    if (orderNetPrice.toDouble() < 5) {
                        val logoutAlertDialog = AlertDialog.Builder(this, R.style.DeliveryTypeTheme).create()
                        logoutAlertDialog.setTitle(getString(R.string.app_name))
                        Log.e("min==", getString(R.string.minimum_order_validation) + String.format("%.3f", 5.000) + " " + getString(R.string.omr))
                        logoutAlertDialog.setMessage(getString(R.string.minimum_order_validation) + " " + String.format("%.3f", 5.000) + " " + getString(R.string.omr))
                        logoutAlertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok)) { dialog, id ->
                            logoutAlertDialog.dismiss()
                        }
                        logoutAlertDialog.show()
                    } else {
                        if (pref?.getPreferenceStringData(pref!!.KEY_FULL_DELIVERY_ADDRESS).isNullOrEmpty()) {
                            val logoutAlertDialog = AlertDialog.Builder(this, R.style.DeliveryTypeTheme).create()
                            logoutAlertDialog.setTitle(getString(R.string.app_name))
                            logoutAlertDialog.setMessage(getString(R.string.add_delivery_address))
                            logoutAlertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok), DialogInterface.OnClickListener { dialog, id ->
                                logoutAlertDialog.dismiss()
                                val intent = Intent(this, DeliveryAddressActivity::class.java)
                                startActivity(intent)
                            })
                            logoutAlertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel), DialogInterface.OnClickListener { dialog, id ->
                                logoutAlertDialog.dismiss()
                            })
                            logoutAlertDialog.show()
                        } else {

                            val intent = Intent(this, CheckOutActivity::class.java)
                            intent.putExtra(AppIntentExtraKeys.ADDRESS, pref?.getPreferenceStringData(pref!!.KEY_FULL_DELIVERY_ADDRESS))
                            intent.putExtra(AppIntentExtraKeys.NAME, pref?.getPreferenceStringData(pref!!.KEY_DELIVERY_ADDRESS_USERNAME))
                            intent.putExtra(AppIntentExtraKeys.MOBILE_NUMBER, pref?.getPreferenceStringData(pref!!.KEY_DELIVERY_ADDRESS_PHONE_NUMBER))
                            intent.putExtra(AppIntentExtraKeys.LANDMARK, pref?.getPreferenceStringData(pref!!.KEY_DELIVERY_ADDRESS_lANDMARK))
                            startActivity(intent)
                        }
                    }
//                    if (cartResponse!=null && cartResponse!!.is_edited!!.isNotEmpty() && cartResponse!!.is_edited!!.equals("1")){
//                    }
//                    if (pref?.getPreferenceStringData(pref!!.KEY_FULL_DELIVERY_ADDRESS).isNullOrEmpty()) {
//                        val intent = Intent(this, DeliveryDetailActivity::class.java)
//                        startActivity(intent)
//                    } else {
//                    }
                }
            }
        }

    }

}