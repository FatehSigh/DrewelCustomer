package com.drewel.drewel.apicall.responsemodel.addtocartresponsemodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Data {

    @SerializedName("Cart")
    @Expose
    var cart: Cart? = null

}
