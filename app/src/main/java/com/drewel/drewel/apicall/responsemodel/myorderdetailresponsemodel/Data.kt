package com.drewel.drewel.apicall.responsemodel.myorderdetailresponsemodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Data {

    @SerializedName("Order")
    @Expose
    var order: Order? = null
    @SerializedName("Products")
    @Expose
    var products: List<Product>? = null
    @SerializedName("Coupons")
    @Expose
    var coupons: List<Coupon>? = null


    @SerializedName("DeliveryBoy")
    @Expose
    var deliveryBoy: DeliveryBoy? = null

}
