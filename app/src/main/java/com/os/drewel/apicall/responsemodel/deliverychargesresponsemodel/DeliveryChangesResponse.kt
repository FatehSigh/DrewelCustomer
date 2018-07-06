package com.os.drewel.apicall.responsemodel.deliverychargesresponsemodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class DeliveryChangesResponse {

    @SerializedName("response")
    @Expose
    var response: Response? = null

}
