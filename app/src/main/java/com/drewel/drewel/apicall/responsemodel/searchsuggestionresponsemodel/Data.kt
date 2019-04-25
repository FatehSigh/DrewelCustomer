package com.drewel.drewel.apicall.responsemodel.searchsuggestionresponsemodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Data {

    @SerializedName("Suggestions")
    @Expose
    var suggestions: List<String>? = null

}
