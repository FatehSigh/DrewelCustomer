package com.os.drewel.apicall.responsemodel.loyaltypointresponsemodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Data {

    @SerializedName("LoyaltyPoints")
    @Expose
    var loyaltyPoints: List<LoyaltyPoint>? = null
    @SerializedName("current_loyalty_points")
    @Expose
    var currentLoyaltyPoints: String? = null

}
