package com.drewel.drewel.activity

//import com.os.drewel.apicall.responsemodel.productdetailresponsemodel.ProductDetailResponse
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.widget.AppCompatTextView
import android.support.v7.widget.LinearLayoutManager
import android.util.DisplayMetrics
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.RelativeLayout
import com.facebook.CallbackManager
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.assist.FailReason
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener
import com.drewel.drewel.R
import com.drewel.drewel.adapter.SimilarProductAdapter
import com.drewel.drewel.adapter.SlidingImageAdapter
import com.drewel.drewel.apicall.DrewelApi
import com.drewel.drewel.apicall.responsemodel.productdetailresponsemodel.ProductDetail
import com.drewel.drewel.apicall.responsemodel.productdetailresponsemodel.ProductDetailResponse
import com.drewel.drewel.application.DrewelApplication
import com.drewel.drewel.constant.AppIntentExtraKeys
import com.drewel.drewel.constant.Constants
import com.drewel.drewel.dialog.ShareBottomSheetDialog
import com.drewel.drewel.prefrences.Prefs
import com.drewel.drewel.rxbus.CartRxJavaBus
import com.drewel.drewel.rxbus.SampleRxJavaBus
import com.drewel.drewel.utill.EqualSpacingItemDecoration
import com.drewel.drewel.utill.Utils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.layout_product_details_activity.*
import kotlinx.android.synthetic.main.product_detail_activity.*
import me.leolin.shortcutbadger.ShortcutBadger
import java.util.*

/**
 * Created by monikab on 3/20/2018.
 */
class ProductDetailActivity : ProductBaseActivity(), View.OnClickListener {

    var disposable: Disposable? = null
    private var productId = ""
    private var productDetailResponse = ProductDetailResponse()
    private val productAdapter = SimilarProductAdapter(this)
    var shareDialog: ShareBottomSheetDialog? = null
    //  var productImagePath = ""
    private var callbackManager = CallbackManager.Factory.create()
    private var productDetail: ProductDetail = ProductDetail()

