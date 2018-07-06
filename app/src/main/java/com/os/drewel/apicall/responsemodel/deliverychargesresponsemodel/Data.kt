package com.os.drewel.apicall.responsemodel.deliverychargesresponsemodel

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Data() : Parcelable {

    @SerializedName("delivery_charge")
    @Expose
    var deliveryCharge: String? = null

    @SerializedName("is_edited")
    @Expose
    var is_edited: String? = null

    @SerializedName("last_paid")
    @Expose
    var last_paid: String? = null


    @SerializedName("same_day_delivery_charge")
    @Expose
    var sameDayDeliveryCharge: String? = null
    @SerializedName("expedite_delivery_charges")
    @Expose
    var expediteDeliveryCharges: String? = null
    @SerializedName("delivery_start_time")
    @Expose
    var deliveryStartTime: String? = null
    @SerializedName("delivery_end_time")
    @Expose
    var deliveryEndTime: String? = null
    @SerializedName("delivery_slots")
    @Expose
    var deliverySlots: String? = null
    @SerializedName("delivery_slot_duration")
    @Expose
    var deliverySlotDuration: String? = null

    constructor(parcel: Parcel) : this() {
        deliveryCharge = parcel.readString()
        sameDayDeliveryCharge = parcel.readString()
        expediteDeliveryCharges = parcel.readString()
        deliveryStartTime = parcel.readString()
        deliveryEndTime = parcel.readString()
        deliverySlots = parcel.readString()
        deliverySlotDuration = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(deliveryCharge)
        parcel.writeString(sameDayDeliveryCharge)
        parcel.writeString(expediteDeliveryCharges)
        parcel.writeString(deliveryStartTime)
        parcel.writeString(deliveryEndTime)
        parcel.writeString(deliverySlots)
        parcel.writeString(deliverySlotDuration)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Data> {
        override fun createFromParcel(parcel: Parcel): Data {
            return Data(parcel)
        }

        override fun newArray(size: Int): Array<Data?> {
            return arrayOfNulls(size)
        }
    }

}
