package com.drewel.drewel.apicall.responsemodel.saveaddressresponsemodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Data {

    @SerializedName("Address")
    @Expose
    var address: Address? = null

}
