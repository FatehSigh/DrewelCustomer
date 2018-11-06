package com.os.drewel.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.opengl.Visibility
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatImageButton
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.RecyclerView
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import com.blankj.utilcode.util.NetworkUtils
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.assist.FailReason
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener
import com.os.drewel.R
import com.os.drewel.activity.BaseActivity
import com.os.drewel.activity.ProductDetailActivity
import com.os.drewel.apicall.DrewelApi
import com.os.drewel.apicall.responsemodel.Product
import com.os.drewel.application.DrewelApplication
import com.os.drewel.constant.AppIntentExtraKeys
import com.os.drewel.constant.Constants
import com.os.drewel.prefrences.Prefs
import com.os.drewel.rxbus.CartRxJavaBus
import com.os.drewel.rxbus.SampleRxJavaBus
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.product_list_all_child.view.*
import java.text.NumberFormat

/**
 * Created by sharukhb on 3/13/2018.
 */

class ProductAdapter(val mContext: Context, internal var visibility: Int) : RecyclerView.Adapter<ProductAdapter.CategoryHolder>() {

    var productList: List<Product> = ArrayList()
    var imageViewHeight = 0
    var imageViewWidth = 0
    val pref = Prefs.getInstance(mContext)
    var clickPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.product_list_all_child, parent, false)
        return CategoryHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryHolder, position: Int) {

        val linearPram = LinearLayout.LayoutParams(imageViewWidth, LinearLayout.LayoutParams.WRAP_CONTENT)
        holder.itemView.productListRootLL.layoutParams = linearPram
//        val linearPram = LinearLayout.LayoutParams(imageViewWidth, imageViewHeight)
//        holder.itemView.productListRootLL.layoutParams = linearPram
//        val relativePram = RelativeLayout.LayoutParams(imageViewWidth, imageViewHeight-150)
//        holder.itemView.imv_product.layoutParams = relativePram

        ImageLoader.getInstance().displayImage(productList[position].productImage, holder.itemView.imv_product, DrewelApplication.getInstance().options)
        if (DrewelApplication.getInstance().getLanguage().equals(Constants.LANGUAGE_ENGLISH)) {
            holder.itemView.tv_product_title.text = productList[position].productName
        } else {
            holder.itemView.tv_product_title.text = productList[position].ar_product_name
        }


        if (!productList[position].offerPrice.isNullOrEmpty()) {
            holder.itemView.original_price_layout.visibility = View.VISIBLE
            if (!productList[position].avgPrice.isNullOrEmpty()) {
                val amount = String.format("%.3f", productList[position].avgPrice!!.toDouble()) + " " + mContext.getString(R.string.omr)
                holder.itemView.tv_original_amount.text = amount
            }
            val amount = String.format("%.3f", productList[position].offerPrice!!.toDouble()) + " " + mContext.getString(R.string.omr)
            holder.itemView.tv_product_amount.text = amount
        } else {
            holder.itemView.original_price_layout.visibility = View.GONE
            if (!productList[position].avgPrice.isNullOrEmpty()) {
                val amount = String.format("%.3f", productList[position].avgPrice!!.toDouble()) + " " + mContext.getString(R.string.omr)
                holder.itemView.tv_product_amount.text = amount
            }
        }
        if (productList[position].weightIn!!.equals("Ml"))
            holder.itemView.tv_quantity.text = productList[position].weight + " " + mContext.getString(R.string.Ml)
        else if (productList[position].weightIn!!.equals("Kg"))
            holder.itemView.tv_quantity.text = productList[position].weight + " " + mContext.getString(R.string.Kg)
        else if (productList[position].weightIn!!.equals("Lt"))
            holder.itemView.tv_quantity.text = productList[position].weight + " " + mContext.getString(R.string.Lt)
        else if (productList[position].weightIn!!.equals("Gm"))
            holder.itemView.tv_quantity.text = productList[position].weight + " " + mContext.getString(R.string.Gm)

//        holder.itemView.tv_quantity.text = weight
        holder.itemView.tag = position
        /* if product is out of stock*/
        if (productList[position].outOfStock == 1) {
            holder.itemView.outOfStockTv.visibility = View.VISIBLE
            holder.itemView.notifyMeBt.visibility = View.VISIBLE
            holder.itemView.imv_add_product.visibility = View.GONE
        } else {
            holder.itemView.outOfStockTv.visibility = View.GONE
            holder.itemView.notifyMeBt.visibility = View.GONE
            holder.itemView.imv_add_product.visibility = View.VISIBLE
        }

        if (productList[position].isWishlist == null || productList[position].isWishlist == 0)
            holder.itemView.addToWishList.setColorFilter(ContextCompat.getColor(mContext, R.color.buttonUnselectedColor))
        else
            holder.itemView.addToWishList.setColorFilter(ContextCompat.getColor(mContext, R.color.buttonOrangeColor))

        holder.itemView.setOnClickListener({ view ->
            clickPosition = holder.layoutPosition
            val intent = Intent(mContext, ProductDetailActivity::class.java)
            intent.putExtra(AppIntentExtraKeys.PRODUCT_ID, productList[view.tag as Int].productId)
            mContext.startActivity(intent)
        })


        holder.itemView.addToWishList.setOnClickListener({

            var flag = ""

            flag = if (productList[holder.layoutPosition].isWishlist == null || productList[holder.layoutPosition].isWishlist == 0)
                "1"
            else
                "2"
            if (NetworkUtils.isConnected()) {
                callAddToWishListApi(holder.layoutPosition, holder.itemView.addToWishList, flag)
            } else com.os.drewel.utill.Utils.getInstance().showToast(mContext, mContext.getString(R.string.error_network_connection))

        })


        holder.itemView.imv_add_product.setOnClickListener({
            if (NetworkUtils.isConnected()) {
                addToCartApi(holder.layoutPosition, holder.itemView.imv_add_product)
            } else com.os.drewel.utill.Utils.getInstance().showToast(mContext, mContext.getString(R.string.error_network_connection))

        })


        holder.itemView.notifyMeBt.setOnClickListener({
            if (NetworkUtils.isConnected()) {
                callNotifyMeApi(holder.layoutPosition, holder.itemView.notifyMeBt)
            } else com.os.drewel.utill.Utils.getInstance().showToast(mContext, mContext.getString(R.string.error_network_connection))

        })
    }


    override fun getItemCount(): Int {
        return productList.size;
    }


    fun productList(data: List<Product>) {
        if (productList !== data) {
            productList = data
            notifyDataSetChanged()
        }
    }


    inner class CategoryHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        init {
            val displaymetrics = DisplayMetrics()
            (mContext as AppCompatActivity).windowManager.defaultDisplay.getMetrics(displaymetrics)
            val width = displaymetrics.widthPixels
            imageViewWidth = (width / 2) - 80
            val height = displaymetrics.heightPixels
            var value = com.os.drewel.utill.Utils.getInstance().dpToPix(mContext, 60)
            if (visibility == View.VISIBLE) {
                value = com.os.drewel.utill.Utils.getInstance().dpToPix(mContext, 150)
                imageViewHeight = (height) / 2 - 220
            } else
                imageViewHeight = (height) / 2 - 150
            value = com.os.drewel.utill.Utils.getInstance().dpToPix(mContext, 100)
            Log.e("Height==>", value.toString())
//            val height = displaymetrics.heightPixels
//            imageViewHeight = (height / 2) -100
            /* if user add or remove product from detail activity then change it in adapter also*/
            SampleRxJavaBus.getInstance().objectPublishSubject.subscribe({ addToWishList ->
                productList[clickPosition].isWishlist = addToWishList as Int

                notifyItemChanged(clickPosition)
            })
        }

    }

    @SuppressLint("CheckResult")
    private fun callAddToWishListApi(position: Int, addToWishList: AppCompatImageView, flag: String) {
        addToWishList.isEnabled = false
        val addToWhishListRequest = HashMap<String, String>()
        addToWhishListRequest["user_id"] = pref.getPreferenceStringData(pref.KEY_USER_ID)
        addToWhishListRequest["language"] = DrewelApplication.getInstance().getLanguage()
        addToWhishListRequest["product_id"] = productList[position].productId!!
        addToWhishListRequest["flag"] = flag
        val defaultAddressObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).addWishlist(addToWhishListRequest)

        defaultAddressObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->

                    DrewelApplication.getInstance().logoutWhenAccountDeactivated(result.response!!.isDeactivate!!, mContext)

                    addToWishList.isEnabled = true
                    // setProgressState(View.GONE, true)
                    com.os.drewel.utill.Utils.getInstance().showToast(mContext, result.response!!.message!!)
                    if (result.response!!.status!!) {

                        productList[position].isWishlist = if (flag.equals("2")) 0 else 1

                        notifyItemChanged(position)

                    }

                }, { error ->
                    addToWishList.isEnabled = true
                    // setProgressState(View.GONE, true)
                    com.os.drewel.utill.Utils.getInstance().showToast(mContext, error.message!!)
                    Log.e("TAG", "{$error.message}")
                }
                )
    }


    @SuppressLint("CheckResult")
    private fun callNotifyMeApi(position: Int, notifyMeButton: AppCompatImageButton) {
        notifyMeButton.isEnabled = false
        val notifyMeRequest = HashMap<String, String>()
        notifyMeRequest.put("user_id", pref.getPreferenceStringData(pref.KEY_USER_ID))
        notifyMeRequest.put("language", DrewelApplication.getInstance().getLanguage())
        notifyMeRequest.put("product_id", productList[position].productId!!)
        val notifyMeObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).productNotify(notifyMeRequest)

        notifyMeObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    DrewelApplication.getInstance().logoutWhenAccountDeactivated(result.response!!.isDeactivate!!, mContext)
                    notifyMeButton.isEnabled = true
                    // setProgressState(View.GONE, true)
                    com.os.drewel.utill.Utils.getInstance().showToast(mContext, result.response!!.message!!)
                    /* if (result.response!!.status!!) {
                         productList[position].isWishlist = if (flag.equals("2")) 0 else 1
                         notifyItemChanged(position)
                     }*/
                }, { error ->
                    notifyMeButton.isEnabled = true
                    // setProgressState(View.GONE, true)
                    com.os.drewel.utill.Utils.getInstance().showToast(mContext, error.message!!)
                    Log.e("TAG", "{$error.message}")
                }
                )
    }


    @SuppressLint("CheckResult")
    private fun addToCartApi(position: Int, addToCartButton: AppCompatImageButton) {
        addToCartButton.isEnabled = false
        val removeFromWhishListRequest = java.util.HashMap<String, String>()
        removeFromWhishListRequest["user_id"] = pref.getPreferenceStringData(pref.KEY_USER_ID)
        removeFromWhishListRequest["language"] = DrewelApplication.getInstance().getLanguage()
        removeFromWhishListRequest["product_id"] = productList[position].productId!!
        removeFromWhishListRequest["cart_id"] = pref.getPreferenceStringData(pref.KEY_CART_ID)
        removeFromWhishListRequest["quantity"] = "1"
        if (productList[position].offerPrice.isNullOrEmpty())
            removeFromWhishListRequest["price"] = productList[position].avgPrice!!
        else
            removeFromWhishListRequest["price"] = productList[position].offerPrice!!

        val defaultAddressObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).addToCart(removeFromWhishListRequest)

        defaultAddressObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    addToCartButton.isEnabled = true
                    // setProgressState(View.GONE, true)
                    com.os.drewel.utill.Utils.getInstance().showToast(mContext, result.response!!.message!!)

                    if (result.response!!.status!!) {
                        pref.setPreferenceStringData(pref.KEY_CART_ID, result.response!!.data!!.cart!!.cartId!!)
                        CartRxJavaBus.getInstance().cartPublishSubject.onNext(result.response!!.data!!.cart!!.quantity!!)
                    }
                }, { error ->
                    addToCartButton.isEnabled = true
                    // setProgressState(View.GONE, true)
                    com.os.drewel.utill.Utils.getInstance().showToast(mContext, error.message!!)
                    Log.e("TAG", "{$error.message}")
                }
                )
    }


}