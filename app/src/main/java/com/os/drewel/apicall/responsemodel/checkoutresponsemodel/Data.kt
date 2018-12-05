package com.os.drewel.apicall.responsemodel.checkoutresponsemodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Data {

    @SerializedName("order_id")
    @Expose
    var orderId: String? = null

    @SerializedName("url")
    @Expose
    var url: String? = null
}
