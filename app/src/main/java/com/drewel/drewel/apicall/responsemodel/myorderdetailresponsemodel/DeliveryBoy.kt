package com.drewel.drewel.apicall.responsemodel.myorderdetailresponsemodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName



class DeliveryBoy {


    @SerializedName("id")
    @Expose
     var id: String? = null
    @SerializedName("name")
    @Expose
     var name: String? = null
    @SerializedName("mobile_number")
    @Expose
     var mobileNumber: String? = null

}