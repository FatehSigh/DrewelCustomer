package com.os.drewel.adapter

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatButton
import android.support.v7.widget.AppCompatImageButton
import android.support.v7.widget.RecyclerView
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import com.nostra13.universalimageloader.core.ImageLoader
import com.os.drewel.R
import com.os.drewel.activity.ProductDetailActivity
import com.os.drewel.apicall.DrewelApi
import com.os.drewel.apicall.responsemodel.Product
import com.os.drewel.application.DrewelApplication
import com.os.drewel.constant.AppIntentExtraKeys
import com.os.drewel.prefrences.Prefs
import com.os.drewel.rxbus.CartRxJavaBus
import com.os.drewel.rxbus.SampleRxJavaBus
import com.os.drewel.utill.CommonUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.wish_list_product_item_.view.*
import java.text.NumberFormat
import java.util.*

/**
 * Created by sharukhb on 3/13/2018.
 */

class WishlistAdapter(val mContext: Context, var wishList: MutableList<Product>) : RecyclerView.Adapter<WishlistAdapter.WishlistHolder>() {

    var imageViewHeight = 0
    val pref = Prefs.getInstance(mContext)
    private var clickPosition = 0
    var progressDialog: ProgressDialog? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WishlistHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.wish_list_product_item_, parent, false)
        return WishlistHolder(view)
    }

    override fun onBindViewHolder(holder: WishlistHolder, position: Int) {

        val linearPram = LinearLayout.LayoutParams(imageViewHeight, LinearLayout.LayoutParams.WRAP_CONTENT)
        holder.itemView.productListRootLL.layoutParams = linearPram

        val relativePram = RelativeLayout.LayoutParams(imageViewHeight, imageViewHeight)
        holder.itemView.imv_product.layoutParams = relativePram

        ImageLoader.getInstance().displayImage(wishList[position].productImage, holder.itemView.imv_product, DrewelApplication.getInstance().options)
        holder.itemView.tv_product_title.text = wishList[position].productName

        /* val amount = NumberFormat.getInstance().format(wishList[position].avgPrice!!.toDouble()) + " " + mContext.getString(R.string.omr)
         holder.itemView.tv_product_amount.text = amount*/


        if (!wishList[position].offerPrice.isNullOrEmpty()) {
            holder.itemView.original_price_layout.visibility = View.VISIBLE
            if (!wishList[position].avgPrice.isNullOrEmpty()) {
                val amount = NumberFormat.getInstance().format(wishList[position].avgPrice!!.toDouble()) + " " + mContext.getString(R.string.omr)
                holder.itemView.tv_original_amount.text = amount
            }
            val amount = NumberFormat.getInstance().format(wishList[position].offerPrice!!.toDouble()) + " " + mContext.getString(R.string.omr)
            holder.itemView.tv_product_amount.text = amount
        } else {
            holder.itemView.original_price_layout.visibility = View.GONE
            if (!wishList[position].avgPrice.isNullOrEmpty()) {
                val amount = NumberFormat.getInstance().format(wishList[position].avgPrice!!.toDouble()) + " " + mContext.getString(R.string.omr)
                holder.itemView.tv_product_amount.text = amount
            }
        }

        val weight = wishList[position].weight + " " + wishList[position].weightIn
        holder.itemView.tv_quantity.text = weight
        holder.itemView.tag = position


        holder.itemView.setOnClickListener({ view ->
            clickPosition = holder.layoutPosition
            val intent = Intent(mContext, ProductDetailActivity::class.java)
            intent.putExtra(AppIntentExtraKeys.PRODUCT_ID, wishList[view.tag as Int].productId)
            mContext.startActivity(intent)
        })

        holder.itemView.removeFromWishListBt.setOnClickListener({
            if (holder.layoutPosition >= 0)
                callRemoveFromWishListApi(holder.layoutPosition, holder.itemView.removeFromWishListBt, "2")
        })

        holder.itemView.moveToCartBt.setOnClickListener({
            if (wishList[holder.layoutPosition].outOfStock == 0) {
                moveToCartApi(holder.layoutPosition, holder.itemView.moveToCartBt)
            }
        })


        if (wishList[position].outOfStock == 1) {
            holder.itemView.moveToCartBt.text = mContext.getString(R.string.out_of_stock)
            holder.itemView.moveToCartBt.setTextColor(ContextCompat.getColor(mContext, R.color.colorRed))
        } else {
            holder.itemView.moveToCartBt.text = mContext.getString(R.string.move_to_cart)
            holder.itemView.moveToCartBt.setTextColor(ContextCompat.getColor(mContext, R.color.black))
        }


    }


    override fun getItemCount(): Int {
        return wishList.size
    }

    inner class WishlistHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        init {
            val displayMetrics = DisplayMetrics()
            (mContext as AppCompatActivity).windowManager.defaultDisplay.getMetrics(displayMetrics)
            val width = displayMetrics.widthPixels
            imageViewHeight = (width / 2) - 32

        }

    }

    private fun moveToCartApi(position: Int, moveToCartButton: AppCompatButton) {
        progressDialog = CommonUtil.showLoadingDialog(mContext)

        moveToCartButton.isEnabled = false
        val removeFromWhishListRequest = HashMap<String, String>()
        removeFromWhishListRequest["user_id"] = pref.getPreferenceStringData(pref.KEY_USER_ID)
        removeFromWhishListRequest["language"] = DrewelApplication.getInstance().getLanguage()
        removeFromWhishListRequest["product_id"] = wishList[position].productId!!
//        removeFromWhishListRequest["wishlist_id"] = wishList[position].wishlistId!!
        removeFromWhishListRequest["quantity"] = "1"
        if (wishList[position].offerPrice.isNullOrEmpty())
            removeFromWhishListRequest["price"] = wishList[position].avgPrice!!
        else
            removeFromWhishListRequest["price"] = wishList[position].offerPrice!!
        val defaultAddressObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).addToCart(removeFromWhishListRequest)

        defaultAddressObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->

                    DrewelApplication.getInstance().logoutWhenAccountDeactivated(result.response!!.isDeactivate!!, mContext)
                    moveToCartButton.isEnabled = true
                    // setProgressState(View.GONE, true)
                    Toast.makeText(mContext, result.response!!.message, Toast.LENGTH_LONG).show()

                    if (result.response!!.status!!) {
                        pref.setPreferenceStringData(pref.KEY_CART_ID, result.response!!.data!!.cart!!.cartId!!)
                        CartRxJavaBus.getInstance().cartPublishSubject.onNext(result.response!!.data!!.cart!!.quantity!!)

                        SampleRxJavaBus.getInstance().objectPublishSubject.onNext(0)
//                        wishList.removeAt(position)
//                        notifyItemRemoved(clickPosition)
//                        notifyItemRangeChanged(clickPosition, wishList.size)

                    }
                    progressDialog?.dismiss()
                }, { error ->
                    moveToCartButton.isEnabled = true
                    // setProgressState(View.GONE, true)
                    Toast.makeText(mContext, error.message, Toast.LENGTH_LONG).show()
                    Log.e("TAG", "{$error.message}")
                    progressDialog?.dismiss()
                }
                )
    }


    private fun callRemoveFromWishListApi(position: Int, removeFromWishList: AppCompatImageButton, flag: String) {

        progressDialog = CommonUtil.showLoadingDialog(mContext)
        removeFromWishList.isEnabled = false
        val removeFromWishListRequest = HashMap<String, String>()
        removeFromWishListRequest["user_id"] = pref.getPreferenceStringData(pref.KEY_USER_ID)
        removeFromWishListRequest["language"] = DrewelApplication.getInstance().getLanguage()
        removeFromWishListRequest["product_id"] = wishList[position].productId!!
        removeFromWishListRequest["flag"] = flag
        val defaultAddressObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).addWishlist(removeFromWishListRequest)

        defaultAddressObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    DrewelApplication.getInstance().logoutWhenAccountDeactivated(result.response!!.isDeactivate!!, mContext)

                    // setProgressState(View.GONE, true)
                    Toast.makeText(mContext, result.response!!.message, Toast.LENGTH_LONG).show()

                    if (result.response!!.status!!) {
                        SampleRxJavaBus.getInstance().objectPublishSubject.onNext(0)
                        wishList.removeAt(position)
                        notifyItemRemoved(clickPosition)
                        notifyItemRangeChanged(clickPosition, wishList.size)

                    }
                    removeFromWishList.isEnabled = true
                    progressDialog?.dismiss()
                }, { error ->
                    removeFromWishList.isEnabled = true
                    // setProgressState(View.GONE, true)
                    Toast.makeText(mContext, error.message, Toast.LENGTH_LONG).show()
                    Log.e("TAG", "{$error.message}")
                    progressDialog?.dismiss()
                }
                )
    }

}