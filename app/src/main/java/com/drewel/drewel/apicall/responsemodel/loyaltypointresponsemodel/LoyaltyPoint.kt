package com.drewel.drewel.apicall.responsemodel.loyaltypointresponsemodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class LoyaltyPoint {

    @SerializedName("loyalty_points")
    @Expose
    var loyaltyPoints: String? = null
    @SerializedName("type")
    @Expose
    var type: String? = null
    @SerializedName("order_id")
    @Expose
    var orderId: String? = null
    @SerializedName("user_id")
    @Expose
    var userId: String? = null
    @SerializedName("user_name")
    @Expose
    var userName: String? = null
    @SerializedName("created_at")
    @Expose
    var createdAt: String? = null
    @SerializedName("img")
    @Expose
    var img: String? = null

}
