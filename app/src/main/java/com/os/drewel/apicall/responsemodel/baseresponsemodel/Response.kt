package com.os.drewel.apicall.responsemodel.baseresponsemodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Response {

    @SerializedName("status")
    @Expose
    var status: Boolean? = null
    @SerializedName("message")
    @Expose
    var message: String? = null
    @SerializedName("is_deactivate")
    @Expose
    var isDeactivate: String? = null


}