    private var productImageBitmap: Bitmap? = null
    private var notificationId = ""
    var FROM = 0
    var is_read = ""
    var isCalled = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.product_detail_activity)
        intiView()
    }

    private fun intiView() {
        driveActivityName = this.javaClass.name
        shareDialog = ShareBottomSheetDialog(this, callbackManager)
        shareDialog!!.activity = this
        setSupportActionBar(toolbar)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        if (intent.hasExtra(AppIntentExtraKeys.PRODUCT_ID))
            productId = intent.getStringExtra(AppIntentExtraKeys.PRODUCT_ID)
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
        /* set click listeners for button*/
        setClickListener()
        /*dynamically set height of viewpager*/
        setHeightOfViewPager()


    }

    override fun onResume() {
        super.onResume()
        if (isNetworkAvailable()) {
            progressBar.visibility = View.VISIBLE
            callProductDetailAPi()
        }
    }


    private fun callReadNotificationApi() {
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
    }


    private fun setHeightOfViewPager() {
        val displaymetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displaymetrics)
        val width = displaymetrics.widthPixels

        val linearPram = RelativeLayout.LayoutParams(width, width - 200)
        productImagePager.layoutParams = linearPram
        outOfStockTv.layoutParams = linearPram
    }

    private fun setClickListener() {
        imv_share_product.setOnClickListener(this)
        addToWishList.setOnClickListener(this)
        notifyMeBt.setOnClickListener(this)
        addToCart.setOnClickListener(this)
        txt_write_your_review.setOnClickListener(this)
        addProductQuantityBt.setOnClickListener(this)
        removeProductQuantityBt.setOnClickListener(this)
    }

    var intentShare = Intent();
    public fun prepareShareIntent(show: Boolean) {
        if (show) {
            val sharingIntent = Intent(android.content.Intent.ACTION_SEND)
//            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "https://56.octallabs.com/drewel/product_details/" + productId)
//            sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(MediaStore.Images.Media.insertImage(contentResolver, shareImagePath, "https://56.octallabs.com/drewel/product_details/"  + productId, "https://56.octallabs.com/drewel/product_details/"  + productId)))
            sharingIntent.putExtra(Intent.EXTRA_TEXT, "https://56.octallabs.com/drewel/product_details/" + productId)
            sharingIntent.type = "text/plain";
            startActivity(Intent.createChooser(sharingIntent, "Share"))
        }
    }

    /* on click of buttons*/
    override fun onClick(view: View) {

        when (view.id) {
            R.id.imv_share_product -> {
                if (shareImagePath != null)
                    prepareShareIntent(true)
                else {
                    if (isNetworkAvailable())
                        saveProductImage(true)
                }
            }
            R.id.addToWishList -> {
                if(pref!!.getPreferenceStringData(pref!!.KEY_USER_ID).isEmpty())
                {
                    startActivity(Intent(this@ProductDetailActivity,WelcomeActivity::class.java))
                    finish()
                }
               else if (isNetworkAvailable())
                    callAddToWishListApi(if (productDetail.isWishlist == 0) "1" else "2")
            }
            R.id.notifyMeBt -> {
                if (isNetworkAvailable())
                    callNotifyMeApi(notifyMeBt)
            }

            R.id.addToCart -> {
                if (isNetworkAvailable())
                    addToCartApi(addToCart)
            }
            R.id.txt_write_your_review -> {

                if(pref!!.getPreferenceStringData(pref!!.KEY_USER_ID).isEmpty())
                {
                    startActivity(Intent(this@ProductDetailActivity,WelcomeActivity::class.java))
                    finish()
                    return
                }

                if (!isNetworkAvailable())
                    return
                var intent = Intent(this@ProductDetailActivity, RateProductActivity::class.java)
                intent.putExtra("DATA", productDetail)
                intent.putExtra("CATEGORY", tv_product_categories.text)
                intent.putExtra("SUBCATEGORY", tv_product_sub_categories.text)
//                intent.putExtra("IMAGE",productDetail.productImage!!.get(0) )
                startActivityForResult(intent, 1000)
            }
            R.id.addProductQuantityBt -> {
                if (!isNetworkAvailable())
                    return
                val quantity = productDetail.cartQuantity!!.toInt() + 1
                val price = if (productDetail.offerPrice.isNullOrEmpty())
                    productDetail.avgPrice!!.toDouble() * quantity
                else
                    productDetail.offerPrice!!.toDouble() * quantity

                callUpdateCartApi(quantity.toString(), price.toString())
            }
            R.id.removeProductQuantityBt -> {
                if (!isNetworkAvailable())
                    return
                if (productDetail.cartQuantity!!.toInt() > 1) {
                    val quantity = productDetail.cartQuantity!!.toInt() - 1
                    val price = if (productDetail.offerPrice.isNullOrEmpty())
                        productDetail.avgPrice!!.toDouble() * quantity
                    else
                        productDetail.offerPrice!!.toDouble() * quantity

                    callUpdateCartApi(quantity.toString(), price.toString())
                } else {
                    callDeleteProductFromCartApi()
                    /*Delete Item*/
                }
            }
        }
    }


    private fun callDeleteProductFromCartApi() {
        addProductQuantityBt.isEnabled = false
        removeProductQuantityBt.isEnabled = false
        val pref = Prefs.getInstance(this)

        val deleteProductRequest = java.util.HashMap<String, String>()
        deleteProductRequest["cart_id"] = pref.getPreferenceStringData(pref.KEY_CART_ID)
        deleteProductRequest["user_id"] = pref.getPreferenceStringData(pref.KEY_USER_ID)
        deleteProductRequest["language"] = DrewelApplication.getInstance().getLanguage()
        deleteProductRequest["product_id"] = productId

        val deleteCartProductObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).deleteCartProduct(deleteProductRequest)
        deleteCartProductObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    DrewelApplication.getInstance().logoutWhenAccountDeactivated(result.response!!.isDeactivate!!, this)
                    addProductQuantityBt.isEnabled = true
                    removeProductQuantityBt.isEnabled = true
                    if (result.response!!.status!!) {

                        outOfStockTv.visibility = View.GONE
                        notifyMeBt.visibility = View.GONE
                        rl_add_sub.visibility = View.GONE
                        addToCart.visibility = View.VISIBLE
                        pref.setPreferenceStringData(pref.KEY_CART_ID, result.response!!.data!!.cart!!.cartId!!)

                        CartRxJavaBus.getInstance().cartPublishSubject.onNext(result.response!!.data!!.cart!!.quantity!!)

                    } else {
                        com.drewel.drewel.utill.Utils.getInstance().showToast(this, result.response!!.message!!)
                    }
                }, { error ->
                    addProductQuantityBt.isEnabled = true
                    removeProductQuantityBt.isEnabled = true
                    com.drewel.drewel.utill.Utils.getInstance().showToast(this, error.message!!)
                    Log.e("TAG", "{$error.message}")
                }
                )
    }

    private fun callUpdateCartApi(quantity: String, price: String) {
        addProductQuantityBt.isEnabled = false
        removeProductQuantityBt.isEnabled = false
        val pref = Prefs.getInstance(this)
//        setProgressState(View.VISIBLE, View.INVISIBLE)
        val updateCartRequest = java.util.HashMap<String, String>()
        updateCartRequest["cart_id"] = pref.getPreferenceStringData(pref.KEY_CART_ID)
        updateCartRequest["user_id"] = pref.getPreferenceStringData(pref.KEY_USER_ID)
        updateCartRequest["language"] = DrewelApplication.getInstance().getLanguage()
        updateCartRequest["product_id"] = productId
        updateCartRequest["quantity"] = quantity
        updateCartRequest["price"] = price

        val updateCartObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).updateCart(updateCartRequest)
        updateCartObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    addProductQuantityBt.isEnabled = true
                    DrewelApplication.getInstance().logoutWhenAccountDeactivated(result.response!!.isDeactivate!!, this)
                    addProductQuantityBt.isEnabled = true
                    removeProductQuantityBt.isEnabled = true
                    if (result.response!!.status!!) {
                        productDetail.cartQuantity = quantity.toInt()
//                      notifyItemChanged(position)
                        val quantity = productDetail.cartQuantity!!.toInt()
                        productQuantityTv.setText(productDetail.cartQuantity.toString())
//                      cartItemClickSubject.onNext(quantity)
                        pref!!.setPreferenceStringData(pref!!.KEY_CART_ID, result.response!!.data!!.cart!!.cartId!!)
                        CartRxJavaBus.getInstance().cartPublishSubject.onNext(result.response!!.data!!.cart!!.quantity!!)
                    } else {
//                      notifyItemChanged(position)
                        com.drewel.drewel.utill.Utils.getInstance().showToast(this, result.response!!.message!!)
                    }
                }, { error ->
                    addProductQuantityBt.isEnabled = true
                    removeProductQuantityBt.isEnabled = true
                    addProductQuantityBt.isEnabled = true
//                    notifyItemChanged(position)
                    com.drewel.drewel.utill.Utils.getInstance().showToast(this, error.message!!)
                    Log.e("TAG", "{$error.message}")
                }
                )
    }

    /* call api to get detail of product*/
    private fun callProductDetailAPi() {
        setProgressState(View.VISIBLE, View.INVISIBLE)
        val productDetailRequest = HashMap<String, String>()
        productDetailRequest["language"] = DrewelApplication.getInstance().getLanguage()
        if(pref!!.getPreferenceStringData(pref!!.KEY_USER_ID).isEmpty())
        productDetailRequest["user_id"] = "1"
        else
        productDetailRequest["user_id"] = pref!!.getPreferenceStringData(pref!!.KEY_USER_ID)
        productDetailRequest["product_id"] = productId
        val signUpObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).getProductDetail(productDetailRequest)
        disposable = signUpObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    setProgressState(View.GONE, View.VISIBLE)
                    DrewelApplication.getInstance().logoutWhenAccountDeactivated(result.response!!.isDeactivate!!, this)
                    if (result.response!!.status!!) {
                        productDetailResponse = result
                        setData()
                    } else
                        com.drewel.drewel.utill.Utils.getInstance().showToast(this, result.response!!.message!!)
                }, { error ->
                    setProgressState(View.GONE, View.GONE)
                    com.drewel.drewel.utill.Utils.getInstance().showToast(this, error.message!!)
                    Log.e("TAG", "{$error}")
                }
                )
    }


    /* after getting response from api set data to fields*/
    private fun setData() {

        productDetail = productDetailResponse.response!!.data!!.product!!

        /* mange product out of stock*/
        if (productDetail.outOfStock == 1) {
            outOfStockTv.visibility = View.VISIBLE
            notifyMeBt.visibility = View.VISIBLE
            addToCart.visibility = View.GONE
            rl_add_sub.visibility = View.GONE
        } else {
            outOfStockTv.visibility = View.GONE
            notifyMeBt.visibility = View.GONE
            addToCart.visibility = View.VISIBLE
            rl_add_sub.visibility = View.GONE
            if (productDetail.isAddedToCart == 1) {
                rl_add_sub.visibility = View.VISIBLE

                addToCart.visibility = View.GONE
            }
        }
        productQuantityTv.setText(productDetail.cartQuantity!!.toString())
        productImagePager.adapter = SlidingImageAdapter(this, productDetail.productImage!!)
        pageIndicatorView.setViewPager(productImagePager)
        if (DrewelApplication.getInstance().getLanguage().equals(Constants.LANGUAGE_ENGLISH)) {
            tv_product_title.text = productDetail.productName
            tv_product_desc.text = productDetail.productDescription
            txt_toolbar.text = productDetail.productName
        } else {
            tv_product_title.text = productDetail.ar_product_name
            tv_product_desc.text = productDetail.ar_product_description
            txt_toolbar.text = productDetail.ar_product_name
        }


        var amount = ""

        if (!productDetail.offerPrice.isNullOrEmpty()) {
            original_price_layout.visibility = View.VISIBLE
            tv_offer_expire_on.visibility = View.VISIBLE
            if (!productDetail.avgPrice.isNullOrEmpty()) {
                amount = String.format("%.3f", productDetail.avgPrice!!.toDouble()) + " " + getString(R.string.omr)
                tv_original_amount.text = amount
            }
            amount = String.format("%.3f", productDetail.offerPrice!!.toDouble()) + " " + getString(R.string.omr)
            tv_product_amount.text = amount
            val offerExpireDate = getString(R.string.offer_expire_on) + " " + Utils.getInstance().convertTimeFormatAndTimeZone(productDetail.offerExpiresOn
                    ?: "", "yyyy-MM-dd", "dd MMM, yyyy")
            tv_offer_expire_on.text = offerExpireDate
        } else {
            tv_offer_expire_on.visibility = View.GONE
            original_price_layout.visibility = View.GONE
            if (!productDetail.avgPrice.isNullOrEmpty()) {
                amount = String.format("%.3f", productDetail.avgPrice!!.toDouble()) + " " + getString(R.string.omr)
                tv_product_amount.text = amount
            }
        }
        var weight: String = ""
     /*   if (productDetail.weightIn!!.equals("Ml",true))
            weight = getString(R.string.weight) + " - " + productDetail.weight + " " + getString(R.string.Ml)
        else if (productDetail.weightIn!!.equals("Kg",true))
            weight = getString(R.string.weight) + " - " + productDetail.weight + " " + getString(R.string.Kg)
        else if (productDetail.weightIn!!.equals("Lt",true))
            weight = getString(R.string.weight) + " - " + productDetail.weight + " " + getString(R.string.Lt)
        else if (productDetail.weightIn!!.equals("Gm",true))*/
            weight = getString(R.string.weight) + " - " + productDetail.weight + " " + productDetail.weightIn!!

//        val weight = getString(R.string.weight) + " - " + productDetail.weight + " " + productDetail.weightIn
        tv_product_weight.text = weight
        addToWishList.text = if (productDetail.isWishlist == 0) getString(R.string.add_to_wish_list) else getString(R.string.added_to_wish_list)
        if (!productDetail.avgRating.isNullOrEmpty())
            tv_rating.text = String.format("%.1f", productDetail.avgRating!!.toDouble()) ?: "0"
        else
            tv_rating.text = "0"
        ratingBar.rating = productDetail.avgRating?.toFloat() ?: 0.0F

        val brand = getString(R.string.brand) + " - " + productDetail.brandName
        if (DrewelApplication.getInstance().getLanguage().equals(Constants.LANGUAGE_ENGLISH)) {
            val brand = getString(R.string.brand) + " - " + productDetail.brandName
            tv_product_brand.text = brand
        } else {
            val brand = getString(R.string.brand) + " - " + productDetail.ar_brand_name
            tv_product_brand.text = brand
        }

        showProductCategory()

        if (productDetailResponse.response!!.data!!.relatedProducts!!.isNotEmpty()) {
            val llm = LinearLayoutManager(this)
            llm.orientation = LinearLayoutManager.HORIZONTAL
            similarProductRecyclerView.layoutManager = llm
            similarProductRecyclerView.addItemDecoration(EqualSpacingItemDecoration(16, EqualSpacingItemDecoration.HORIZONTAL))
            similarProductRecyclerView.adapter = productAdapter
            productAdapter.productList = productDetailResponse.response!!.data!!.relatedProducts!!
            productAdapter.notifyDataSetChanged()
        } else {
            similarProductTv.visibility = View.GONE
            similarProductRecyclerView.visibility = View.GONE
        }

        if (DrewelApplication.getInstance().getLanguage().equals(Constants.LANGUAGE_ENGLISH)) {
            shareDialog!!.productTitle = productDetail.productName!!
        } else {
            shareDialog!!.productTitle = productDetail.ar_product_name!!
        }
        shareDialog!!.productPrice = amount

        shareDialog!!.shareImageURL = productDetailResponse.response!!.data!!.product!!.productImage!![0]
        saveProductImage(false)
    }

    private fun showProductCategory() {
        var category = ""
        var subCategory = ""
        for (i in productDetail.category!!.indices) {
            category += if (i == productDetail.category!!.size - 1) {

                if (DrewelApplication.getInstance().getLanguage().equals(Constants.LANGUAGE_ENGLISH)) {
                    productDetail.category!![i].categoryName!!
                } else {
                    productDetail.category!![i].ar_category_name!!
                }
            } else {
                if (DrewelApplication.getInstance().getLanguage().equals(Constants.LANGUAGE_ENGLISH)) {
                    productDetail.category!![i].categoryName!! + ", "
                } else {
                    productDetail.category!![i].ar_category_name!! + ", "
                }
            }
        }
        tv_product_categories.text = category

        if (productDetail.subCategory?.isEmpty() != false) {
            tv_product_sub_categories.visibility = View.GONE
        } else {
            for (i in productDetail.subCategory!!.indices) {
                subCategory += if (i == productDetail.subCategory!!.size - 1) {

                    if (DrewelApplication.getInstance().getLanguage().equals(Constants.LANGUAGE_ENGLISH)) {
                        productDetail.subCategory!![i].categoryName!!
                    } else {
                        productDetail.subCategory!![i].ar_category_name!!
                    }
                } else {
                    if (DrewelApplication.getInstance().getLanguage().equals(Constants.LANGUAGE_ENGLISH)) {
                        productDetail.subCategory!![i].categoryName!! + ", "
                    } else {
                        productDetail.subCategory!![i].ar_category_name!! + ", "
                    }
                }
                tv_product_sub_categories.text = subCategory
                if (subCategory.isBlank()) {
                    tv_product_sub_categories.visibility = View.GONE
                } else {
                    tv_product_sub_categories.visibility = View.VISIBLE
                    tv_product_sub_categories.text = "> " + subCategory
                }
            }
        }
    }

    private fun saveProductImage(isShow: Boolean) {

        ImageLoader.getInstance().loadImage(productDetailResponse.response!!.data!!.product!!.productImage!![0], object : ImageLoadingListener {
            override fun onLoadingComplete(p0: String?, p1: View?, p2: Bitmap?) {
                productImageBitmap = p2
                checkRequestPermission(isShow)
//
// productImageBitmap

            }

            override fun onLoadingStarted(p0: String?, p1: View?) {

            }

            override fun onLoadingCancelled(p0: String?, p1: View?) {
            }

            override fun onLoadingFailed(p0: String?, p1: View?, p2: FailReason?) {

            }

        })

    }

    /* handle progress dialog visibility*/
    private fun setProgressState(visibility: Int, viewVisibility: Int) {
        progressBar.visibility = visibility
        detailLayout.visibility = viewVisibility
    }


    override fun onDestroy() {
        super.onDestroy()
        /* stop api calling when user press back button*/
        if (disposable != null)
            disposable!!.dispose()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1000) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                var isSubmitted = data.getIntExtra("isSubmitted", 0)
                productDetail.review_submited = isSubmitted
                if (isNetworkAvailable()) {
                    progressBar.visibility = View.VISIBLE
                    callProductDetailAPi()
                }
            }
        } else
            callbackManager.onActivityResult(requestCode, resultCode, data)

    }


    private fun callAddToWishListApi(flag: String) {
        addToWishList.isEnabled = false
        val addToWhishListRequest = HashMap<String, String>()
        if (pref!!.getPreferenceStringData(pref!!.KEY_USER_ID).isEmpty())
            addToWhishListRequest["user_id"] = "1"
        else
        addToWhishListRequest["user_id"] = pref!!.getPreferenceStringData(pref!!.KEY_USER_ID)
        addToWhishListRequest["language"] = DrewelApplication.getInstance().getLanguage()
        addToWhishListRequest["product_id"] = productId
        addToWhishListRequest["flag"] = flag
        val defaultAddressObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).addWishlist(addToWhishListRequest)

        defaultAddressObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    addToWishList.isEnabled = true
                    DrewelApplication.getInstance().logoutWhenAccountDeactivated(result.response!!.isDeactivate!!, this)
                    if (result.response!!.status!!) {
                        com.drewel.drewel.utill.Utils.getInstance().showToast(this, result.response!!.message!!)
                        productDetail.isWishlist = if (flag == "2") 0 else 1
                        addToWishList.text = if (flag == "2") getString(R.string.add_to_wish_list) else getString(R.string.added_to_wish_list)
                        SampleRxJavaBus.getInstance().objectPublishSubject.onNext(if (flag == "2") 0 else 1)
                    } else
                        com.drewel.drewel.utill.Utils.getInstance().showToast(this, result.response!!.message!!)
                }, { error ->
                    addToWishList.isEnabled = true
                    com.drewel.drewel.utill.Utils.getInstance().showToast(this, error.message!!)
                    Log.e("TAG", "{$error.message}")
                }
                )
    }


    private fun callNotifyMeApi(notifyMeButton: AppCompatTextView) {
        notifyMeButton.isEnabled = false
        val notifyMeRequest = HashMap<String, String>()
        notifyMeRequest["user_id"] = pref!!.getPreferenceStringData(pref!!.KEY_USER_ID)
        notifyMeRequest["language"] = DrewelApplication.getInstance().getLanguage()
        notifyMeRequest["product_id"] = productDetail.productId!!
        val notifyMeObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).productNotify(notifyMeRequest)

        notifyMeObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    DrewelApplication.getInstance().logoutWhenAccountDeactivated(result.response!!.isDeactivate!!, this)
                    notifyMeButton.isEnabled = true
                    com.drewel.drewel.utill.Utils.getInstance().showToast(this, result.response!!.message!!)
                }, { error ->
                    notifyMeButton.isEnabled = true
                    // setProgressState(View.GONE, true)
                    com.drewel.drewel.utill.Utils.getInstance().showToast(this, error.message!!)
                    Log.e("TAG", "{$error.message}")
                }
                )
    }

    var shouldCall = false
    private fun addToCartApi(addToCartButton: AppCompatTextView) {

        if(pref!!.getPreferenceStringData(pref!!.KEY_USER_ID).isEmpty())
        {
            startActivity(Intent(this@ProductDetailActivity,WelcomeActivity::class.java))
            finish()
            return
        }
        addToCartButton.isEnabled = false
        val removeFromWhishListRequest = HashMap<String, String>()
        removeFromWhishListRequest["user_id"] = pref!!.getPreferenceStringData(pref!!.KEY_USER_ID)
        removeFromWhishListRequest["language"] = DrewelApplication.getInstance().getLanguage()
        removeFromWhishListRequest["product_id"] = productDetail.productId!!
        removeFromWhishListRequest["cart_id"] = pref!!.getPreferenceStringData(pref!!.KEY_CART_ID)
        removeFromWhishListRequest["quantity"] = "1"
        if (productDetail.offerPrice.isNullOrEmpty())
            removeFromWhishListRequest["price"] = productDetail.avgPrice!!
        else
            removeFromWhishListRequest["price"] = productDetail.offerPrice!!

        val defaultAddressObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).addToCart(removeFromWhishListRequest)

        defaultAddressObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    DrewelApplication.getInstance().logoutWhenAccountDeactivated(result.response!!.isDeactivate!!, this)
                    addToCartButton.isEnabled = true
                    productQuantityTv.setText("1")
                    outOfStockTv.visibility = View.GONE
                    notifyMeBt.visibility = View.GONE
                    rl_add_sub.visibility = View.VISIBLE
                    addToCart.visibility = View.GONE
                    // setProgressState(View.GONE, true)
                    com.drewel.drewel.utill.Utils.getInstance().showToast(this, result.response!!.message!!)

                    if (result.response!!.status!!) {
                        productDetail.cartQuantity = 1
                        pref!!.setPreferenceStringData(pref!!.KEY_CART_ID, result.response!!.data!!.cart!!.cartId!!)
                        CartRxJavaBus.getInstance().cartPublishSubject.onNext(result.response!!.data!!.cart!!.quantity!!)
                    }
                }, { error ->
                    addToCartButton.isEnabled = true
                    // setProgressState(View.GONE, true)
                    com.drewel.drewel.utill.Utils.getInstance().showToast(this, error.message!!)
                    Log.e("TAG", "{$error.message}")
                }
                )
    }

    private var shareImagePath: String? = ""

    private fun checkRequestPermission(show: Boolean) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        } else {
            shareImagePath = Utils.getInstance().saveBitmapToExternalStorage(productImageBitmap, "share")
            if (show)
                prepareShareIntent(show)
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                shareImagePath = Utils.getInstance().saveBitmapToExternalStorage(productImageBitmap, "share")
            } else {

            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
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
}