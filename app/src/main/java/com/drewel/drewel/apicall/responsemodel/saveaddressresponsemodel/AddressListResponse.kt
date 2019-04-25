package com.drewel.drewel.apicall.responsemodel.saveaddressresponsemodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class AddressListResponse {

    @SerializedName("response")
    @Expose
    var response: Response? = null

}
