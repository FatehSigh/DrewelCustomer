package com.os.drewel.apicall.responsemodel.couponresponsemodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Data {

    @SerializedName("Coupons")
    @Expose
    var coupons: List<Coupon>? = null

}
