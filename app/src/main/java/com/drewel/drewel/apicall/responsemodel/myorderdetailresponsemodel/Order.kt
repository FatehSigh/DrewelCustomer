package com.drewel.drewel.apicall.responsemodel.myorderdetailresponsemodel

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
    @SerializedName("total_amount")
    @Expose
    var totalAmount: String? = null
    @SerializedName("transaction_id")
    @Expose
    var transactionId: String? = null
    @SerializedName("net_amount")
    @Expose
    var netAmount: String? = null
    @SerializedName("loyalty_points")
    @Expose
    var loyaltyPoints: String? = null
    @SerializedName("loyalty_discount")
    @Expose
    var loyaltyDiscount: String? = null
    @SerializedName("coupon_discount")
    @Expose
    var couponDiscount: String? = null
    @SerializedName("delivery_charges")
    @Expose
    var deliveryCharges: String? = null
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
    @SerializedName("deliver_to")
    @Expose
    var deliverTo: String? = null
    @SerializedName("deliver_mobile")
    @Expose
    var deliverMobile: String? = null
    @SerializedName("delivery_address")
    @Expose
    var deliveryAddress: String? = null
    @SerializedName("order_date")
    @Expose
    var orderDate: String? = null

    @SerializedName("cancelled_before")
    @Expose
    var cancelledBefore: String? = null



    @SerializedName("delivery_latitude")
    @Expose
    var deliveryLatitude: String? = null

    @SerializedName("delivery_longitude")
    @Expose
    var deliveryLongitude: String? = null

}
