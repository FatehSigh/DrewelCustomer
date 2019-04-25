package com.drewel.drewel.apicall.responsemodel.deliveryaddressresponsemodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Address {

    @SerializedName("id")
    @Expose
    var id: String? = null
    @SerializedName("address")
    @Expose
    var address: String? = null
    @SerializedName("latitude")
    @Expose
    var latitude: String? = null
    @SerializedName("longitude")
    @Expose
    var longitude: String? = null
    @SerializedName("is_default")
    @Expose
    var isDefault: String? = null

    @SerializedName("name")
    @Expose
    var name: String? = null

    @SerializedName("mobile_number")
    @Expose
    var mobileNumber: String? = null

    @SerializedName("landmark")
    @Expose
    var landmark: String? = null

    @SerializedName("full_address")
    @Expose
    var fullAddress: String? = null


    @SerializedName("user_name")
    @Expose
    var username: String? = null

    @SerializedName("delivery_address_type")
    @Expose
    var delivery_address_type: String? = null
    @SerializedName("zip_code")
    @Expose
    var zip_code: String? = null

    override fun toString(): String {
        return "Address(id=$id, address=$address, latitude=$latitude, longitude=$longitude, isDefault=$isDefault, name=$name, mobileNumber=$mobileNumber, landmark=$landmark, fullAddress=$fullAddress, username=$username, delivery_address_type=$delivery_address_type, zip_code=$zip_code)"
    }

}
