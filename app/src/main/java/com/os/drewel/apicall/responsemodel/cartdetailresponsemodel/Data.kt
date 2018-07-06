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

}
