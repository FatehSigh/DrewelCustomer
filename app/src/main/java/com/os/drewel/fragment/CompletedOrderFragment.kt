package com.os.drewel.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
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
import com.os.drewel.constant.AppIntentExtraKeys
import com.os.drewel.constant.Constants
import com.os.drewel.constant.Constants.COMPLETED_ORDER
import com.os.drewel.delegate.OnClickItem
import com.os.drewel.utill.EqualSpacingItemDecoration
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.my_current_order.*

/**
 * Created by monikab on 2/21/2018.
 */
class CompletedOrderFragment : BaseFragment(), OnClickItem {
    override fun onClick(tag: String, position: Int) {
    }

    private var currentOrderDisposable: Disposable? = null
    private var myCurrentOrderList: List<Order> = ArrayList()
    private var currentOrderAdapter: MyCurrentOrderAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.my_current_order, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        updateMenuTitles()
        callMyCurrentOrderApi()
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

    private fun callMyCurrentOrderApi() {
        setProgressState(View.VISIBLE)

        val myCurrentOrderRequest = HashMap<String, String>()
        myCurrentOrderRequest["user_id"] = pref!!.getPreferenceStringData(pref!!.KEY_USER_ID)
        myCurrentOrderRequest["language"] = DrewelApplication.getInstance().getLanguage()
        myCurrentOrderRequest["flag"] = Constants.PREVIOUS_ORDERS
        val myCurrentOrderObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).getMyOrders(myCurrentOrderRequest)
        currentOrderDisposable = myCurrentOrderObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    setProgressState(View.GONE)
                    if (result.response!!.status!!) {
                        noOrderAlertTv.visibility = View.GONE
                        myOrderRv.visibility = View.VISIBLE
                        myCurrentOrderList = result.response?.data?.order!!
                        setAdapter()
                    } else {
                        noOrderAlertTv.visibility = View.VISIBLE
                        myOrderRv.visibility = View.GONE
                    }
                }, { error ->
                    setProgressState(View.GONE)
                    Log.e("TAG", "{$error.message}")
                }
                )
    }

    private fun setAdapter() {
        if (currentOrderAdapter == null) {
            myOrderRv.layoutManager = LinearLayoutManager(context)
//            myOrderRv.addItemDecoration(EqualSpacingItemDecoration(26, EqualSpacingItemDecoration.VERTICAL))

            currentOrderAdapter = MyCurrentOrderAdapter(context, myCurrentOrderList, COMPLETED_ORDER, this)
            myOrderRv.adapter = currentOrderAdapter
        } else
            currentOrderAdapter?.notifyDataSetChanged()
    }

    private fun setProgressState(visibility: Int) {
        progressBar.visibility = visibility
    }

    override fun onStop() {
        super.onStop()
        currentOrderDisposable?.dispose()
    }

}