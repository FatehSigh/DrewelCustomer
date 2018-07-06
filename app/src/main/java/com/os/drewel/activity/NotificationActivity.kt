package com.os.drewel.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.os.drewel.R
import com.os.drewel.adapter.NotificationAdapter
import com.os.drewel.apicall.DrewelApi
import com.os.drewel.apicall.responsemodel.notificationresponsemodel.Notification
import com.os.drewel.application.DrewelApplication
import com.os.drewel.constant.AppIntentExtraKeys
import com.os.drewel.delegate.OnClick
import com.os.drewel.firebase.DrewelFirebaseMessagingService
import com.os.drewel.prefrences.Prefs
import com.os.drewel.utill.BadgeIntentService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_notifiaction.*
import kotlinx.android.synthetic.main.app_toolbar.*
import me.leolin.shortcutbadger.ShortcutBadger


class NotificationActivity : BaseActivity() {

    private val defaultAddressClickSubject = PublishSubject.create<Int>()
    private lateinit var itemClickDisposable: Disposable
    private var notificationAdapter: NotificationAdapter? = null
    private var notificationList: List<Notification> = ArrayList()
    var position: Int = 0
    var unread: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifiaction)
        initView()
        clickOfAdapterItem()
        if (isNetworkAvailable())
            getNotificationApi()
    }

    private fun clickOfAdapterItem() {
        itemClickDisposable = defaultAddressClickSubject.subscribe { position ->
            this.position = position
            var intent: Intent? = null
            if (notificationList[position].type.equals(DrewelFirebaseMessagingService.NotificationType.general)) {
                notificationId = notificationList[position].id!!
                callReadNotificationApi()
                return@subscribe
            }

            if (notificationList[position].type.equals(DrewelFirebaseMessagingService.NotificationType.pendingCart)) {
                intent = Intent(this, CartActivity::class.java)
            } else if (notificationList[position].type.equals(DrewelFirebaseMessagingService.NotificationType.orderPlaced)) {
                intent = Intent(this, MyOrderDetailActivity::class.java)
                intent.putExtra(AppIntentExtraKeys.ORDER_ID, notificationList[position].item_id)
            } else if (notificationList[position].type.equals(DrewelFirebaseMessagingService.NotificationType.orderCancelled)) {
                intent = Intent(this, MyOrderDetailActivity::class.java)
                intent.putExtra(AppIntentExtraKeys.ORDER_ID, notificationList[position].item_id)
            } else if (notificationList[position].type.equals(DrewelFirebaseMessagingService.NotificationType.productAvailable)) {
                intent = Intent(this, ProductDetailActivity::class.java)
                intent.putExtra(AppIntentExtraKeys.PRODUCT_ID, notificationList[position].item_id)
            } else if (notificationList[position].type.equals(DrewelFirebaseMessagingService.NotificationType.deliveryStatusChange)) {
                intent = Intent(this, MyOrderDetailActivity::class.java)
                intent.putExtra(AppIntentExtraKeys.ORDER_ID, notificationList[position].item_id)
            } else if (notificationList[position].type.equals(DrewelFirebaseMessagingService.NotificationType.deliveryBoyAssigned)) {
                intent = Intent(this, MyOrderDetailActivity::class.java)
                intent.putExtra(AppIntentExtraKeys.ORDER_ID, notificationList[position].item_id)
            } /*else if (notificationList[position].type.equals(DrewelFirebaseMessagingService.NotificationType.general)) {// intent!!.putExtra(AppIntentExtraKeys.ORDER_ID, data.item_id)
                intent = Intent(this, NotificationActivity::class.java)
            }*/
            intent!!.putExtra(AppIntentExtraKeys.FROM, 2)
            intent.putExtra(AppIntentExtraKeys.NOTIFICATION_ID, notificationList[position].id)
            intent.putExtra(AppIntentExtraKeys.IS_READ, notificationList[position].isRead)
            startActivityForResult(intent, 2000)
        }
    }

    private var notificationId = ""
    private fun setAdapter() {
        if (notificationAdapter == null) {
            notificationAdapter = NotificationAdapter(this, notificationList)
            notificationAdapter!!.defaultAddressClickSubject = defaultAddressClickSubject
            notificationRv.layoutManager = LinearLayoutManager(this)
            notificationRv.adapter = notificationAdapter
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2000) {
            if (data != null) {
                notificationList[position].isRead = data.getStringExtra(AppIntentExtraKeys.IS_READ)
                notificationAdapter!!.notifyDataSetChanged()
            }
        }
    }

    private fun initView() {
        toolbarTitleTv.text = getString(R.string.notification)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        if (intent.getIntExtra(AppIntentExtraKeys.FROM, 0) == 1) {
            FROM = 1
            notificationId = intent.getStringExtra(AppIntentExtraKeys.NOTIFICATION_ID)
            if (isNetworkAvailable())
                callReadNotificationApi()
        }
    }

    @SuppressLint("CheckResult")
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
//                    if (FROM!=1){
                    notificationList[position].isRead = "1"
                    notificationAdapter!!.notifyDataSetChanged()

                    unread = Prefs.getInstance(this).getPreferenceIntData(Prefs.getInstance(this).UNREAD_COUNT)
                    unread -= 1
                    Prefs.getInstance(this).setPreferenceIntData(Prefs.getInstance(this).UNREAD_COUNT, unread)
                    ShortcutBadger.applyCount(this, Prefs.getInstance(this).getPreferenceIntData(Prefs.getInstance(this).UNREAD_COUNT))

                }, { error ->
                    Log.e("TAG", "{$error.message}")
                })
    }

    var FROM = 0
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (FROM == 1) {
                    var intent = Intent(this, HomeActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
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
        }
        super.onBackPressed()
    }

    private var disposable: Disposable? = null

    private fun getNotificationApi() {
        setProgressState(View.VISIBLE, false)
        val notificationRequest = HashMap<String, String>()
        notificationRequest["language"] = DrewelApplication.getInstance().getLanguage()
        notificationRequest["user_id"] = pref!!.getPreferenceStringData(pref!!.KEY_USER_ID)
        val signUpObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).getNotifications(notificationRequest)
        disposable = signUpObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    setProgressState(View.GONE, true)

                    if (result.response!!.status!!) {
                        notificationList = result.response!!.data!!.notifications!!
                        setAdapter()
                    } else
                        Toast.makeText(this, result.response!!.message, Toast.LENGTH_LONG).show()

                }, { error ->
                    setProgressState(View.GONE, true)
                    Toast.makeText(this, error.message, Toast.LENGTH_LONG).show()
                    Log.e("TAG", "{$error.message}")
                }
                )
    }

    private fun setProgressState(visibility: Int, enableButton: Boolean) {
        progressBar.visibility = visibility
        notificationRv.isEnabled = enableButton
    }

}


