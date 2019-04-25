package com.drewel.drewel.apicall.responsemodel.productRequestresponsemodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class ProductRequestResponse {

    @SerializedName("response")
    @Expose
    var response: Response? = null

}
