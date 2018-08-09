package com.os.drewel.activity

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.RatingBar
import android.widget.Toast
import com.blankj.utilcode.util.KeyboardUtils
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.os.drewel.R
import com.os.drewel.adapter.NotificationAdapter
import com.os.drewel.adapter.ReviewListAdapter
import com.os.drewel.apicall.DrewelApi
import com.os.drewel.apicall.responsemodel.productdetailresponsemodel.ProductDetail
import com.os.drewel.apicall.responsemodel.reviewResponseModel.Review
import com.os.drewel.application.DrewelApplication
import com.os.drewel.constant.AppIntentExtraKeys
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.content_rate_product_activity.*
import kotlinx.android.synthetic.main.request_product_activity.*

/**
 * Created by monikab on 3/23/2018.
 */
class RateProductActivity : BaseActivity(), View.OnClickListener {
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    var productDetail: ProductDetail? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.rate_product_activity)
        initView()

    }

    private fun initView() {
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        /* when user click on adapter row*/
        productDetail = intent.getParcelableExtra("DATA")
        var category = intent.getStringExtra("CATEGORY")
        var subCategory = intent.getStringExtra("SUBCATEGORY")
        if (productDetail!!.review_submited == 1) {
            myCartChildRL.visibility = GONE
            tv_review.visibility = VISIBLE
            tv_review.text = getString(R.string.review_submitted)
        } else {
            myCartChildRL.visibility = VISIBLE
            tv_review.visibility = GONE
        }

        callProductReviewListAPi()
        tv_product_categories.text = category
        tv_product_sub_categories.text = subCategory
        tv_product_title.text = productDetail!!.productName
        if (productDetail!!.productImage!!.isNotEmpty())
            ImageLoader.getInstance().displayImage(productDetail!!.productImage!!.get(0), productImageIv, DrewelApplication.getInstance().options)
        setClickListeners()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                val intent = Intent()
                intent.putExtra("isSubmitted", productDetail!!.review_submited)
                setResult(Activity.RESULT_OK, intent)
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setClickListeners() {
        DoneBt.setOnClickListener(this)
        ratingBar.onRatingBarChangeListener = RatingBar.OnRatingBarChangeListener { ratingBars, rating, fromUser ->
            //val rateValue = ratingBar.rating.toString()
            ratingBar.rating = rating
        }
    }

    override fun onBackPressed() {
        val intent = Intent()
        intent.putExtra("isSubmitted", productDetail!!.review_submited)
        setResult(Activity.RESULT_OK, intent)
        super.onBackPressed()
    }

    override fun onClick(view: View) {

        when (view.id) {

            R.id.DoneBt -> {

                KeyboardUtils.hideSoftInput(this)
                if (ratingBar.rating == 0f)
                    com.os.drewel.utill.Utils.getInstance().showToast(this,getString(R.string.rate_product_validation))
                else if (tv_product_desc.text.toString().isEmpty())
                    com.os.drewel.utill.Utils.getInstance().showToast(this,getString(R.string.review_product_validation))
                else if (isNetworkAvailable())
                    callRequestProductApi()
                else
                    com.os.drewel.utill.Utils.getInstance().showToast(this,getString(R.string.error_network_connection))
            }
        }
    }

    private fun callRequestProductApi() {
        progressBar.visibility = View.VISIBLE
        val addDeliveryAddressRequest = HashMap<String, String>()
        addDeliveryAddressRequest["user_id"] = pref!!.getPreferenceStringData(pref!!.KEY_USER_ID)
        addDeliveryAddressRequest["language"] = DrewelApplication.getInstance().getLanguage()
        addDeliveryAddressRequest["product_id"] = productDetail!!.productId!!
        addDeliveryAddressRequest["ratings"] = ratingBar.rating.toString()
        addDeliveryAddressRequest["reviews"] = tv_product_desc.text.toString()
        val signUpObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).addRating(addDeliveryAddressRequest)
        compositeDisposable.add(signUpObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    progressBar.visibility = View.GONE
                    if (result.response!!.status!!) {
                        com.os.drewel.utill.Utils.getInstance().showToast(this,result.response!!.message!!)
                        productDetail!!.review_submited = 1
                        myCartChildRL.visibility = GONE
                        tv_review.visibility = VISIBLE
                        tv_review.text = getString(R.string.review_submitted)
                        callProductReviewListAPi()
                    } else {
                    }
                }, { error ->
                    progressBar.visibility = View.GONE
                    com.os.drewel.utill.Utils.getInstance().showToast(this,error.message!!)
                    Log.e("TAG", "{$error.message}")
                }
                ))
    }

    var productId = ""
    var reviewList: List<Review> = ArrayList()
    private fun callProductReviewListAPi() {
        progressBar.visibility = View.VISIBLE
        val productDetailRequest = HashMap<String, String>()
        productDetailRequest["language"] = DrewelApplication.getInstance().getLanguage()
        productDetailRequest["user_id"] = pref!!.getPreferenceStringData(pref!!.KEY_USER_ID)
        productDetailRequest["product_id"] = productDetail!!.productId!!
        val signUpObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).productReviewList(productDetailRequest)
        compositeDisposable.add(signUpObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    progressBar.visibility = View.GONE
                    if (result.response!!.status!!) {
                        reviewList = result.response!!.data!!.reviews!!
                        setAdapter()
                    } else {
                    }
                }, { error ->
                    progressBar.visibility = View.GONE
                    com.os.drewel.utill.Utils.getInstance().showToast(this,error.message!!)
                    Log.e("TAG", "{$error.message}")
                }
                ))
    }

    private var reviewAdapter: ReviewListAdapter? = null
    private fun setAdapter() {
        if (reviewAdapter == null) {
            reviewAdapter = ReviewListAdapter(reviewList!!)
            rv_review.layoutManager = LinearLayoutManager(this)
            rv_review.adapter = reviewAdapter
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }


    override fun onResume() {
        super.onResume()
        KeyboardUtils.hideSoftInput(this)
    }

}