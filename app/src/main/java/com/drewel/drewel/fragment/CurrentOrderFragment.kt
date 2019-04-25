package com.drewel.drewel.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.drewel.drewel.R
import com.drewel.drewel.activity.HomeActivity
import com.drewel.drewel.adapter.MyCurrentOrderAdapter
import com.drewel.drewel.apicall.DrewelApi
import com.drewel.drewel.apicall.responsemodel.myorderresponsemodel.Order
import com.drewel.drewel.application.DrewelApplication
import com.drewel.drewel.constant.Constants
import com.drewel.drewel.constant.Constants.CURRENT_ORDER
import com.drewel.drewel.delegate.OnClickItem
import com.drewel.drewel.prefrences.Prefs
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.my_current_order.*

/**
 * Created by monikab on 2/21/2018.
 */
class CurrentOrderFragment : BaseFragment(), OnClickItem {
    override fun onClick(tag: String, position: Int) {
        if (tag.equals("Delete")) {
            if (isNetworkAvailable())
                callDeleteOrderApi(position)
        }
    }

    private var currentOrderDisposable: Disposable? = null
    private var myCurrentOrderList: MutableList<Order> = ArrayList()
    private var currentOrderAdapter: MyCurrentOrderAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.my_current_order, container, false)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        val intentFilter = IntentFilter()
        intentFilter.addAction("UPDATE_STATUS")
        activity!!.registerReceiver(broadcastReceiver, intentFilter)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        updateMenuTitles()
        if (isNetworkAvailable())
            callMyCurrentOrderApi(VISIBLE)
        swipeRefreshLayout.setOnRefreshListener(object : SwipeRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                if (isNetworkAvailable())
                    refreshItems()
            }
        })
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            //   Toast.makeText(context, "This is the broadcast", Toast.LENGTH_SHORT).show();
            if (isNetworkAvailable())
                refreshItems()
        }
    }

    fun refreshItems() {
        // Load complete
        Handler().postDelayed({
            try {
                if (isAdded)
                    callMyCurrentOrderApi(GONE)
            } catch (e: Exception) {
                swipeRefreshLayout.isRefreshing = false
            }
        }, 2000)
    }

    private fun callDeleteOrderApi(position: Int) {
        val pref = Prefs.getInstance(activity)
        val updateCartRequest = java.util.HashMap<String, String>()
        updateCartRequest["user_id"] = pref.getPreferenceStringData(pref.KEY_USER_ID)
        updateCartRequest["language"] = DrewelApplication.getInstance().getLanguage()
        updateCartRequest["order_id"] = myCurrentOrderList[position].orderId!!
        val updateCartObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).cancelOrder(updateCartRequest)
        updateCartObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    DrewelApplication.getInstance().logoutWhenAccountDeactivated(result.response!!.isDeactivate!!, activity!!)
                    if (result.response!!.status!!) {
                        myCurrentOrderList.removeAt(position)
                        currentOrderAdapter!!.notifyDataSetChanged()
//                        callMyCurrentOrderApi(VISIBLE)
                    } else {
                        com.drewel.drewel.utill.Utils.getInstance().showToast(activity, result.response!!.message!!)
                    }
                }, { error ->
                    com.drewel.drewel.utill.Utils.getInstance().showToast(activity, error.message!!)
                    Log.e("TAG", "{$error.message}")
                }
                )
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

    private fun callMyCurrentOrderApi(show: Int) {
        setProgressState(show)
        val myCurrentOrderRequest = HashMap<String, String>()
        myCurrentOrderRequest["user_id"] = pref!!.getPreferenceStringData(pref!!.KEY_USER_ID)
        myCurrentOrderRequest["language"] = DrewelApplication.getInstance().getLanguage()
        myCurrentOrderRequest["flag"] = Constants.CURRENT_ORDERS
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
//                        if (result.response?.data?.order!!.isEmpty())
//                            myCurrentOrderList = ArrayList()
                        if (myCurrentOrderList.size > 0) {
                            noOrderAlertTv.visibility = View.GONE
                            myOrderRv.visibility = View.VISIBLE
                        } else {
                            noOrderAlertTv.visibility = View.VISIBLE
                            myOrderRv.visibility = View.GONE
                        }
                        setAdapter()
                    } else {
                        noOrderAlertTv.visibility = View.VISIBLE
                        myOrderRv.visibility = View.GONE
                    }
                }, { error ->
                    noOrderAlertTv.visibility = View.VISIBLE
                    myOrderRv.visibility = View.GONE
                    swipeRefreshLayout.isRefreshing = false
                    setProgressState(View.GONE)
                    Log.e("TAG", "{$error.message}")
                })
    }

    private fun setAdapter() {
        myOrderRv.layoutManager = LinearLayoutManager(context)
        currentOrderAdapter = MyCurrentOrderAdapter(context, myCurrentOrderList, CURRENT_ORDER, this)
        myOrderRv.adapter = currentOrderAdapter
    }

    override fun onDestroyView() {
        for (i in myCurrentOrderList) {
            i.stopTimer = true
        }
        if (currentOrderAdapter != null)
            currentOrderAdapter!!.notifyDataSetChanged()
        super.onDestroyView()

    }

    override fun onDestroy() {
        for (i in myCurrentOrderList) {
            i.stopTimer = true
        }
        if (currentOrderAdapter != null)
            currentOrderAdapter!!.notifyDataSetChanged()
        super.onDestroy()
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