package com.os.drewel.apicall.responsemodel.loginresponsemodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Datum {

    @SerializedName("user_id")
    @Expose
    var userId: String? = null
    @SerializedName("first_name")
    @Expose
    var firstName: String? = null
    @SerializedName("last_name")
    @Expose
    var lastName: String? = null
    @SerializedName("email")
    @Expose
    var email: String? = null
    @SerializedName("role_id")
    @Expose
    var roleId: String? = null
    @SerializedName("latitude")
    @Expose
    var latitude: String? = null
    @SerializedName("longitude")
    @Expose
    var longitude: String? = null
    @SerializedName("is_notification")
    @Expose
    var isNotification: String? = null
    @SerializedName("fb_id")
    @Expose
    var fbId: String? = null
    @SerializedName("remember_token")
    @Expose
    var rememberToken: String? = null
    @SerializedName("modified")
    @Expose
    var modified: String? = null
    @SerializedName("is_mobileverify")
    @Expose
    var isMobileverify: String? = null
    @SerializedName("img")
    @Expose
    var img: String? = null


    @SerializedName("mobile_number")
    @Expose
    var mobile_number: String? = null

    @SerializedName("country_code")
    @Expose
    var country_code: String? = null


    @SerializedName("authotp")
    @Expose
    var authotp: String? = null


    @SerializedName("address")
    @Expose
    var address: String? = null

    @SerializedName("address_name")
    @Expose
    var addressName: String? = null


    @SerializedName("address_latitude")
    @Expose
    var addressLatitude: String? = null

    @SerializedName("address_longitude")
    @Expose
    var addressLongitude: String? = null

    @SerializedName("address_id")
    @Expose
    var addressId: String? = null


    @SerializedName("cart_quantity")
    @Expose
    var cartQuantity: String? = null

    @SerializedName("cart_id")
    @Expose
    var cartId: String? = null

    @SerializedName("user_mobile")
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

}
