package com.drewel.drewel.apicall.responsemodel.reviewResponseModel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class ReviewListResponse {

    @SerializedName("response")
    @Expose
    var response: Response? = null

}
