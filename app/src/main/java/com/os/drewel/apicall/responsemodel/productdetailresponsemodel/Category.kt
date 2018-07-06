package com.os.drewel.apicall.responsemodel.productdetailresponsemodel

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Category() :Parcelable {

    @SerializedName("id")
    @Expose
    var id: String? = null
    @SerializedName("category_name")
    @Expose
    var categoryName: String? = null
    @SerializedName("img")
    @Expose
    var img: String? = null

    constructor(parcel: Parcel) : this() {
        id = parcel.readString()
        categoryName = parcel.readString()
        img = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(categoryName)
        parcel.writeString(img)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return "Category(id=$id, categoryName=$categoryName, img=$img)"
    }

    companion object CREATOR : Parcelable.Creator<Category> {
        override fun createFromParcel(parcel: Parcel): Category {
            return Category(parcel)
        }

        override fun newArray(size: Int): Array<Category?> {
            return arrayOfNulls(size)
        }
    }

}
