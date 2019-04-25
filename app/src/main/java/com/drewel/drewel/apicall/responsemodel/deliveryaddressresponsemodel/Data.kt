package com.drewel.drewel.apicall.responsemodel.deliveryaddressresponsemodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Data : Serializable {

    @SerializedName("Address")
    @Expose
    var address: MutableList<Address>? = null

}
