package com.drewel.drewel.apicall.responsemodel.applyloyaltypointresponsemodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class ApplyLoyaltyPointResponse {

    @SerializedName("response")
    @Expose
    var response: Response? = null

}
