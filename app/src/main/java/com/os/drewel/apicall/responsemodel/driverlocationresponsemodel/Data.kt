package com.os.drewel.apicall.responsemodel.driverlocationresponsemodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Data {

    @SerializedName("location")
    @Expose
    var location: Location? = null

}
