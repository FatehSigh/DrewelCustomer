package com.drewel.drewel.apicall.responsemodel.applyloyaltypointresponsemodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Data {

    @SerializedName("discount")
    @Expose
    var discount: Double? = null

    @SerializedName("loyalty_points")
    @Expose
    var loyalty_points: Double? = null
}
