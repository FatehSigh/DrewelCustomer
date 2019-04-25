package com.drewel.drewel.apicall.requestmodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class CheckOutRequest() {

    @SerializedName("user_id")
    @Expose
    var userId: String? = null

    @SerializedName("language")
    @Expose
    var language: String? = null

    @SerializedName("amount")
    @Expose
    var amount: String? = null

    @SerializedName("quantity")
    @Expose
    var quantity: String? = null

    @SerializedName("cart_id")
    @Expose
    var cartId: String? = null

    @SerializedName("payment_mode")
    @Expose
    var paymentMode: String? = null

    @SerializedName("delivery_type")
    @Expose
    var deliveryType: String? = null

    @SerializedName("delivery_address")
    @Expose
    var deliveryAddress: String? = null


    @SerializedName("delivery_latitude")
    @Expose
    var deliveryLatitude: String? = null


    @SerializedName("delivery_longitude")
    @Expose
    var deliveryLongitude: String? = null


    @SerializedName("delivery_date")
    @Expose
    var deliveryDate: String? = null


    @SerializedName("delivery_start_time")
    @Expose
    var deliveryStartTime: String? = null


    @SerializedName("delivery_end_time")
    @Expose
    var deliveryEndTime: String? = null


    @SerializedName("loyalty_points")
    @Expose
    var loyaltyPoints: String? = null

    @SerializedName("delivery_charges")
    @Expose
    var deliveryCharges: String? = null

    @SerializedName("transaction_id")
    @Expose
    var transactionId: String? = null

    @SerializedName("deliver_to")
    @Expose
    var deliverTo: String? = null

    @SerializedName("deliver_mobile")
    @Expose
    var deliverMobile: String? = null

    @SerializedName("coupons")
    @Expose
    var coupons: MutableList<String>? = null

    @SerializedName("delivery_landmark")
    @Expose
    var deliveryLandmark: String? = null


    @SerializedName("address_id")
    @Expose
    var addressId: String? = null
    @SerializedName("delivery_address_type")
    @Expose
    var delivery_address_type: String? = null

}
