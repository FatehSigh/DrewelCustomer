package com.drewel.drewel.apicall.responsemodel.couponresponsemodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class CouponListResponse {

    @SerializedName("response")
    @Expose
    var response: Response? = null

}
