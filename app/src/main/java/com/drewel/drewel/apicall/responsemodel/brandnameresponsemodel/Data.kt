package com.drewel.drewel.apicall.responsemodel.brandnameresponsemodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Data {

    @SerializedName("Brands")
    @Expose
    var brands: List<Brand>? = null

}
