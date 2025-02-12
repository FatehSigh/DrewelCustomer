package com.drewel.drewel.apicall.requestmodel

import android.os.Parcel
import android.os.Parcelable

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class SignUpRequest() : Parcelable {

    @SerializedName("fb_id")
    @Expose
     var fbId: String ? = null
    @SerializedName("language")
    @Expose
     var language: String? = null
    @SerializedName("device_id")
    @Expose
     var deviceId: String? = null
    @SerializedName("device_type")
    @Expose
     var deviceType: String ? = null
    @SerializedName("email")
    @Expose
     var email: String ? = null
    @SerializedName("first_name")
    @Expose
     var firstName: String ? = null
    @SerializedName("last_name")
    @Expose
     var lastName: String ? = null
    @SerializedName("mobile_number")
    @Expose
     var mobileNumber: String ? = null
    @SerializedName("country_code")
    @Expose
     var countryCode: String ? = null
    @SerializedName("password")
    @Expose
     var password: String ? = null

    @SerializedName("role_id")
    @Expose
     var roleId: String ? = null


    @SerializedName("profile_image")
    @Expose
    var profileImage: String ? = null

    constructor(parcel: Parcel) : this() {
        this.fbId = parcel.readValue(String::class.java.classLoader) as String?
        this.roleId = parcel.readValue(String::class.java.classLoader) as String?
        this.language = parcel.readValue(String::class.java.classLoader) as String?
        this.deviceId = parcel.readValue(String::class.java.classLoader) as String?
        this.deviceType = parcel.readValue(String::class.java.classLoader) as String?
        this.email = parcel.readValue(String::class.java.classLoader) as String?
        this.firstName = parcel.readValue(String::class.java.classLoader) as String?
        this.lastName = parcel.readValue(String::class.java.classLoader) as String?
        this.mobileNumber = parcel.readValue(String::class.java.classLoader) as String?
        this.countryCode = parcel.readValue(String::class.java.classLoader) as String?
        this.password = parcel.readValue(String::class.java.classLoader) as String?
        this.profileImage = parcel.readValue(String::class.java.classLoader) as String?
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeValue(fbId)
        dest.writeValue(roleId)
        dest.writeValue(language)
        dest.writeValue(deviceId)
        dest.writeValue(deviceType)
        dest.writeValue(email)
        dest.writeValue(firstName)
        dest.writeValue(lastName)
        dest.writeValue(mobileNumber)
        dest.writeValue(countryCode)
        dest.writeValue(password)
        dest.writeValue(profileImage)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SignUpRequest> {
        override fun createFromParcel(parcel: Parcel): SignUpRequest {
            return SignUpRequest(parcel)
        }

        override fun newArray(size: Int): Array<SignUpRequest?> {
            return arrayOfNulls(size)
        }
    }


}
