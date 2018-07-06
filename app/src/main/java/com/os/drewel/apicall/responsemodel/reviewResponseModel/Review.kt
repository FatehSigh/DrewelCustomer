package com.os.drewel.apicall.responsemodel.reviewResponseModel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Review {

    @SerializedName("reviews")
    @Expose
    var reviews: String? = null
    @SerializedName("ratings")
    @Expose
    var ratings: String? = null
    @SerializedName("user_id")
    @Expose
    var user_id: String? = null
    @SerializedName("id")
    @Expose
    var id: String? = null
    @SerializedName("user_name")
    @Expose
    var user_name: String? = null

}
