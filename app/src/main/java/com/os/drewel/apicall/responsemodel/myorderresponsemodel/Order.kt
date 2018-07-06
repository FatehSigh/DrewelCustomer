package com.os.drewel.apicall.responsemodel.myorderresponsemodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Order {
    @SerializedName("order_id")
    @Expose
    var orderId: String? = null
    @SerializedName("delivery_date")
    @Expose
    var deliveryDate: String? = null
    @SerializedName("delivery_start_time")
    @Expose
    var deliveryStartTime: String? = null
    @SerializedName("delivery_end_time")
    @Expose
    var deliveryEndTime: String? = null
    @SerializedName("total_quantity")
    @Expose
    var totalQuantity: String? = null
    @SerializedName("order_delivery_status")
    @Expose
    var orderDeliveryStatus: String? = null
    @SerializedName("order_status")
    @Expose
    var orderStatus: String? = null
    @SerializedName("payment_mode")
    @Expose
    var paymentMode: String? = null
    @SerializedName("is_cancelled")
    @Expose
    var isCancelled: String? = null

    @SerializedName("is_edited")
    @Expose
    var is_edited: String? = null

    @SerializedName("order_date")
    @Expose
    var order_date: String? = null
    @SerializedName("server_time")
    @Expose
    var server_time: String? = null

    @SerializedName("total_amount")
    @Expose
    var totalAmount: String? = null

    var stopTimer: Boolean? = false

}
