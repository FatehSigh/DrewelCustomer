package com.drewel.drewel.fragment

import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.drewel.drewel.R
import com.drewel.drewel.activity.HomeActivity
import com.drewel.drewel.adapter.ProductAdapter2
import com.drewel.drewel.apicall.DrewelApi
import com.drewel.drewel.apicall.responsemodel.Product
import com.drewel.drewel.apicall.responsemodel.productlistresponsemodel.Brand
import com.drewel.drewel.application.DrewelApplication
import com.drewel.drewel.utill.EqualSpacingItemDecoration
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.content_brand_wise_products.*
import java.util.*

/**
 * Created by monikab on 3/15/2018.
 */
class OfferFragment : BaseFragment() {

    private var productAdapter: ProductAdapter2? = null

    var disposable: Disposable? = null

    var brand: Brand = Brand()
    var productList: List<Product> = ArrayList()

    //    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_brand_wise_product)
//        initView()
//    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        return inflater.inflate(R.layout.content_brand_wise_products, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateMenuTitles()
        initView()
        /* call api if network available8*/
        if (isNetworkAvailable())
            callDiscountListApi()
    }

    private fun setProgressState(visibility: Int) {
        progressBar.visibility = visibility
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

    private fun initView() {
        setAdapter()
    }

    private fun callDiscountListApi() {
        setProgressState(View.VISIBLE)
        val getWishListRequest = HashMap<String, String>()

        if (pref!!.getPreferenceStringData(pref!!.KEY_USER_ID).isEmpty())
            getWishListRequest["user_id"] = "1"
        else
            getWishListRequest["user_id"] = pref!!.getPreferenceStringData(pref!!.KEY_USER_ID)
        getWishListRequest["language"] = DrewelApplication.getInstance().getLanguage()

        val getProductObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).getDiscount(getWishListRequest)
        disposable = getProductObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    DrewelApplication.getInstance().logoutWhenAccountDeactivated(result.response!!.isDeactivate!!, activity!!)
                    setProgressState(View.GONE)

                    if (result.response!!.status!!) {
                        brandWiseProductRecyclerView.visibility = View.VISIBLE
                        productList = result.response!!.data!!.product!!
                        setAdapter()

                    } else {
                        brandWiseProductRecyclerView.visibility = View.INVISIBLE
                        com.drewel.drewel.utill.Utils.getInstance().showToast(activity, result.response!!.message!!)
                    }
                }, { error ->
                    setProgressState(View.GONE)
                    com.drewel.drewel.utill.Utils.getInstance().showToast(activity, error.message!!)
                    Log.e("TAG", "{$error.message}")
                }
                )
    }

    private fun setAdapter() {
        if (productAdapter == null) {
            productAdapter = ProductAdapter2(activity!!)
            productAdapter!!.productList = productList
            brandWiseProductRecyclerView.layoutManager = GridLayoutManager(activity, 2)
            brandWiseProductRecyclerView.addItemDecoration(EqualSpacingItemDecoration(10, EqualSpacingItemDecoration.GRID))
            brandWiseProductRecyclerView.adapter = productAdapter

        } else {
            productAdapter!!.productList = productList
            productAdapter!!.notifyDataSetChanged()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (disposable != null)
            disposable!!.dispose()
    }


}