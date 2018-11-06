package com.os.drewel.fragment

import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.os.drewel.R
import com.os.drewel.activity.HomeActivity
import com.os.drewel.adapter.MyCurrentOrderAdapter
import com.os.drewel.apicall.DrewelApi
import com.os.drewel.apicall.responsemodel.myorderresponsemodel.Order
import com.os.drewel.application.DrewelApplication
import com.os.drewel.constant.Constants
import com.os.drewel.constant.Constants.COMPLETED_ORDER
import com.os.drewel.delegate.OnClickItem
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.my_current_order.*

/**
 * Created by monikab on 2/21/2018.
 */
class CompletedOrderFragment : BaseFragment(), OnClickItem {
    override fun onClick(tag: String, position: Int) {
        if (tag.equals("DeletePrevious")) {
            if (isNetworkAvailable())
                callDeleteNotificationApi(position, false)
        }
    }

    private var currentOrderDisposable: Disposable? = null
    private var myCurrentOrderList: MutableList<Order> = ArrayList()
    private var currentOrderAdapter: MyCurrentOrderAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.my_current_order, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        updateMenuTitles()
        if (isNetworkAvailable())
            callMyCurrentOrderApi(View.VISIBLE)
        txt_clearall.setOnClickListener {
            showLogoutDialog(getString(R.string.want_to_delete_allorder), 0, true)
        }
        swipeRefreshLayout.setOnRefreshListener(object : SwipeRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                if (isNetworkAvailable())
                    refreshItems()
            }
        })
    }

    fun refreshItems() {
        // Load complete
        Handler().postDelayed({
            try {
                if (isAdded)
                    callMyCurrentOrderApi(View.GONE)
            } catch (e: Exception) {
                swipeRefreshLayout.isRefreshing = false
            }
        }, 2000)
    }

    private fun updateMenuTitles() {
        var activity = activity as HomeActivity
        if (activity.menu == null)
            return
        activity.menu!!.findItem(R.id.menu_addrequest).isVisible = false
        activity.menu!!.findItem(R.id.menu_filter).isVisible = false
        activity.menu!!.findItem(R.id.menu_carts).isVisible = true
        activity.menu!!.findItem(R.id.menu_whishlist).isVisible = true
    }

    private fun callMyCurrentOrderApi(visible: Int) {
        setProgressState(visible)

        val myCurrentOrderRequest = HashMap<String, String>()
        myCurrentOrderRequest["user_id"] = pref!!.getPreferenceStringData(pref!!.KEY_USER_ID)
        myCurrentOrderRequest["language"] = DrewelApplication.getInstance().getLanguage()
        myCurrentOrderRequest["flag"] = Constants.PREVIOUS_ORDERS
        val myCurrentOrderObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).getMyOrders(myCurrentOrderRequest)
        currentOrderDisposable = myCurrentOrderObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    swipeRefreshLayout.isRefreshing = false
                    setProgressState(View.GONE)
                    if (result.response!!.status!!) {
                        noOrderAlertTv.visibility = View.GONE
                        myOrderRv.visibility = View.VISIBLE
                        myCurrentOrderList = (result.response?.data?.order as MutableList<Order>?)!!
                        if (myCurrentOrderList.isEmpty())
                            txt_clearall.visibility = View.GONE
                        else
                            txt_clearall.visibility = View.VISIBLE
                        setAdapter()
                    } else {
                        noOrderAlertTv.visibility = View.VISIBLE
                        myOrderRv.visibility = View.GONE
                    }
                }, { error ->
                    swipeRefreshLayout.isRefreshing = false
                    setProgressState(View.GONE)
                    Log.e("TAG", "{$error.message}")
                }
                )
    }

    private fun setAdapter() {
//        if (currentOrderAdapter == null) {
        myOrderRv.layoutManager = LinearLayoutManager(context)
//            myOrderRv.addItemDecoration(EqualSpacingItemDecoration(26, EqualSpacingItemDecoration.VERTICAL))
        currentOrderAdapter = MyCurrentOrderAdapter(context, myCurrentOrderList, COMPLETED_ORDER, this)
        myOrderRv.adapter = currentOrderAdapter
//        } else
//            currentOrderAdapter?.notifyDataSetChanged()

//        val swipeHelper = object : SwipeHelper(activity, myOrderRv) {
//            override fun instantiateUnderlayButton(viewHolder: RecyclerView.ViewHolder, underlayButtons: MutableList<SwipeHelper.UnderlayButton>) {
//                underlayButtons.add(SwipeHelper.UnderlayButton(
//                        getString(R.string.delete),
//                        0,
//                        Color.parseColor("#eb011c"),
//                        UnderlayButtonClickListener {
//                            //                            Log.e("Position on delete", it.toString())
//                            showLogoutDialog(getString(R.string.want_to_delete_order), it, false)
//                        }
//                ))
//            }
//        }

    }

    private fun showLogoutDialog(message: String, position: Int, clearAll: Boolean) {
        val logoutAlertDialog = AlertDialog.Builder(activity!!, R.style.DeliveryTypeTheme).create()
        logoutAlertDialog.setTitle(getString(R.string.app_name))
        logoutAlertDialog.setMessage(message)
        logoutAlertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.yes), DialogInterface.OnClickListener { dialog, id ->
            logoutAlertDialog.dismiss()
            if (isNetworkAvailable())
                callDeleteNotificationApi(position, clearAll)
        })
        logoutAlertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.no), DialogInterface.OnClickListener { dialog, id ->
            logoutAlertDialog.dismiss()
            currentOrderAdapter!!.notifyDataSetChanged()
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
            readNotificationRequest["order_id"] = myCurrentOrderList[position].orderId!!
        val cancelOrderObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).clear_order(readNotificationRequest)
        cancelOrderObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    setProgressState(View.GONE)
                    DrewelApplication.getInstance().logoutWhenAccountDeactivated(result.response!!.isDeactivate!!, activity!!)
//                    if (FROM!=1){
                    if (clearAll) {
                        myCurrentOrderList = ArrayList()
                        myOrderRv.layoutManager = LinearLayoutManager(context)
                        currentOrderAdapter = MyCurrentOrderAdapter(context, myCurrentOrderList, COMPLETED_ORDER, this)
                        myOrderRv.adapter = currentOrderAdapter
                        txt_clearall.visibility = View.GONE
                    } else {
                        myCurrentOrderList.removeAt(position)
                        currentOrderAdapter!!.notifyDataSetChanged()
                        try {
                            if (myCurrentOrderList.isEmpty())
                                txt_clearall.visibility = View.GONE
                            else
                                txt_clearall.visibility = View.VISIBLE
                        } catch (e: Exception) {

                        }
                    }
                }, { error ->
                    setProgressState(View.GONE)
                    Log.e("TAG", "{$error.message}")
                })
    }

    private fun setProgressState(visibility: Int) {
        if (isAdded)
            progressBar.visibility = visibility
    }

    override fun onStop() {
        super.onStop()
        currentOrderDisposable?.dispose()
    }
}