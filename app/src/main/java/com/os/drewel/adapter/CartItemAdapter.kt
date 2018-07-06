package com.os.drewel.adapter

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.nostra13.universalimageloader.core.ImageLoader
import com.os.drewel.R
import com.os.drewel.activity.ProductDetailActivity
import com.os.drewel.apicall.DrewelApi
import com.os.drewel.apicall.responsemodel.cartdetailresponsemodel.Cart
import com.os.drewel.application.DrewelApplication
import com.os.drewel.constant.AppIntentExtraKeys
import com.os.drewel.prefrences.Prefs
import com.os.drewel.rxbus.CartRxJavaBus
import com.os.drewel.utill.CommonUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.child_my_cart.view.*
import java.text.NumberFormat
import java.util.*

/**
 * Created by sharukhb on 3/13/2018.
 */

class CartItemAdapter(val mContext: Context, val cartIemList: MutableList<Cart>) : RecyclerView.Adapter<CartItemAdapter.CartItemHolder>() {

    lateinit var cartItemClickSubject: PublishSubject<String>
    var isAnyProductOutOfStock = false
    var progressDialog: ProgressDialog? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartItemHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.child_my_cart, parent, false)
        return CartItemHolder(view)
    }

    override fun onBindViewHolder(holder: CartItemHolder, position: Int) {
        ImageLoader.getInstance().displayImage(cartIemList[position].productImage, holder.itemView.productImageIv, DrewelApplication.getInstance().options)
        holder.itemView.tv_product_title.text = cartIemList[position].productName
        holder.itemView.productQuantityTv.text = cartIemList[position].quantity

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
            holder.itemView.tv_original_amount.text = NumberFormat.getInstance().format(totalAmount) + " " + mContext.getString(R.string.omr)

            val offerPrice = cartItem.offerPrice!!.toDouble()
            val totalOfferAmount = offerPrice * quantity
            val totalOfferAmountStr = NumberFormat.getInstance().format(totalOfferAmount) + " " + mContext.getString(R.string.omr)
            holder.itemView.tv_product_amount.text = totalOfferAmountStr

        } else {
            holder.itemView.original_price_layout.visibility = View.GONE
            holder.itemView.tv_product_amount.text = NumberFormat.getInstance().format(totalAmount) + " " + mContext.getString(R.string.omr)
        }

        if (cartIemList[position].outOfStock == 1) {
            isAnyProductOutOfStock = true
            holder.itemView.outOfStockTv.visibility = View.VISIBLE
        } else
            holder.itemView.outOfStockTv.visibility = View.GONE


        holder.itemView.tv_product_categories.text = showProductCategory(cartIemList[position])

        val subcategory = showProductSubcategory(cartIemList[position])
        holder.itemView.tv_product_sub_categories.text = subcategory

        if (subcategory.isBlank())
            holder.itemView.tv_product_sub_categories.visibility = View.GONE
        else
            holder.itemView.tv_product_sub_categories.visibility = View.VISIBLE
    }


    override fun getItemCount(): Int {
        return cartIemList.size;
    }


    inner class CartItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        init {
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

                    callUpdateCartApi(adapterPosition, quantity.toString(), price.toString(), itemView)
                }
                R.id.removeProductQuantityBt -> {
                    if (cartIemList[adapterPosition].quantity!!.toInt() > 1) {
                        val quantity = cartIemList[adapterPosition].quantity!!.toInt() - 1

                        val price = if (cartIemList[adapterPosition].offerPrice.isNullOrEmpty())
                            cartIemList[adapterPosition].productPrice!!.toDouble() * quantity
                        else
                            cartIemList[adapterPosition].offerPrice!!.toDouble() * quantity

                        callUpdateCartApi(adapterPosition, quantity.toString(), price.toString(), itemView)
                    } else {
                        callDeleteProductFromCartApi(adapterPosition, itemView)
                        /*Delete Item*/
                    }
                }

                R.id.imv_product_delete -> {
                    callDeleteProductFromCartApi(adapterPosition, itemView)
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
                        Toast.makeText(mContext, result.response!!.message, Toast.LENGTH_LONG).show()
                    }
                }, { error ->
                    progressDialog?.dismiss()
                    addProductQuantityBt.isEnabled = true
                    notifyItemChanged(position)
                    Toast.makeText(mContext, error.message, Toast.LENGTH_LONG).show()
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
                        Toast.makeText(mContext, result.response!!.message, Toast.LENGTH_LONG).show()
                    }
                }, { error ->
                    progressDialog?.dismiss()
                    itemView.isEnabled = false
                    notifyItemChanged(position)
                    Toast.makeText(mContext, error.message, Toast.LENGTH_LONG).show()
                    Log.e("TAG", "{$error.message}")
                }
                )
    }


    private fun showProductCategory(product: Cart): String {

        var category = ""

        for (i in product.category!!.indices) {
            category += if (i == product.category!!.size - 1) {
                product.category!![i].categoryName!!
            } else
                product.category!![i].categoryName!! + ", "
        }

        return category
    }

    private fun showProductSubcategory(product: Cart): String {

        var subCategory = ""

        if (product.subCategory?.isEmpty() != false) {

        } else {
            for (i in product.subCategory!!.indices) {
                subCategory += if (i == product.subCategory!!.size - 1) {
                    product.subCategory!![i].categoryName!!
                } else
                    product.subCategory!![i].categoryName!! + ", "
            }
        }

        return subCategory
    }

}


