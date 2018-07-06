package com.os.drewel.apicall.responsemodel.driverlocationresponsemodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class DriverLocationResponse {

    @SerializedName("response")
    @Expose
    var response: Response? = null

}
