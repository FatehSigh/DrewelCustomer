package com.drewel.drewel.apicall.responsemodel.cartdetailresponsemodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class CartDetailResponse {

    @SerializedName("response")
    @Expose
    var response: Response? = null

}
