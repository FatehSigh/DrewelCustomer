package com.os.drewel.apicall.responsemodel.myorderdetailresponsemodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Coupon {

    @SerializedName("coupone_code")
    @Expose
    var coupone_code: String? = null
    @SerializedName("amount")
    @Expose
    var amount: String? = null
    @SerializedName("coupon_id")
    @Expose
    var coupon_id: String? = null

    @SerializedName("coupon_percent")
    @Expose
    var coupon_percent: String? = null


}