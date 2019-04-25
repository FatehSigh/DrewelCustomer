package com.drewel.drewel.apicall.responsemodel.addtocartresponsemodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class AddToCartResponse {

    @SerializedName("response")
    @Expose
    var response: Response? = null

}
