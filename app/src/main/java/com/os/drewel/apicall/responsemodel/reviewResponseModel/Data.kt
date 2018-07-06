package com.os.drewel.apicall.responsemodel.reviewResponseModel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Data {

    @SerializedName("reviews")
    @Expose
    var reviews: List<Review>? = null

}
