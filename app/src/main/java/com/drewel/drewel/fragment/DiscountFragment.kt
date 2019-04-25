package com.drewel.drewel.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.drewel.drewel.R
import com.drewel.drewel.activity.HomeActivity
import com.drewel.drewel.adapter.DiscountAdapter
import com.drewel.drewel.apicall.DrewelApi
import com.drewel.drewel.apicall.responsemodel.couponresponsemodel.Coupon
import com.drewel.drewel.application.DrewelApplication
import com.drewel.drewel.constant.AppIntentExtraKeys
import com.drewel.drewel.utill.EqualSpacingItemDecoration
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.layout_discount.*

/**
 * Created by sharukhb on 3/13/2018.
 */

class DiscountFragment : BaseFragment() {


    private var discountAdapter: DiscountAdapter? = null
    var disposable: Disposable? = null

    var couponList: List<Coupon> = ArrayList()
    var isFromCheckout = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        return inflater.inflate(R.layout.layout_discount, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateMenuTitles()
        /* call api if network available8*/
        if (isNetworkAvailable())
            callCategoryListApi()
    }

    private fun updateMenuTitles() {
        if (activity is HomeActivity) {
            var activitie = activity as HomeActivity
            if (activitie.menu == null)
                return
            activitie.menu!!.findItem(R.id.menu_addrequest).isVisible = false
            activitie.menu!!.findItem(R.id.menu_filter).isVisible = false
            activitie.menu!!.findItem(R.id.menu_carts).isVisible = true
            activitie.menu!!.findItem(R.id.menu_whishlist).isVisible = true

        }
    }

    private fun setAdapter() {

        if (discountAdapter == null) {
            val llm = LinearLayoutManager(context)
            llm.orientation = LinearLayoutManager.VERTICAL
            discountRv!!.layoutManager = llm
            discountRv.addItemDecoration(EqualSpacingItemDecoration(26, EqualSpacingItemDecoration.VERTICAL))
            discountAdapter = DiscountAdapter(context, couponList)
            discountRv!!.adapter = discountAdapter
            Log.e("isFromCheckout==>",isFromCheckout.toString())
            discountAdapter?.isFromCheckout = isFromCheckout

            discountAdapter?.applyCouponCodeClickSubject?.subscribe(
                    { couponCodePosition ->
                        if (isFromCheckout) {

                            val intent = Intent()
                            intent.putExtra(AppIntentExtraKeys.COUPON_CODE, couponList[couponCodePosition].couponCode)
                            activity?.setResult(Activity.RESULT_OK, intent)
                            activity?.finish()
                        }
                    }
            )
        } else
            discountAdapter!!.notifyDataSetChanged()


    }


    private fun callCategoryListApi() {

        setProgressState(View.VISIBLE)
        val categoryRequest = HashMap<String, String>()
        categoryRequest.put("user_id", pref!!.getPreferenceStringData(pref!!.KEY_USER_ID))
        categoryRequest.put("language", DrewelApplication.getInstance().getLanguage())

        val signUpObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).getDiscountList(categoryRequest)
        disposable = signUpObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    setProgressState(View.GONE)
                    DrewelApplication.getInstance().logoutWhenAccountDeactivated(result.response!!.isDeactivate!!, context!!)
                    if (result.response!!.status!!) {

                        couponList = result.response!!.data!!.coupons!!
                        setAdapter()

                    } else
                        com.drewel.drewel.utill.Utils.getInstance().showToast(activity, result.response!!.message!!)

                }, { error ->
                    setProgressState(View.GONE)
                    com.drewel.drewel.utill.Utils.getInstance().showToast(activity, error.message!!)
                    Log.e("TAG", "{$error.message}")
                }
                )
    }

    private fun setProgressState(visibility: Int) {
        progressBar.visibility = visibility
    }

    override fun onDestroy() {
        super.onDestroy()
        if (disposable != null)
            disposable!!.dispose()
    }

}