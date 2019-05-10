package com.drewel.drewel.activity

import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.util.Log
import android.view.View
import com.drewel.drewel.R
import com.drewel.drewel.adapter.ProductAdapter2
import com.drewel.drewel.apicall.DrewelApi
import com.drewel.drewel.apicall.responsemodel.Product
import com.drewel.drewel.application.DrewelApplication
import com.drewel.drewel.constant.AppIntentExtraKeys
import com.drewel.drewel.utill.EqualSpacingItemDecoration
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_brand_wise_product.*
import kotlinx.android.synthetic.main.content_brand_wise_products.*
import java.util.*

/**
 * Created by monikab on 3/30/2018.
 */
class SearchWiseProductActivity : ProductBaseActivity() {

    private var productAdapter: ProductAdapter2? = null

    var disposable: Disposable? = null

    private var productList: List<Product> = ArrayList()
    private var searchKey: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_brand_wise_product)
        initView()
    }

    private fun initView() {
        driveActivityName=this.javaClass.name
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        if (intent.hasExtra(AppIntentExtraKeys.SEARCH_KEY)) {

            searchKey = intent.getStringExtra(AppIntentExtraKeys.SEARCH_KEY)
            productTitle.text = searchKey

        }
    }

    override fun onStart() {
        super.onStart()
        if (isNetworkAvailable())
            callGetProductApi()
    }


    override fun onDestroy() {
        super.onDestroy()
        if (disposable != null)
            disposable!!.dispose()
    }

    private fun setProgressState(visibility: Int) {
        progressBar.visibility = visibility
    }

    private fun callGetProductApi() {
        setProgressState(View.VISIBLE)

        val getSearchProductRequest = HashMap<String, String>()
        getSearchProductRequest["key"] = searchKey
        if(pref!!.getPreferenceStringData(pref!!.KEY_USER_ID).isEmpty())
        getSearchProductRequest["user_id"] = "1"
        else
        getSearchProductRequest["user_id"] = pref!!.getPreferenceStringData(pref!!.KEY_USER_ID)
        getSearchProductRequest["language"] = DrewelApplication.getInstance().getLanguage()


        val getProductObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).getSearchProduct(getSearchProductRequest)
        disposable = getProductObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    DrewelApplication.getInstance().logoutWhenAccountDeactivated(result.response!!.isDeactivate!!,this)
                    setProgressState(View.GONE)

                    if (result.response!!.status!!) {
                        brandWiseProductRecyclerView.visibility = View.VISIBLE
                        productList = result.response!!.data.product
                        setAdapter()


                    } else {
                        com.drewel.drewel.utill.Utils.getInstance().showToast(this,result.response!!.message!!)
                        brandWiseProductRecyclerView.visibility = View.GONE

                    }
                }, { error ->
                    setProgressState(View.GONE)
                    com.drewel.drewel.utill.Utils.getInstance().showToast(this,error.message!!)
                    Log.e("TAG", "{$error.message}")
                }
                )
    }

    private fun setAdapter() {
        if (productAdapter == null) {
            productAdapter = ProductAdapter2(this)
            productAdapter!!.productList = productList
            brandWiseProductRecyclerView.layoutManager = GridLayoutManager(this,2 )
            brandWiseProductRecyclerView.addItemDecoration(EqualSpacingItemDecoration(10, EqualSpacingItemDecoration.GRID))
            brandWiseProductRecyclerView.adapter = productAdapter

        } else {

            productAdapter!!.productList = productList
            productAdapter!!.notifyDataSetChanged()
        }
    }

}