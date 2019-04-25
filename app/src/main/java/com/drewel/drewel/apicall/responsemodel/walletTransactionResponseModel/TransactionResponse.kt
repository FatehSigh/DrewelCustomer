package com.drewel.drewel.apicall.responsemodel.walletTransactionResponseModel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class TransactionResponse {

    @SerializedName("response")
    @Expose
    var response: Response? = null

}
