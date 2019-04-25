package com.drewel.drewel.adapter

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.blankj.utilcode.util.NetworkUtils
import com.nostra13.universalimageloader.core.ImageLoader
import com.drewel.drewel.R
import com.drewel.drewel.activity.ProductDetailActivity
import com.drewel.drewel.apicall.DrewelApi
import com.drewel.drewel.apicall.responsemodel.cartdetailresponsemodel.Cart
import com.drewel.drewel.application.DrewelApplication
import com.drewel.drewel.constant.AppIntentExtraKeys
import com.drewel.drewel.constant.Constants
import com.drewel.drewel.prefrences.Prefs
import com.drewel.drewel.rxbus.CartRxJavaBus
import com.drewel.drewel.utill.CommonUtil
import com.drewel.drewel.utill.Utils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.child_my_cart.view.*
import java.util.*

/**
 * Created by sharukhb on 3/13/2018.
 */

class CartItemAdapter(val mContext: Context, val cartIemList: MutableList<Cart>) : RecyclerView.Adapter<CartItemAdapter.CartItemHolder>() {

    lateinit var cartItemClickSubject: PublishSubject<String>
    var isAnyProductOutOfStock = false
    var progressDialog: ProgressDialog? = null
    var height = 0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartItemHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.child_my_cart, parent, false)
        return CartItemHolder(view)
    }

    override fun onBindViewHolder(holder: CartItemHolder, position: Int) {
        ImageLoader.getInstance().displayImage(cartIemList[position].productImage, holder.itemView.productImageIv, DrewelApplication.getInstance().options)

        holder.itemView.productQuantityTv.text = cartIemList[position].quantity
        if (DrewelApplication.getInstance().getLanguage().equals(Constants.LANGUAGE_ENGLISH)) {
            holder.itemView.tv_product_title.text = cartIemList[position].productName
        } else
            holder.itemView.tv_product_title.text = cartIemList[position].ar_product_name

        val cartItem = cartIemList[position]
        val quantity = cartItem.quantity!!.toInt()
        val price = cartItem.productPrice!!.toDouble()
        val totalAmount = price * quantity

        // holder.itemView.tv_product_amount.text = NumberFormat.getInstance().format(totalAmount)+" "+mContext.getString(R.string.omr)
//        if (quantity == 1) {
//            holder.itemView.removeProductQuantityBt.background.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP)
//            holder.itemView.removeProductQuantityBt.isEnabled = true
//        } else {
//            holder.itemView.removeProductQuantityBt.isEnabled = true
//            holder.itemView.removeProductQuantityBt.background.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP)
//        }

        if (!cartIemList[position].offerPrice.isNullOrEmpty()) {
            holder.itemView.original_price_layout.visibility = View.VISIBLE
            holder.itemView.tv_original_amount.text = String.format("%.3f", totalAmount) + " " + mContext.getString(R.string.omr)

            val offerPrice = cartItem.offerPrice!!.toDouble()
            val totalOfferAmount = offerPrice * quantity
            val totalOfferAmountStr = String.format("%.3f", totalOfferAmount.toDouble()) + " " + mContext.getString(R.string.omr)
            holder.itemView.tv_product_amount.text = totalOfferAmountStr

        } else {
            holder.itemView.original_price_layout.visibility = View.GONE
            holder.itemView.tv_product_amount.text = String.format("%.3f", totalAmount) + " " + mContext.getString(R.string.omr)
        }

        if (cartIemList[position].outOfStock == 1) {
            isAnyProductOutOfStock = true
            holder.itemView.outOfStockTv.visibility = View.VISIBLE
            holder.itemView.myCartChildRL.setBackgroundColor(mContext.resources.getColor(R.color.out_of_stock))
        } else {
            holder.itemView.outOfStockTv.visibility = View.GONE
            holder.itemView.myCartChildRL.setBackgroundColor(mContext.resources.getColor(R.color.white))
        }

        holder.itemView.tv_product_categories.text = showProductCategory(cartIemList[position])
        val subcategory = showProductSubcategory(cartIemList[position])


        if (subcategory.isBlank()) {
            holder.itemView.tv_product_sub_categories.visibility = View.GONE
        } else {
            holder.itemView.tv_product_sub_categories.visibility = View.VISIBLE
            holder.itemView.tv_product_sub_categories.text="> "+subcategory
        }

    }


    override fun getItemCount(): Int {
        return cartIemList.size;
    }


    inner class CartItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        init {
            height = itemView.myCartChildRL.height
            itemView.addProductQuantityBt.setOnClickListener(this)
            itemView.removeProductQuantityBt.setOnClickListener(this)
            itemView.imv_product_delete.setOnClickListener(this)
            itemView.setOnClickListener(this)
        }


        override fun onClick(view: View) {
            when (view.id) {
                R.id.addProductQuantityBt -> {
                    val quantity = cartIemList[adapterPosition].quantity!!.toInt() + 1
                    val price = if (cartIemList[adapterPosition].offerPrice.isNullOrEmpty())
                        cartIemList[adapterPosition].productPrice!!.toDouble() * quantity
                    else
                        cartIemList[adapterPosition].offerPrice!!.toDouble() * quantity
                    if (NetworkUtils.isConnected()) {
                        callUpdateCartApi(adapterPosition, quantity.toString(), price.toString(), itemView)
                    } else com.drewel.drewel.utill.Utils.getInstance().showToast(mContext, mContext.getString(R.string.error_network_connection))

                }
                R.id.removeProductQuantityBt -> {
                    if (cartIemList[adapterPosition].quantity!!.toInt() > 1) {
                        val quantity = cartIemList[adapterPosition].quantity!!.toInt() - 1

                        val price = if (cartIemList[adapterPosition].offerPrice.isNullOrEmpty())
                            cartIemList[adapterPosition].productPrice!!.toDouble() * quantity
                        else
                            cartIemList[adapterPosition].offerPrice!!.toDouble() * quantity
                        if (NetworkUtils.isConnected()) {
                            callUpdateCartApi(adapterPosition, quantity.toString(), price.toString(), itemView)
                        } else Utils.getInstance().showToast(mContext, mContext.getString(R.string.error_network_connection))

                    } else {
                        if (NetworkUtils.isConnected()) {
                            callDeleteProductFromCartApi(adapterPosition, itemView)
                        } else Utils.getInstance().showToast(mContext, mContext.getString(R.string.error_network_connection))

                        /*Delete Item*/
                    }
                }

                R.id.imv_product_delete -> {
                    if (NetworkUtils.isConnected()) {
                        callDeleteProductFromCartApi(adapterPosition, itemView)
                    } else Utils.getInstance().showToast(mContext, mContext.getString(R.string.error_network_connection))


                }

                R.id.myCartChildRL -> {
                    val intent = Intent(mContext, ProductDetailActivity::class.java)
                    intent.putExtra(AppIntentExtraKeys.PRODUCT_ID, cartIemList[adapterPosition].productId)
                    mContext.startActivity(intent)
                }
            }
        }
    }

    private fun callUpdateCartApi(position: Int, quantity: String, price: String, addProductQuantityBt: View) {
        addProductQuantityBt.isEnabled = false
        val pref = Prefs.getInstance(mContext)
        progressDialog = CommonUtil.showLoadingDialog(mContext)
        val updateCartRequest = HashMap<String, String>()
        updateCartRequest["cart_id"] = pref.getPreferenceStringData(pref.KEY_CART_ID)
        updateCartRequest["user_id"] = pref.getPreferenceStringData(pref.KEY_USER_ID)
        updateCartRequest["language"] = DrewelApplication.getInstance().getLanguage()
        updateCartRequest["product_id"] = cartIemList[position].productId!!
        updateCartRequest["quantity"] = quantity
        updateCartRequest["price"] = price

        val updateCartObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).updateCart(updateCartRequest)
        updateCartObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    addProductQuantityBt.isEnabled = true
                    DrewelApplication.getInstance().logoutWhenAccountDeactivated(result.response!!.isDeactivate!!, mContext)
                    progressDialog?.dismiss()
                    if (result.response!!.status!!) {
                        cartIemList[position].quantity = quantity
                        notifyItemChanged(position)
                        cartItemClickSubject.onNext(quantity)
                        pref.setPreferenceStringData(pref.KEY_CART_ID, result.response!!.data!!.cart!!.cartId!!)
                        CartRxJavaBus.getInstance().cartPublishSubject.onNext(result.response!!.data!!.cart!!.quantity!!)

                    } else {
                        notifyItemChanged(position)
                        com.drewel.drewel.utill.Utils.getInstance().showToast(mContext, result.response!!.message!!)
                    }
                }, { error ->
                    progressDialog?.dismiss()
                    addProductQuantityBt.isEnabled = true
                    notifyItemChanged(position)
                    com.drewel.drewel.utill.Utils.getInstance().showToast(mContext, error.message!!)
                    Log.e("TAG", "{$error.message}")
                }
                )
    }

    private fun callDeleteProductFromCartApi(position: Int, itemView: View) {
        progressDialog = CommonUtil.showLoadingDialog(mContext)
        itemView.isEnabled = false
        val pref = Prefs.getInstance(mContext)

        val deleteProductRequest = HashMap<String, String>()
        deleteProductRequest["cart_id"] = pref.getPreferenceStringData(pref.KEY_CART_ID)
        deleteProductRequest["user_id"] = pref.getPreferenceStringData(pref.KEY_USER_ID)
        deleteProductRequest["language"] = DrewelApplication.getInstance().getLanguage()
        deleteProductRequest["product_id"] = cartIemList[position].productId!!

        val deleteCartProductObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).deleteCartProduct(deleteProductRequest)
        deleteCartProductObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    itemView.isEnabled = true
                    DrewelApplication.getInstance().logoutWhenAccountDeactivated(result.response!!.isDeactivate!!, mContext)
                    progressDialog?.dismiss()
                    if (result.response!!.status!!) {
                        isAnyProductOutOfStock = false
                        cartIemList.removeAt(position)
                        notifyItemRemoved(position)
                        notifyItemRangeChanged(position, cartIemList.size)
                        cartItemClickSubject.onNext("")
                        pref.setPreferenceStringData(pref.KEY_CART_ID, result.response!!.data!!.cart!!.cartId!!)
                        CartRxJavaBus.getInstance().cartPublishSubject.onNext(result.response!!.data!!.cart!!.quantity!!)

                    } else {
                        notifyItemChanged(position)
                        com.drewel.drewel.utill.Utils.getInstance().showToast(mContext, result.response!!.message!!)
                    }
                }, { error ->
                    progressDialog?.dismiss()
                    itemView.isEnabled = false
                    notifyItemChanged(position)
                    com.drewel.drewel.utill.Utils.getInstance().showToast(mContext, error.message!!)
                    Log.e("TAG", "{$error.message}")
                }
                )
    }


    private fun showProductCategory(product: Cart): String {

        var category = ""

        for (i in product.category!!.indices) {
            category += if (i == product.category!!.size - 1) {
                if (DrewelApplication.getInstance().getLanguage().equals(Constants.LANGUAGE_ENGLISH)) {
                    product.category!![i].categoryName!!
                } else {
                    product.category!![i].ar_category_name!!
                }
            } else {
                if (DrewelApplication.getInstance().getLanguage().equals(Constants.LANGUAGE_ENGLISH)) {
                    product.category!![i].categoryName!! + ", "
                } else {
                    product.category!![i].ar_category_name!! + ", "
                }
            }

        }
        return category
    }

    private fun showProductSubcategory(product: Cart): String {

        var subCategory = ""

        if (product.subCategory?.isEmpty() != false) {

        } else {
            for (i in product.subCategory!!.indices) {
                subCategory += if (i == product.subCategory!!.size - 1) {
                    if (DrewelApplication.getInstance().getLanguage().equals(Constants.LANGUAGE_ENGLISH)) {
                        product.subCategory!![i].categoryName!!
                    } else {
                        product.subCategory!![i].ar_category_name!!
                    }
                } else {
                    if (DrewelApplication.getInstance().getLanguage().equals(Constants.LANGUAGE_ENGLISH)) {
                        product.subCategory!![i].categoryName!! + ", "
                    } else {
                        product.subCategory!![i].ar_category_name!! + ", "
                    }
                }

            }
        }

        return subCategory
    }

}


