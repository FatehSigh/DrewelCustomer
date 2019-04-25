package com.drewel.drewel.apicall.responsemodel.productRequestresponsemodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.ArrayList

class Data {

    @SerializedName("requests")
    @Expose
    var productRequest: ArrayList<ProductRequest>? = null

}
