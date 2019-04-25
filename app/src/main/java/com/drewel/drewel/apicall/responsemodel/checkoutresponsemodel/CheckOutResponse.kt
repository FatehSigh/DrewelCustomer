package com.drewel.drewel.apicall.responsemodel.checkoutresponsemodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class CheckOutResponse {

    @SerializedName("response")
    @Expose
    var response: Response? = null

}
