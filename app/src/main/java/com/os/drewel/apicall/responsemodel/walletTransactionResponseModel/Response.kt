package com.os.drewel.apicall.responsemodel.walletTransactionResponseModel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Response {

    @SerializedName("status")
    @Expose
    var status: Boolean? = null
    @SerializedName("message")
    @Expose
    var message: String? = null
    @SerializedName("data")
    @Expose
    var data: Data? = null
    @SerializedName("is_deactivate")
    @Expose
    var isDeactivate: Int? = null

}
