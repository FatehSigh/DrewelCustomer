package com.drewel.drewel.apicall.responsemodel.productRequestresponsemodel

import java.io.Serializable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class ProductRequest : Serializable {

    @SerializedName("product_name")
    @Expose
    var product_name: String? = null
    @SerializedName("user_id")
    @Expose
    var user_id: String? = null
    @SerializedName("request_id")
    @Expose
    var request_id: String? = null
    @SerializedName("reply")
    @Expose
    var reply: String? = null
    @SerializedName("requested_on")
    @Expose
    var requested_on: String? = null
    @SerializedName("order_date")
    @Expose
    var order_date: String? = null
    @SerializedName("server_time")
    @Expose
    var server_time: String? = null
    var stopTimer: Boolean? = false
}
