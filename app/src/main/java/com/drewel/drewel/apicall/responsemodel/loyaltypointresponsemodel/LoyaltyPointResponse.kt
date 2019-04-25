package com.drewel.drewel.apicall.responsemodel.loyaltypointresponsemodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class LoyaltyPointResponse {

    @SerializedName("response")
    @Expose
    var response: Response? = null

}
