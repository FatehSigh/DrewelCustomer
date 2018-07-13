package com.os.drewel.apicall.responsemodel.cartdetailresponsemodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Data {

    @SerializedName("Cart")
    @Expose
    var cart: MutableList<Cart>? = null

    @SerializedName("cart_id")
    @Expose
    var cartId: String? = null

    @SerializedName("total_quantity")
    @Expose
    var quantity: String? = null

    @SerializedName("is_edited")
    @Expose
    var is_edited: String? = null

    @SerializedName("last_paid")
    @Expose
    var last_paid: String? = null

    @SerializedName("last_total_quantity")
    @Expose
    var last_total_quantity: String? = null
}
