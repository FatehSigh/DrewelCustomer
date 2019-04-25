package com.drewel.drewel.apicall.responsemodel.walletTransactionResponseModel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Transaction {
    @SerializedName("id")
    @Expose
    var id: String? = null

    @SerializedName("amount")
    @Expose
    var amount: String? = null

    @SerializedName("transaction_id")
    @Expose
    var transaction_id: String? = null

    @SerializedName("type")
    @Expose
    var type: String? = null

    @SerializedName("order_id")
    @Expose
    var order_id: String? = null

    @SerializedName("date")
    @Expose
    var date: String? = null
}