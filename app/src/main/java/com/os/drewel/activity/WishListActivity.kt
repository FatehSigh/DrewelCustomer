package com.os.drewel.activity

import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.os.drewel.R
import com.os.drewel.adapter.WishlistAdapter
import com.os.drewel.apicall.DrewelApi
import com.os.drewel.apicall.responsemodel.Product
import com.os.drewel.application.DrewelApplication
import com.os.drewel.utill.EqualSpacingItemDecoration
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_wish_list.*
import kotlinx.android.synthetic.main.content_wishlist.*
import java.util.*

/**
 * Created by monikab on 3/15/2018.
 */
class WishListActivity : ProductBaseActivity() {

    private var wishlistAdapter: WishlistAdapter? = null
    var disposable: Disposable? = null
    private var productList: MutableList<Product> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wish_list)
        initView()
    }

    private fun initView() {
        driveActivityName=this.javaClass.name
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
    }

    override fun onStart() {
        super.onStart()
        if (isNetworkAvailable())
            callGetWishListApi()
    }


    private fun setAdapter() {
        if (wishlistAdapter == null) {
            wishlistAdapter = WishlistAdapter(this, productList)
            wishlistRecyclerView.layoutManager = GridLayoutManager(this,2)
            wishlistRecyclerView.addItemDecoration(EqualSpacingItemDecoration(16, EqualSpacingItemDecoration.GRID))
            wishlistRecyclerView.adapter = wishlistAdapter
        } else {
            wishlistAdapter!!.wishList=productList
            wishlistAdapter!!.notifyDataSetChanged()
        }
    }


    private fun callGetWishListApi() {
        setProgressState(View.VISIBLE)
        val getWishListRequest = HashMap<String, String>()

        getWishListRequest["user_id"] = pref!!.getPreferenceStringData(pref!!.KEY_USER_ID)
        getWishListRequest["language"] = DrewelApplication.getInstance().getLanguage()

        val getProductObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).getWishlist(getWishListRequest)
        disposable = getProductObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    DrewelApplication.getInstance().logoutWhenAccountDeactivated(result.response!!.isDeactivate!!,this)
                    setProgressState(View.GONE)

                    if (result.response!!.status!!) {
                        wishlistRecyclerView.visibility=View.VISIBLE
                        productList = result.response!!.data!!.products!!
                        setAdapter()

                    } else {
                        wishlistRecyclerView.visibility=View.GONE
                        com.os.drewel.utill.Utils.getInstance().showToast(this,result.response!!.message!!)
                    }
                }, { error ->
                    setProgressState(View.GONE)
                    com.os.drewel.utill.Utils.getInstance().showToast(this,error.message!!)
                    Log.e("TAG", "{$error.message}")
                }
                )
    }

    override fun onDestroy() {
        super.onDestroy()
        if (disposable != null)
            disposable!!.dispose()
    }

    private fun setProgressState(visibility: Int) {
        progressBar.visibility = visibility
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }



}