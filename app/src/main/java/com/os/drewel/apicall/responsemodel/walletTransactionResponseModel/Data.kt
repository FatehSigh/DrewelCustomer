package com.os.drewel.apicall.responsemodel.walletTransactionResponseModel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Data {

    @SerializedName("Transactions")
    @Expose
    var transactions: List<Transaction>? = null
    @SerializedName("wallet_balance")
    @Expose
    var wallet_balance: String? = null

}
