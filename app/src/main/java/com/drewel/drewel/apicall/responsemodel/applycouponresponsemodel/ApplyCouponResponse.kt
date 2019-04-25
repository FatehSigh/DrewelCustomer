package com.drewel.drewel.apicall.responsemodel.applycouponresponsemodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class ApplyCouponResponse {

    @SerializedName("response")
    @Expose
    var response: Response? = null

}
