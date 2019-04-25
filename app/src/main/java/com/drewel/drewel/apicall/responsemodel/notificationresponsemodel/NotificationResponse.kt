package com.drewel.drewel.apicall.responsemodel.notificationresponsemodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class NotificationResponse {

    @SerializedName("response")
    @Expose
    var response: Response? = null

}
