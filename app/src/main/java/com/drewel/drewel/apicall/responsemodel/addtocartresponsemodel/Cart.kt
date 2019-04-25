package com.drewel.drewel.apicall.responsemodel.addtocartresponsemodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Cart {

    @SerializedName("cart_id")
    @Expose
    var cartId: String? = null
    @SerializedName("quantity")
    @Expose
    var quantity: String? = null

}
