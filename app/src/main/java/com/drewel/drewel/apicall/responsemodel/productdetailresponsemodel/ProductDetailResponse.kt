package com.drewel.drewel.apicall.responsemodel.productdetailresponsemodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class ProductDetailResponse {

    @SerializedName("response")
    @Expose
    var response: Response? = null

}
